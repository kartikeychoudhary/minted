package com.minted.api.statement.service;

import java.io.IOException;

public interface StatementParserService {

    String extractText(byte[] pdfBytes, String password) throws IOException;
}
