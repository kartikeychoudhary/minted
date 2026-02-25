package com.minted.api.split.controller;

import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.split.dto.*;
import com.minted.api.split.service.SplitService;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/splits")
@RequiredArgsConstructor
public class SplitTransactionController {

    private final SplitService splitService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSplits(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<SplitTransactionResponse> splits = splitService.getAllByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", splits
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSplitById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        SplitTransactionResponse split = splitService.getById(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", split
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSplit(
            @Valid @RequestBody SplitTransactionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        SplitTransactionResponse split = splitService.create(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "data", split,
                "message", "Split transaction created successfully"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSplit(
            @PathVariable Long id,
            @Valid @RequestBody SplitTransactionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        SplitTransactionResponse split = splitService.update(id, request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", split,
                "message", "Split transaction updated successfully"
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSplit(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        splitService.delete(id, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Split transaction deleted successfully"
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getBalanceSummary(Authentication authentication) {
        Long userId = getUserId(authentication);
        SplitBalanceSummaryResponse summary = splitService.getBalanceSummary(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", summary
        ));
    }

    @GetMapping("/balances")
    public ResponseEntity<Map<String, Object>> getFriendBalances(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<FriendBalanceResponse> balances = splitService.getFriendBalances(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", balances
        ));
    }

    @PostMapping("/settle")
    public ResponseEntity<Map<String, Object>> settleFriend(
            @Valid @RequestBody SettleRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        splitService.settleFriend(request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Settlement completed successfully"
        ));
    }

    @GetMapping("/friend/{friendId}/shares")
    public ResponseEntity<Map<String, Object>> getSharesByFriend(
            @PathVariable Long friendId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        List<SplitShareResponse> shares = splitService.getSharesByFriend(friendId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", shares
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
