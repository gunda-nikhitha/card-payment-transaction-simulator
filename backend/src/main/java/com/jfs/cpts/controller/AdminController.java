package com.jfs.cpts.controller;

import com.jfs.cpts.dto.admin.UserManagementResponse;
import com.jfs.cpts.dto.dashboard.DashboardSummary;
import com.jfs.cpts.dto.dashboard.StatusDistribution;
import com.jfs.cpts.dto.dashboard.TrendDataPoint;
import com.jfs.cpts.dto.transaction.TransactionResponse;
import com.jfs.cpts.entity.Role;
import com.jfs.cpts.service.DashboardService;
import com.jfs.cpts.service.TransactionService;
import com.jfs.cpts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative views: global metrics, user management, and system-wide transactions")
public class AdminController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final DashboardService dashboardService;

    public AdminController(UserService userService,
                           TransactionService transactionService,
                           DashboardService dashboardService) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get global system-wide dashboard summary metrics")
    public ResponseEntity<DashboardSummary> getGlobalDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboardSummary());
    }

    @GetMapping("/status-distribution")
    @Operation(summary = "Get global transaction status distribution")
    public ResponseEntity<List<StatusDistribution>> getGlobalDistribution() {
        return ResponseEntity.ok(dashboardService.getStatusDistribution(null));
    }

    @GetMapping("/trends")
    @Operation(summary = "Get global transaction trends")
    public ResponseEntity<List<TrendDataPoint>> getGlobalTrends(@RequestParam(defaultValue = "14") int days) {
        return ResponseEntity.ok(dashboardService.getTrends(null, days));
    }

    @GetMapping("/users")
    @Operation(summary = "Get paginated user accounts with transaction counts")
    public ResponseEntity<Page<UserManagementResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 100), Sort.by(direction, sortBy));
        return ResponseEntity.ok(userService.getAllUsers(pageRequest));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Toggle user active/disabled status")
    public ResponseEntity<UserManagementResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(userService.setUserActive(id, active));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Update user role (USER or ADMIN)")
    public ResponseEntity<UserManagementResponse> updateUserRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        return ResponseEntity.ok(userService.setUserRole(id, role));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get all transactions across all users with filters and pagination")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TransactionResponse> result = transactionService.getAllTransactions(
                search, status, fromDate, toDate, minAmount, maxAmount, sortBy, sortDir, page, size
        );
        return ResponseEntity.ok(result);
    }
}
