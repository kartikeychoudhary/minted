package com.minted.api.service;

import com.minted.api.dto.SystemSettingResponse;

public interface SystemSettingService {
    String getValue(String key);
    SystemSettingResponse getSetting(String key);
    SystemSettingResponse updateSetting(String key, String value);
    boolean isSignupEnabled();
}
