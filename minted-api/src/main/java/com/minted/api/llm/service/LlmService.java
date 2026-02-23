package com.minted.api.llm.service;

import com.minted.api.statement.dto.ParsedTransactionRow;
import com.minted.api.llm.entity.MerchantCategoryMapping;

import java.util.List;

public interface LlmService {

    String getProviderName();

    List<ParsedTransactionRow> parseStatement(String extractedText, Long userId, Long statementId,
                                               List<MerchantCategoryMapping> merchantMappings, String apiKey, String modelKey);
}
