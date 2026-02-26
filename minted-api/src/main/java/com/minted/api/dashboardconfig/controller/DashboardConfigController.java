package com.minted.api.dashboardconfig.controller;

import com.minted.api.dashboardconfig.dto.DashboardConfigRequest;
import com.minted.api.dashboardconfig.dto.DashboardConfigResponse;
import com.minted.api.dashboardconfig.service.DashboardConfigService;
import com.minted.api.user.entity.User;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard-config")
@RequiredArgsConstructor
public class DashboardConfigController {

    private final DashboardConfigService dashboardConfigService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig(Authentication authentication) {
        Long userId = getUserId(authentication);
        DashboardConfigResponse config = dashboardConfigService.getConfig(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", config
        ));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> saveConfig(
            @RequestBody DashboardConfigRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        DashboardConfigResponse config = dashboardConfigService.saveConfig(request, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", config,
                "message", "Dashboard configuration saved successfully"
        ));
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
