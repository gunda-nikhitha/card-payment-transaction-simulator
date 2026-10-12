package com.jfs.cpts.controller;

import com.jfs.cpts.dto.ai.ChatRequest;
import com.jfs.cpts.dto.ai.ChatResponse;
import com.jfs.cpts.dto.ai.ConversationResponse;
import com.jfs.cpts.entity.User;
import com.jfs.cpts.security.RateLimiterService;
import com.jfs.cpts.service.AiService;
import com.jfs.cpts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Learning Assistant", description = "RAG-powered conversational assistant for card payment concepts")
public class AiController {

    private final AiService aiService;
    private final UserService userService;
    private final RateLimiterService rateLimiterService;

    public AiController(AiService aiService,
                        UserService userService,
                        RateLimiterService rateLimiterService) {
        this.aiService = aiService;
        this.userService = userService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Send a question to the Payment Learning Assistant")
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        rateLimiterService.checkAiRateLimit(userDetails.getUsername());
        User user = userService.findByEmail(userDetails.getUsername());
        ChatResponse response = aiService.processChat(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get all chat conversation threads for the logged-in user")
    public ResponseEntity<List<ConversationResponse>> getConversations(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(aiService.getUserConversations(user.getId()));
    }

    @GetMapping("/conversations/{id}")
    @Operation(summary = "Get a specific chat conversation with its full message history")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(aiService.getConversation(id, user.getId()));
    }

    @DeleteMapping("/conversations/{id}")
    @Operation(summary = "Delete a conversation thread")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        aiService.deleteConversation(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
