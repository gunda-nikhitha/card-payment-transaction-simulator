package com.jfs.cpts.service;

import com.jfs.cpts.dto.admin.UserManagementResponse;
import com.jfs.cpts.dto.auth.RegisterRequest;
import com.jfs.cpts.dto.user.ChangePasswordRequest;
import com.jfs.cpts.dto.user.UpdateProfileRequest;
import com.jfs.cpts.entity.Role;
import com.jfs.cpts.entity.User;
import com.jfs.cpts.exception.AppException;
import com.jfs.cpts.exception.DuplicateResourceException;
import com.jfs.cpts.exception.ResourceNotFoundException;
import com.jfs.cpts.mapper.EntityMapper;
import com.jfs.cpts.repository.TransactionRepository;
import com.jfs.cpts.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user account operations, profile updates, and admin user management.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityMapper mapper;

    public UserService(UserRepository userRepository,
                       TransactionRepository transactionRepository,
                       PasswordEncoder passwordEncoder,
                       EntityMapper mapper) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    /**
     * Register a new user with USER role.
     */
    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("Registered new user with id: {} and email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    /**
     * Find user by email.
     */
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    /**
     * Find user by id.
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Update user profile name.
     */
    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findById(userId);
        user.setName(request.name().trim());
        return userRepository.save(user);
    }

    /**
     * Change user password with validation.
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findById(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new AppException("Current password does not match", HttpStatus.BAD_REQUEST);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new AppException("New password cannot be the same as current password", HttpStatus.BAD_REQUEST);
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user id: {}", userId);
    }

    /**
     * Admin: Get all users with transaction counts.
     */
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> {
            long txnCount = transactionRepository.countByUserId(user.getId());
            return mapper.toUserManagementResponse(user, txnCount);
        });
    }

    /**
     * Admin: Toggle user active status.
     */
    @Transactional
    public UserManagementResponse setUserActive(Long userId, boolean active) {
        User user = findById(userId);
        user.setActive(active);
        user = userRepository.save(user);
        long txnCount = transactionRepository.countByUserId(user.getId());
        log.info("Updated user id: {} active status to: {}", userId, active);
        return mapper.toUserManagementResponse(user, txnCount);
    }

    /**
     * Admin: Update user role.
     */
    @Transactional
    public UserManagementResponse setUserRole(Long userId, Role role) {
        User user = findById(userId);
        user.setRole(role);
        user = userRepository.save(user);
        long txnCount = transactionRepository.countByUserId(user.getId());
        log.info("Updated user id: {} role to: {}", userId, role);
        return mapper.toUserManagementResponse(user, txnCount);
    }
}
