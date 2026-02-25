package com.minted.api.llm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minted.api.statement.dto.ParsedTransactionRow;
import com.minted.api.llm.entity.MerchantCategoryMapping;
import com.minted.api.common.exception.BadRequestException;
import com.minted.api.llm.service.LlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiLlmService implements LlmService {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={apiKey}";

    private final ObjectMapper objectMapper;

    @Override
    public String getProviderName() {
        return "GEMINI";
    }

    @Override
    public List<ParsedTransactionRow> parseStatement(String extractedText, Long userId, Long statementId,
                                                      List<MerchantCategoryMapping> merchantMappings,
                                                      List<String> availableCategories,
                                                      String apiKey, String modelKey) {
        String prompt = buildPrompt(extractedText, merchantMappings, availableCategories);

        String url = GEMINI_API_URL
                .replace("{model}", modelKey)
                .replace("{apiKey}", apiKey);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            RestClient restClient = RestClient.create();
            String responseBody = restClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.debug("Gemini raw response for statement {}: {}", statementId, responseBody);

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty() || candidates.get(0).path("content").path("parts").isEmpty()) {
                throw new BadRequestException("LLM returned empty response. Please try again.");
            }

            String llmText = candidates.get(0).path("content").path("parts").get(0).path("text").asText();

            // Clean up response - remove markdown code blocks if present
            llmText = llmText.trim();
            if (llmText.startsWith("```json")) {
                llmText = llmText.substring(7);
            } else if (llmText.startsWith("```")) {
                llmText = llmText.substring(3);
            }
            if (llmText.endsWith("```")) {
                llmText = llmText.substring(0, llmText.length() - 3);
            }
            llmText = llmText.trim();

            List<ParsedTransactionRow> rows = objectMapper.readValue(llmText, new TypeReference<>() {});

            // Assign tempIds and ensure amounts are positive
            for (ParsedTransactionRow row : rows) {
                row.setTempId(UUID.randomUUID().toString());
                if (row.getAmount() != null && row.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    row.setAmount(row.getAmount().abs());
                }
                if (row.getNotes() == null) {
                    row.setNotes("");
                }
                if (row.getTags() == null) {
                    row.setTags("");
                }
            }

            return rows;

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse LLM response for statement {}: {}", statementId, e.getMessage(), e);
            throw new BadRequestException("LLM returned unparseable response. Please try again. Error: " + e.getMessage());
        }
    }

    String buildPrompt(String extractedText, List<MerchantCategoryMapping> merchantMappings, List<String> availableCategories) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a financial data extraction expert. Extract all transactions from the following credit card statement text.\n\n");
        sb.append("Return ONLY a valid JSON array with no markdown, no code blocks, no explanation.\n");
        sb.append("Each object in the array must have EXACTLY these fields:\n");
        sb.append("- \"amount\": number (positive value, never negative)\n");
        sb.append("- \"type\": \"EXPENSE\" or \"INCOME\" (credits/refunds = INCOME, purchases = EXPENSE)\n");
        sb.append("- \"description\": string (merchant name / transaction description, max 200 chars)\n");
        sb.append("- \"transactionDate\": string in \"YYYY-MM-DD\" format\n");
        sb.append("- \"categoryName\": string — MUST be one of the AVAILABLE CATEGORIES listed below\n");
        sb.append("- \"notes\": string (any extra info like reference number, empty string if none)\n\n");

        if (availableCategories != null && !availableCategories.isEmpty()) {
            sb.append("=== AVAILABLE CATEGORIES (you MUST pick from this list, do NOT invent new categories) ===\n");
            sb.append(String.join(", ", availableCategories));
            sb.append("\n=== END AVAILABLE CATEGORIES ===\n\n");
        }

        if (merchantMappings != null && !merchantMappings.isEmpty()) {
            sb.append("=== MERCHANT CATEGORY HINTS (treat these as ABSOLUTE RULES, highest priority) ===\n");
            sb.append(buildMerchantHintsBlock(merchantMappings));
            sb.append("For all other merchants not listed above, pick the best matching category from the AVAILABLE CATEGORIES list.\n");
            sb.append("=== END HINTS ===\n\n");
        }

        sb.append("Do NOT include:\n");
        sb.append("- Opening balance, closing balance, minimum payment, payment due rows\n");
        sb.append("- Any row that is a fee summary or statement header\n\n");
        sb.append("Statement text:\n---\n");
        sb.append(extractedText);
        sb.append("\n---\n\n");
        sb.append("Return ONLY the JSON array. Start your response with [ and end with ].");

        return sb.toString();
    }

    String buildMerchantHintsBlock(List<MerchantCategoryMapping> mappings) {
        StringBuilder sb = new StringBuilder();
        for (MerchantCategoryMapping mapping : mappings) {
            String snippetPart = mapping.getSnippetList().stream()
                    .map(s -> "'" + s.toUpperCase() + "'")
                    .collect(Collectors.joining(" or "));
            sb.append("- If description contains ").append(snippetPart)
                    .append(" → categoryName must be '").append(mapping.getCategory().getName()).append("'\n");
        }
        return sb.toString();
    }
}
