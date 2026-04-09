package com.minted.api.integration.dto;

import java.util.List;

public record BulkPushRequest(List<Long> splitTransactionIds, boolean forcePush) {}
