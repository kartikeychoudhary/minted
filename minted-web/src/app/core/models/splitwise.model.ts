export interface IntegrationStatusResponse {
    provider: string;
    enabled: boolean;
    connected: boolean;
    connectedUserName?: string;
    connectedUserEmail?: string;
    connectedAt?: string;
}

export interface SplitwiseAdminConfigResponse {
    enabled: boolean;
    clientIdConfigured: boolean;
    clientSecretConfigured: boolean;
    redirectUri: string;
}

export interface SplitwiseAuthUrlResponse {
    authorizationUrl: string;
}

export interface SplitwiseFriend {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
    displayName: string;
    avatarUrl: string;
    linkedMintedFriendId?: number;
}

export interface FriendLinkResponse {
    friendId: number;
    friendName: string;
    splitwiseFriendId: number;
    splitwiseFriendName: string;
    splitwiseFriendEmail: string;
    linkedAt: string;
}

export interface PushResult {
    splitTransactionId: number;
    description: string;
    success: boolean;
    alreadyPushed: boolean;
    splitwiseExpenseId?: number;
    errorMessage?: string;
}

export interface BulkPushResponse {
    totalRequested: number;
    successCount: number;
    skippedCount: number;
    failedCount: number;
    results: PushResult[];
}
