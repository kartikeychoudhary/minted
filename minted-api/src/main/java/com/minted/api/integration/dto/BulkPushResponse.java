package com.minted.api.integration.dto;

import java.util.List;

public record BulkPushResponse(
        int totalRequested,
        int successCount,
        int skippedCount,
        int failedCount,
        List<PushResult> results
) {}
