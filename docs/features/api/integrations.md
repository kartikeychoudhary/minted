# Integrations API Spec

This document details the API endpoints and resources associated with third-party software integrations, specifically focused on **Splitwise**.

## Integration Configuration

Integrations are globally enabled or disabled by system administrators using the `Global Settings` endpoints (detailed in `admin.md`).

For Splitwise, the following system settings govern the OAuth configuration:
- `SPLITWISE_ENABLED` (boolean)
- `SPLITWISE_CLIENT_ID` (string)
- `SPLITWISE_CLIENT_SECRET` (string)
- `SPLITWISE_REDIRECT_URI` (string)

## Endpoints

### 1. Get Integration Status
```http
GET /api/v1/integrations/splitwise/status
```
Returns the current connection status for the authenticated user and if the integration is enabled globally.

**Response (200 OK):**
```json
{
  "integrationName": "SPLITWISE",
  "connected": true,
  "enabled": true
}
```

### 2. Get Splitwise Authorization URL
```http
GET /api/v1/integrations/splitwise/auth-url
```
Returns the OAuth 2.0 authorization URL that the client application should redirect or open in a popup for the user to grant permission.

**Response (200 OK):**
```json
{
  "authorizationUrl": "https://secure.splitwise.com/oauth/authorize?response_type=code&client_id=..."
}
```

### 3. Handle OAuth Callback
```http
POST /api/v1/integrations/splitwise/callback
```
Handles the OAuth 2.0 callback code and automatically retrieves and encrypts the access token in the `user_integrations` table.

**Request:**
```json
{
  "code": "auth_code_from_splitwise"
}
```

**Response (200 OK):**
Returns a generic success response message when successful.

### 4. Disconnect Splitwise
```http
POST /api/v1/integrations/splitwise/disconnect
```
Deletes the user's connection reference and linked friend records from `user_integrations` and `friend_splitwise_links`.

**Response (200 OK):**
Returns a generic success message upon disconnection.

### 5. Push Single Split to Splitwise
```http
POST /api/v1/integrations/splitwise/push/{splitId}
```
Pushes an existing Minted Split transaction out to the target user's Splitwise account as an Expense. Re-pushes can be forced using a boolean query parameter.

**Query Parameters:**
- `forcePush` (boolean, optional, default=false): If `true`, pushes the expense even if it's already marked as pushed according to the `split_splitwise_pushes` table.

**Response (200 OK):**
```json
{
  "success": true,
  "alreadyPushed": false,
  "splitwiseExpenseId": "123456",
  "errorMessage": null
}
```

### 6. Bulk Push Splits to Splitwise
```http
POST /api/v1/integrations/splitwise/push/bulk
```
Pushes an array of Split IDs to Splitwise iteratively, returning a summary count of successes, failures, and skipped duplicates.

**Request:**
```json
{
  "splitIds": [1, 2, 3],
  "forcePush": false
}
```

**Response (200 OK):**
```json
{
  "successCount": 2,
  "failedCount": 0,
  "skippedCount": 1
}
```

### 7. Manage Splitwise Friends
The integration controller also contains standard endpoints to link (`POST /api/v1/integrations/splitwise/friends/{friendId}/link`), unlink (`DELETE`), and retrieve Splitwise friends mappings.

## Database Entities

- `UserIntegration`: Stores OAuth access tokens per user and provider. Tokens are encrypted using Jasypt.
- `FriendSplitwiseLink`: Links a local Minted Friend ID to an external Splitwise Friend ID.
- `SplitSplitwisePush`: Records which Minted Splits have already been successfully pushed to Splitwise, storing the external Expense ID to prevent unwanted duplicate transactions.
