package com.minted.api.service.impl;

import com.minted.api.exception.BadRequestException;
import com.minted.api.service.StatementParserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class StatementParserServiceImpl implements StatementParserService {

    private static final int MAX_TEXT_LENGTH = 100_000;

    @Override
    public String extractText(byte[] pdfBytes, String password) throws IOException {
        PDDocument doc = null;
        try {
            doc = Loader.loadPDF(pdfBytes, password != null && !password.isBlank() ? password : "");
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            if (text.length() > MAX_TEXT_LENGTH) {
                text = text.substring(0, MAX_TEXT_LENGTH)
                        + "\n[Note: Statement text truncated at 100,000 characters for LLM processing]";
            }

            return text;
        } catch (org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException e) {
            throw new BadRequestException("Invalid PDF password. Please check and try again.");
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }
}
