package com.minted.api.integration.splitwise;

/**
 * Thrown when the Splitwise API returns an error or is unreachable.
 * The message is always user-friendly and safe to surface in API responses.
 */
public class SplitwiseApiException extends RuntimeException {

    public SplitwiseApiException(String message) {
        super(message);
    }

    public SplitwiseApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
