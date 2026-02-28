package com.minted.api.analytics.controller;

import com.minted.api.analytics.dto.AnalyticsSummaryResponse;
import com.minted.api.analytics.dto.BudgetSummaryResponse;
import com.minted.api.analytics.dto.CategoryWiseResponse;
import com.minted.api.dashboard.dto.ChartDataResponse;
import com.minted.api.analytics.dto.SpendingActivityResponse;
import com.minted.api.analytics.dto.TotalBalanceResponse;
import com.minted.api.analytics.dto.TrendResponse;
import com.minted.api.user.entity.User;
import com.minted.api.transaction.enums.TransactionType;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long accountId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        AnalyticsSummaryResponse summary = analyticsService.getSummary(userId, startDate, endDate, accountId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", summary
        ));
    }

    @GetMapping("/category-wise")
    public ResponseEntity<Map<String, Object>> getCategoryWise(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long accountId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        TransactionType effectiveType = type != null ? type : TransactionType.EXPENSE;
        List<CategoryWiseResponse> data = analyticsService.getCategoryWise(userId, startDate, endDate, effectiveType, accountId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }

    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getTrend(
            @RequestParam(defaultValue = "6") int months,
            @RequestParam(required = false) Long accountId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<TrendResponse> data = analyticsService.getTrend(userId, months, accountId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }

    @GetMapping("/spending-activity")
    public ResponseEntity<Map<String, Object>> getSpendingActivity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<SpendingActivityResponse> data = analyticsService.getSpendingActivity(userId, startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }

    @GetMapping("/total-balance")
    public ResponseEntity<Map<String, Object>> getTotalBalance(Authentication authentication) {
        Long userId = getUserId(authentication);
        TotalBalanceResponse data = analyticsService.getTotalBalance(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }

    @GetMapping("/budget-summary")
    public ResponseEntity<Map<String, Object>> getBudgetSummary(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<BudgetSummaryResponse> data = analyticsService.getBudgetSummary(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
