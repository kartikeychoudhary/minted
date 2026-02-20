package com.minted.api.controller;

import com.minted.api.dto.*;
import com.minted.api.service.DefaultListsService;
import com.minted.api.service.JobExecutionService;
import com.minted.api.service.SystemSettingService;
import com.minted.api.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JobExecutionService jobExecutionService;
    private final DefaultListsService defaultListsService;
    private final UserManagementService userManagementService;
    private final SystemSettingService systemSettingService;

    // --- Jobs & Schedules ---

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobExecutionResponse>> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(jobExecutionService.getAllJobExecutions(pageable));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobExecutionResponse> getJobExecution(@PathVariable Long id) {
        return ResponseEntity.ok(jobExecutionService.getJobExecutionById(id));
    }

    @PostMapping("/jobs/{jobName}/trigger")
    public ResponseEntity<Void> triggerJob(@PathVariable String jobName) {
        jobExecutionService.triggerJobManually(jobName);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/schedules")
    public ResponseEntity<List<JobScheduleConfigResponse>> getSchedules() {
        return ResponseEntity.ok(jobExecutionService.getAllScheduleConfigs());
    }

    @PutMapping("/schedules/{id}")
    public ResponseEntity<JobScheduleConfigResponse> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody JobScheduleConfigRequest request) {
        return ResponseEntity.ok(jobExecutionService.updateScheduleConfig(id, request));
    }

    // --- Default Lists ---

    @GetMapping("/defaults/categories")
    public ResponseEntity<List<DefaultCategoryResponse>> getDefaultCategories() {
        return ResponseEntity.ok(defaultListsService.getAllCategories());
    }

    @PostMapping("/defaults/categories")
    public ResponseEntity<DefaultCategoryResponse> createDefaultCategory(
            @Valid @RequestBody DefaultCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defaultListsService.createCategory(request));
    }

    @DeleteMapping("/defaults/categories/{id}")
    public ResponseEntity<Void> deleteDefaultCategory(@PathVariable Long id) {
        defaultListsService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/defaults/account-types")
    public ResponseEntity<List<DefaultAccountTypeResponse>> getDefaultAccountTypes() {
        return ResponseEntity.ok(defaultListsService.getAllAccountTypes());
    }

    @PostMapping("/defaults/account-types")
    public ResponseEntity<DefaultAccountTypeResponse> createDefaultAccountType(
            @Valid @RequestBody DefaultAccountTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defaultListsService.createAccountType(request));
    }

    @DeleteMapping("/defaults/account-types/{id}")
    public ResponseEntity<Void> deleteDefaultAccountType(@PathVariable Long id) {
        defaultListsService.deleteAccountType(id);
        return ResponseEntity.noContent().build();
    }

    // --- User Management ---

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers() {
        return ResponseEntity.ok(Map.of("success", true, "data", userManagementService.getAllUsers()));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of("success", true, "data", userManagementService.getUserById(id)));
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody CreateUserRequest request) {
        AdminUserResponse user = userManagementService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "data", user, "message", "User created successfully"));
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleUserActive(@PathVariable Long id, Authentication authentication) {
        AdminUserResponse user = userManagementService.toggleUserActive(id, authentication.getName());
        return ResponseEntity.ok(Map.of("success", true, "data", user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        userManagementService.deleteUser(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        userManagementService.resetPassword(id, request);
        return ResponseEntity.ok(Map.of("success", true, "message", "Password reset successfully"));
    }

    // --- System Settings ---

    @GetMapping("/settings/{key}")
    public ResponseEntity<Map<String, Object>> getSetting(@PathVariable String key) {
        return ResponseEntity.ok(Map.of("success", true, "data", systemSettingService.getSetting(key)));
    }

    @PutMapping("/settings/{key}")
    public ResponseEntity<Map<String, Object>> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateSettingRequest request) {
        return ResponseEntity.ok(Map.of("success", true, "data", systemSettingService.updateSetting(key, request.value())));
    }
}
