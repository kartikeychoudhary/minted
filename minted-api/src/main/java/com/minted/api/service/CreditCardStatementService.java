package com.minted.api.service;

import com.minted.api.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CreditCardStatementService {

    StatementResponse uploadAndExtract(MultipartFile file, Long accountId, String pdfPassword, Long userId);

    StatementResponse triggerLlmParse(Long statementId, Long userId);

    void confirmImport(ConfirmStatementRequest request, Long userId);

    List<StatementResponse> getUserStatements(Long userId);

    StatementResponse getStatementById(Long statementId, Long userId);

    List<ParsedTransactionRow> getParsedRows(Long statementId, Long userId);
}
