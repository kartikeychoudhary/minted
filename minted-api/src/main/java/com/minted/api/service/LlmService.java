package com.minted.api.service;

import com.minted.api.dto.ParsedTransactionRow;
import com.minted.api.entity.MerchantCategoryMapping;

import java.util.List;

public interface LlmService {

    String getProviderName();

    List<ParsedTransactionRow> parseStatement(String extractedText, Long userId, Long statementId,
                                               List<MerchantCategoryMapping> merchantMappings, String apiKey, String modelKey);
}
