package com.minted.api.controller;

import com.minted.api.dto.*;
import com.minted.api.entity.User;
import com.minted.api.exception.ResourceNotFoundException;
import com.minted.api.repository.LlmModelRepository;
import com.minted.api.repository.UserRepository;
import com.minted.api.service.LlmConfigService;
import com.minted.api.service.MerchantMappingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/llm-config")
@RequiredArgsConstructor
public class LlmConfigController {

    private final LlmConfigService llmConfigService;
    private final MerchantMappingService merchantMappingService;
    private final LlmModelRepository modelRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig(Authentication authentication) {
        Long userId = getUserId(authentication);
        LlmConfigResponse config = llmConfigService.getConfig(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", config));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> saveConfig(
            @RequestBody LlmConfigRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        LlmConfigResponse config = llmConfigService.saveConfig(request, userId);
        return ResponseEntity.ok(Map.of("success", true, "data", config));
    }

    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> getActiveModels() {
        List<LlmModelResponse> models = modelRepository.findByIsActiveTrueOrderByIsDefaultDescNameAsc().stream()
                .map(LlmModelResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", models));
    }

    @GetMapping("/mappings")
    public ResponseEntity<Map<String, Object>> getMappings(Authentication authentication) {
        Long userId = getUserId(authentication);
        List<MerchantMappingResponse> mappings = merchantMappingService.getMappings(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", mappings));
    }

    @PostMapping("/mappings")
    public ResponseEntity<Map<String, Object>> createMapping(
            @Valid @RequestBody MerchantMappingRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        MerchantMappingResponse mapping = merchantMappingService.createMapping(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", mapping));
    }

    @PutMapping("/mappings/{id}")
    public ResponseEntity<Map<String, Object>> updateMapping(
            @PathVariable Long id,
            @Valid @RequestBody MerchantMappingRequest request,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        MerchantMappingResponse mapping = merchantMappingService.updateMapping(id, request, userId);
        return ResponseEntity.ok(Map.of("success", true, "data", mapping));
    }

    @DeleteMapping("/mappings/{id}")
    public ResponseEntity<Void> deleteMapping(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);
        merchantMappingService.deleteMapping(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
