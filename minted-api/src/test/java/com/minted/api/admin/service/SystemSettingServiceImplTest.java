package com.minted.api.admin.service;

import com.minted.api.admin.dto.SystemSettingResponse;
import com.minted.api.admin.entity.SystemSetting;
import com.minted.api.admin.repository.SystemSettingRepository;
import com.minted.api.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemSettingServiceImplTest {

    @Mock private SystemSettingRepository settingRepository;

    @InjectMocks
    private SystemSettingServiceImpl systemSettingService;

    // ── getValue ──────────────────────────────────────────────────────────────

    @Test
    void getValue_found_returnsValue() {
        SystemSetting setting = buildSetting("SIGNUP_ENABLED", "true");
        when(settingRepository.findBySettingKey("SIGNUP_ENABLED")).thenReturn(Optional.of(setting));

        String value = systemSettingService.getValue("SIGNUP_ENABLED");

        assertThat(value).isEqualTo("true");
    }

    @Test
    void getValue_notFound_throwsResourceNotFound() {
        when(settingRepository.findBySettingKey("MISSING_KEY")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemSettingService.getValue("MISSING_KEY"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("MISSING_KEY");
    }

    // ── getSetting ────────────────────────────────────────────────────────────

    @Test
    void getSetting_found_returnsResponse() {
        SystemSetting setting = buildSetting("SIGNUP_ENABLED", "false");
        when(settingRepository.findBySettingKey("SIGNUP_ENABLED")).thenReturn(Optional.of(setting));

        SystemSettingResponse response = systemSettingService.getSetting("SIGNUP_ENABLED");

        assertThat(response.settingKey()).isEqualTo("SIGNUP_ENABLED");
        assertThat(response.settingValue()).isEqualTo("false");
    }

    @Test
    void getSetting_notFound_throwsResourceNotFound() {
        when(settingRepository.findBySettingKey("NO_KEY")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemSettingService.getSetting("NO_KEY"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateSetting ─────────────────────────────────────────────────────────

    @Test
    void updateSetting_success() {
        SystemSetting setting = buildSetting("SIGNUP_ENABLED", "false");
        when(settingRepository.findBySettingKey("SIGNUP_ENABLED")).thenReturn(Optional.of(setting));
        when(settingRepository.save(setting)).thenReturn(setting);

        SystemSettingResponse response = systemSettingService.updateSetting("SIGNUP_ENABLED", "true");

        assertThat(setting.getSettingValue()).isEqualTo("true");
        assertThat(response.settingValue()).isEqualTo("true");
        verify(settingRepository).save(setting);
    }

    @Test
    void updateSetting_notFound_throwsResourceNotFound() {
        when(settingRepository.findBySettingKey("NO_KEY")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemSettingService.updateSetting("NO_KEY", "val"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── isSignupEnabled ───────────────────────────────────────────────────────

    @Test
    void isSignupEnabled_valueTrue_returnsTrue() {
        when(settingRepository.findBySettingKey("SIGNUP_ENABLED"))
                .thenReturn(Optional.of(buildSetting("SIGNUP_ENABLED", "true")));

        assertThat(systemSettingService.isSignupEnabled()).isTrue();
    }

    @Test
    void isSignupEnabled_valueFalse_returnsFalse() {
        when(settingRepository.findBySettingKey("SIGNUP_ENABLED"))
                .thenReturn(Optional.of(buildSetting("SIGNUP_ENABLED", "false")));

        assertThat(systemSettingService.isSignupEnabled()).isFalse();
    }

    @Test
    void isSignupEnabled_valueTrueUpperCase_returnsTrue() {
        // equalsIgnoreCase should handle "TRUE"
        when(settingRepository.findBySettingKey("SIGNUP_ENABLED"))
                .thenReturn(Optional.of(buildSetting("SIGNUP_ENABLED", "TRUE")));

        assertThat(systemSettingService.isSignupEnabled()).isTrue();
    }

    @Test
    void isSignupEnabled_settingMissing_throwsResourceNotFound() {
        when(settingRepository.findBySettingKey("SIGNUP_ENABLED")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> systemSettingService.isSignupEnabled())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private SystemSetting buildSetting(String key, String value) {
        SystemSetting s = new SystemSetting();
        s.setId(1L);
        s.setSettingKey(key);
        s.setSettingValue(value);
        s.setDescription("Test setting");
        return s;
    }
}
