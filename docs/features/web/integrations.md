# Integrations Frontend Spec

This document details the frontend implementation for third-party integrations (specifically Splitwise).

## 1. Integrations Module
Provides a dedicated view for managing external connections.

**Route:** `/integrations`

### Components
- **`IntegrationsPage`**: The main dashboard displaying connection status, the 'Connect to Splitwise' OAuth initiator button, and the friend-linking data table.
- **`SplitwiseCallback`**: A simple headless route loaded by the OAuth popup. It takes the `?code=` query parameter and posts a message back to the parent `window.opener` before self-closing.

### Services
- **`SplitwiseService`**: Makes HTTP calls to backend endpoints (`/api/v1/integrations/splitwise/*`) for fetching OAuth URLs, sending callback codes, mapping friends, and bulk pushing splits.

## 2. Splits Page Enhancements
The `SplitsModule` has been augmented to support direct integration with Splitwise.
- **`checkSplitwiseStatus`**: Calls out to the backend upon load to verify connection status.
- **`SplitActionsCellRendererComponent`**: Added a new `onPushToSplitwise` click event via an extra button (`pi pi-link`), injected conditionally if `isSplitwiseConnected` is true.
- **Bulk Push**: Added a "Push Selected" button to the splits grid toolbar and enabled AG grid multiple-row selection allowing users to select several splits and force batch-push them.

## 3. Server Settings Component (`admin`)
The Server Settings view has been updated to add an Integrations Configuration card.
Admins can toggle the feature enablement (`SPLITWISE_ENABLED`) and edit the respective Client ID/Secret parameters safely.
