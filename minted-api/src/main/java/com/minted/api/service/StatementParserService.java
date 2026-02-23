package com.minted.api.service;

import java.io.IOException;

public interface StatementParserService {

    String extractText(byte[] pdfBytes, String password) throws IOException;
}
