package com.jfs.cpts.controller;

import com.jfs.cpts.dto.dashboard.DashboardSummary;
import com.jfs.cpts.dto.dashboard.StatusDistribution;
import com.jfs.cpts.dto.dashboard.TrendDataPoint;
import com.jfs.cpts.entity.User;
import com.jfs.cpts.service.DashboardService;
import com.jfs.cpts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Personalized transaction analytics and metric trends")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    public DashboardController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get transaction metrics summary for the logged-in user")
    public ResponseEntity<DashboardSummary> getUserSummary(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(dashboardService.getUserDashboardSummary(user.getId()));
    }

    @GetMapping("/status-distribution")
    @Operation(summary = "Get transaction status distribution for donut/pie chart")
    public ResponseEntity<List<StatusDistribution>> getStatusDistribution(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(dashboardService.getStatusDistribution(user.getId()));
    }

    @GetMapping("/trends")
    @Operation(summary = "Get daily transaction trends over the specified time window")
    public ResponseEntity<List<TrendDataPoint>> getTrends(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "14") int days) {
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(dashboardService.getTrends(user.getId(), days));
    }
}
