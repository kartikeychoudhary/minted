package com.minted.api.admin.service;

import com.minted.api.admin.dto.SystemSettingResponse;
import com.minted.api.admin.entity.SystemSetting;
import com.minted.api.common.exception.ResourceNotFoundException;
import com.minted.api.admin.repository.SystemSettingRepository;
import com.minted.api.admin.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository settingRepository;

    @Override
    @Transactional(readOnly = true)
    public String getValue(String key) {
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key));
        return setting.getSettingValue();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemSettingResponse getSetting(String key) {
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key));
        return SystemSettingResponse.from(setting);
    }

    @Override
    @Transactional
    public SystemSettingResponse updateSetting(String key, String value) {
        SystemSetting setting = settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key));
        setting.setSettingValue(value);
        SystemSetting saved = settingRepository.save(setting);
        return SystemSettingResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSignupEnabled() {
        return "true".equalsIgnoreCase(getValue("SIGNUP_ENABLED"));
    }
}
