package com.minted.api.dashboardconfig.service;

import com.minted.api.dashboardconfig.dto.DashboardConfigRequest;
import com.minted.api.dashboardconfig.dto.DashboardConfigResponse;
import com.minted.api.dashboardconfig.entity.DashboardConfiguration;
import com.minted.api.dashboardconfig.repository.DashboardConfigurationRepository;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.user.entity.User;
import com.minted.api.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardConfigServiceImplTest {

    @Mock private DashboardConfigurationRepository repository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private DashboardConfigServiceImpl dashboardConfigService;

    // ── getConfig ─────────────────────────────────────────────────────────────

    @Test
    void getConfig_configExists_returnsResponse() {
        DashboardConfiguration config = buildConfig(1L, "2,3");
        when(repository.findByUserId(1L)).thenReturn(Optional.of(config));

        DashboardConfigResponse response = dashboardConfigService.getConfig(1L);

        assertThat(response.excludedCategoryIds()).containsExactly(2L, 3L);
    }

    @Test
    void getConfig_noConfig_returnsEmpty() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());

        DashboardConfigResponse response = dashboardConfigService.getConfig(1L);

        assertThat(response.id()).isNull();
        assertThat(response.excludedCategoryIds()).isEmpty();
    }

    // ── saveConfig ────────────────────────────────────────────────────────────

    @Test
    void saveConfig_newConfig_savesExcludedIds() {
        User user = buildUser(1L);
        DashboardConfiguration saved = buildConfig(1L, "1,2");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());
        when(repository.save(any(DashboardConfiguration.class))).thenReturn(saved);

        DashboardConfigResponse response = dashboardConfigService.saveConfig(
                new DashboardConfigRequest(List.of(1L, 2L)), 1L);

        assertThat(response.excludedCategoryIds()).containsExactly(1L, 2L);
        verify(repository).save(any(DashboardConfiguration.class));
    }

    @Test
    void saveConfig_userNotFound_throwsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardConfigService.saveConfig(
                new DashboardConfigRequest(List.of()), 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getExcludedCategoryIds ────────────────────────────────────────────────

    @Test
    void getExcludedCategoryIds_parsesCommaSeparated() {
        DashboardConfiguration config = buildConfig(1L, "5,10,15");
        when(repository.findByUserId(1L)).thenReturn(Optional.of(config));

        List<Long> result = dashboardConfigService.getExcludedCategoryIds(1L);

        assertThat(result).containsExactly(5L, 10L, 15L);
    }

    @Test
    void getExcludedCategoryIds_noConfig_returnsEmpty() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());

        List<Long> result = dashboardConfigService.getExcludedCategoryIds(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getExcludedCategoryIds_blankIds_returnsEmpty() {
        DashboardConfiguration config = buildConfig(1L, "");
        when(repository.findByUserId(1L)).thenReturn(Optional.of(config));

        List<Long> result = dashboardConfigService.getExcludedCategoryIds(1L);

        assertThat(result).isEmpty();
    }

    // helpers

    private DashboardConfiguration buildConfig(Long id, String excludedIds) {
        DashboardConfiguration c = new DashboardConfiguration();
        c.setId(id);
        c.setExcludedCategoryIds(excludedIds);
        return c;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("alice");
        return u;
    }
}
