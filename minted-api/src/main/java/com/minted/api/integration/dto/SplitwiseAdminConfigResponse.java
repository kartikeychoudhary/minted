package com.minted.api.integration.dto;

public record SplitwiseAdminConfigResponse(
        boolean enabled,
        boolean clientIdConfigured,
        boolean clientSecretConfigured,
        String redirectUri
) {}
