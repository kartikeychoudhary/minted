package com.minted.api.dto;

import java.util.List;

public record StatementParseResult(
        Long statementId,
        List<ParsedTransactionRow> rows
) {}
