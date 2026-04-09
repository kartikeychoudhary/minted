package com.minted.api.integration.splitwise;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.minted.api.integration.dto.SplitwiseFriendDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thin HTTP client wrapper around the Splitwise REST API v3.
 * All methods throw {@link SplitwiseApiException} on API or network errors.
 */
@Slf4j
@Component
public class SplitwiseApiClient {

    private static final String BASE_URL = "https://secure.splitwise.com/api/v3.0";
    private static final String TOKEN_URL = "https://secure.splitwise.com/oauth/token";
    private static final String AUTH_BASE_URL = "https://secure.splitwise.com/oauth/authorize";

    private final RestClient restClient;

    public SplitwiseApiClient() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ── OAuth ──────────────────────────────────────────────────────────────────

    public String buildAuthorizationUrl(String clientId, String redirectUri, String state) {
        return AUTH_BASE_URL +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&state=" + state;
    }

    public SplitwiseTokenResponse exchangeCodeForToken(
            String code, String clientId, String clientSecret, String redirectUri) {
        try {
            RestClient tokenClient = RestClient.builder()
                    .baseUrl(TOKEN_URL)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("redirect_uri", redirectUri);
            body.add("code", code);

            return tokenClient.post()
                    .body(body)
                    .retrieve()
                    .body(SplitwiseTokenResponse.class);
        } catch (HttpClientErrorException e) {
            log.error("Splitwise token exchange failed: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new SplitwiseApiException("Splitwise rejected the authorization code. It may be expired or invalid. Please try connecting again.", e);
        } catch (Exception e) {
            log.error("Splitwise token exchange error", e);
            throw new SplitwiseApiException("Could not connect to Splitwise servers during authorization. Check your network connection.", e);
        }
    }

    // ── User ──────────────────────────────────────────────────────────────────

    public SplitwiseUser getCurrentUser(String accessToken) {
        try {
            SplitwiseCurrentUserResponse resp = restClient.get()
                    .uri("/get_current_user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(SplitwiseCurrentUserResponse.class);
            if (resp == null || resp.user() == null) {
                throw new SplitwiseApiException("Splitwise returned an empty user response.");
            }
            return resp.user();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new SplitwiseApiException("Your Splitwise session has expired. Please reconnect your account.", e);
        } catch (HttpClientErrorException e) {
            throw new SplitwiseApiException("Splitwise API error: " + extractErrorMessage(e), e);
        } catch (SplitwiseApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get Splitwise current user", e);
            throw new SplitwiseApiException("Could not reach Splitwise servers. Check your network connection.", e);
        }
    }

    // ── Friends ───────────────────────────────────────────────────────────────

    public List<SplitwiseFriend> getFriends(String accessToken) {
        try {
            SplitwiseFriendsResponse resp = restClient.get()
                    .uri("/get_friends")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(SplitwiseFriendsResponse.class);
            return resp != null && resp.friends() != null ? resp.friends() : new ArrayList<>();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new SplitwiseApiException("Your Splitwise session has expired. Please reconnect your account.", e);
        } catch (HttpClientErrorException e) {
            throw new SplitwiseApiException("Splitwise API error while fetching friends: " + extractErrorMessage(e), e);
        } catch (SplitwiseApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get Splitwise friends", e);
            throw new SplitwiseApiException("Could not reach Splitwise servers. Check your network connection.", e);
        }
    }

    // ── Expenses ──────────────────────────────────────────────────────────────

    public SplitwiseExpense createExpense(String accessToken, CreateExpenseRequest request) {
        try {
            Map<String, Object> body = buildExpenseBody(request);

            SplitwiseExpenseResponse resp = restClient.post()
                    .uri("/create_expense")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .body(body)
                    .retrieve()
                    .body(SplitwiseExpenseResponse.class);

            if (resp == null || resp.expense() == null) {
                throw new SplitwiseApiException("Splitwise returned an empty response when creating the expense.");
            }
            if (resp.errors() != null && !resp.errors().isEmpty()) {
                throw new SplitwiseApiException("Splitwise rejected the expense: " + resp.errors());
            }
            return resp.expense();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new SplitwiseApiException("Your Splitwise session has expired. Please reconnect your account.", e);
        } catch (HttpClientErrorException e) {
            throw new SplitwiseApiException("Splitwise rejected the expense: " + extractErrorMessage(e), e);
        } catch (HttpServerErrorException e) {
            throw new SplitwiseApiException("Splitwise server error. Please try again later.", e);
        } catch (SplitwiseApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create Splitwise expense", e);
            throw new SplitwiseApiException("Could not reach Splitwise servers. Check your network connection.", e);
        }
    }

    private Map<String, Object> buildExpenseBody(CreateExpenseRequest req) {
        Map<String, Object> body = new HashMap<>();
        body.put("cost", req.totalAmount().toPlainString());
        body.put("description", req.description());
        body.put("category_id", 18); // General category
        body.put("date", req.date().toString());
        body.put("split_equally", false);

        // Users array: index 0 = current user (payer), rest = friends
        for (int i = 0; i < req.users().size(); i++) {
            ExpenseUser u = req.users().get(i);
            body.put("users__" + i + "__user_id", u.userId());
            body.put("users__" + i + "__paid_share", u.paidShare().toPlainString());
            body.put("users__" + i + "__owed_share", u.owedShare().toPlainString());
        }
        return body;
    }

    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            String body = e.getResponseBodyAsString();
            if (body.contains("\"errors\"")) {
                return body;
            }
        } catch (Exception ignored) {}
        return e.getStatusCode() + " " + e.getStatusText();
    }

    // ── Inner DTOs ────────────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SplitwiseTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn,
            @JsonProperty("refresh_token") String refreshToken
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SplitwiseCurrentUserResponse(SplitwiseUser user) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SplitwiseFriendsResponse(List<SplitwiseFriend> friends) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SplitwiseUser(
            Long id,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            String email
    ) {
        public String displayName() {
            return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SplitwiseFriend(
            Long id,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            String email,
            @JsonProperty("picture") SplitwisePicture picture
    ) {
        public String displayName() {
            return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        }

        public SplitwiseFriendDto toDto(Long linkedMintedFriendId) {
            return new SplitwiseFriendDto(id, firstName, lastName, email, displayName(),
                    picture != null ? picture.small() : null, linkedMintedFriendId);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SplitwisePicture(String small, String medium, String large) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SplitwiseExpenseResponse(
            SplitwiseExpense expense,
            Map<String, Object> errors
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SplitwiseExpense(
            Long id,
            String description,
            String cost
    ) {}

    // ── Request Builder ───────────────────────────────────────────────────────

    public record CreateExpenseRequest(
            String description,
            BigDecimal totalAmount,
            LocalDate date,
            List<ExpenseUser> users
    ) {}

    public record ExpenseUser(Long userId, BigDecimal paidShare, BigDecimal owedShare) {}
}
