package com.minted.api.dashboard.controller;

import com.minted.api.dashboard.dto.ChartDataResponse;
import com.minted.api.dashboard.dto.DashboardCardRequest;
import com.minted.api.dashboard.dto.DashboardCardResponse;
import com.minted.api.user.entity.User;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.repository.UserRepository;
import com.minted.api.analytics.service.AnalyticsService;
import com.minted.api.dashboard.service.DashboardCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard-cards")
@RequiredArgsConstructor
public class DashboardCardController {

    private final DashboardCardService cardService;
    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCards(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<DashboardCardResponse> cards = cardService.getAllByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", cards
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getAllActiveCards(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<DashboardCardResponse> cards = cardService.getAllActiveByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", cards
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCardById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        DashboardCardResponse card = cardService.getById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", card
        ));
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<Map<String, Object>> getCardData(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        ChartDataResponse data = analyticsService.getCardData(userId, id, startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCard(
            @Valid @RequestBody DashboardCardRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        DashboardCardResponse card = cardService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", card,
                "message", "Dashboard card created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody DashboardCardRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        DashboardCardResponse card = cardService.update(id, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", card,
                "message", "Dashboard card updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCard(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        cardService.delete(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dashboard card deleted successfully"
        ));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleCard(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        cardService.toggleActive(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dashboard card status toggled successfully"
        ));
    }

    @PutMapping("/reorder")
    public ResponseEntity<Map<String, Object>> reorderCards(
            @RequestBody List<Long> cardIds,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        cardService.reorderCards(userId, cardIds);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dashboard cards reordered successfully"
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}

