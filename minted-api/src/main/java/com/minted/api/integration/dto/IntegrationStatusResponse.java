package com.minted.api.integration.dto;

import java.time.LocalDateTime;

public record IntegrationStatusResponse(
        String provider,
        boolean enabled,
        boolean connected,
        String connectedUserName,
        String connectedUserEmail,
        LocalDateTime connectedAt
) {}
