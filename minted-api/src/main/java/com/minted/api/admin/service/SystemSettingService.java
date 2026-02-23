package com.minted.api.admin.service;

import com.minted.api.admin.dto.SystemSettingResponse;

public interface SystemSettingService {
    String getValue(String key);
    SystemSettingResponse getSetting(String key);
    SystemSettingResponse updateSetting(String key, String value);
    boolean isSignupEnabled();
}
