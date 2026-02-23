package com.minted.api.common.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JPA AttributeConverter that encrypts/decrypts sensitive fields (e.g., API keys)
 * using AES-256-GCM with a random IV per encryption.
 *
 * Stored format: Base64( IV_12_bytes || ciphertext || GCM_tag )
 */
@Slf4j
@Component
@Converter
public class ApiKeyEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey secretKey;

    public ApiKeyEncryptor(@Value("${app.encryption.key:${MINTED_JASYPT_PASSWORD:default-jasypt-password}}") String keySource) {
        this.secretKey = deriveKey(keySource);
    }

    /**
     * Derives a 256-bit AES key from the provided password using SHA-256.
     */
    private SecretKey deriveKey(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to derive encryption key", e);
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(attribute.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Prepend IV to ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);

            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            log.error("Failed to encrypt API key", e);
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(dbData);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);

            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            // If decryption fails, the value might be a legacy plain-text key.
            // Return as-is to avoid breaking existing data. It will be re-encrypted on next save.
            log.warn("Failed to decrypt value — may be legacy plain-text. Returning as-is.");
            return dbData;
        }
    }
}
