package com.minted.api.statement.enums;

public enum StatementStatus {
    UPLOADED,
    TEXT_EXTRACTED,
    SENT_FOR_AI_PARSING,
    LLM_PARSED,
    CONFIRMING,
    COMPLETED,
    FAILED
}
