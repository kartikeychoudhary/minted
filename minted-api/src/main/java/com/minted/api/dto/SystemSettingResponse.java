package com.minted.api.dto;

import com.minted.api.entity.SystemSetting;

public record SystemSettingResponse(
        Long id,
        String settingKey,
        String settingValue,
        String description
) {
    public static SystemSettingResponse from(SystemSetting s) {
        return new SystemSettingResponse(
                s.getId(),
                s.getSettingKey(),
                s.getSettingValue(),
                s.getDescription()
        );
    }
}
