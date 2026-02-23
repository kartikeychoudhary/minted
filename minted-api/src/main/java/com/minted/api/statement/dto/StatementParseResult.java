package com.minted.api.statement.dto;

import java.util.List;

public record StatementParseResult(
        Long statementId,
        List<ParsedTransactionRow> rows
) {}
