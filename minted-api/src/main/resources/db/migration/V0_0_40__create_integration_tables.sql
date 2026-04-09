-- Integration tables for 3rd-party service connections

-- Stores per-user OAuth tokens for external integrations (e.g. Splitwise)
CREATE TABLE user_integrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    access_token TEXT,
    token_type VARCHAR(20) DEFAULT 'bearer',
    expires_at TIMESTAMP NULL,
    splitwise_user_id BIGINT,
    splitwise_user_name VARCHAR(200),
    splitwise_user_email VARCHAR(255),
    connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_provider (user_id, provider),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_integrations_user_provider ON user_integrations(user_id, provider);

-- Links a Minted friend to their Splitwise contact ID
CREATE TABLE friend_splitwise_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    friend_id BIGINT NOT NULL UNIQUE,
    splitwise_friend_id BIGINT NOT NULL,
    splitwise_friend_name VARCHAR(200),
    splitwise_friend_email VARCHAR(255),
    linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (friend_id) REFERENCES friends(id) ON DELETE CASCADE
);

CREATE INDEX idx_friend_splitwise_links_friend_id ON friend_splitwise_links(friend_id);

-- Tracks which split transactions have been pushed to Splitwise (prevents duplicates)
CREATE TABLE split_splitwise_pushes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    split_transaction_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    splitwise_expense_id BIGINT NOT NULL,
    pushed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_split_user (split_transaction_id, user_id),
    FOREIGN KEY (split_transaction_id) REFERENCES split_transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_split_splitwise_pushes_split_id ON split_splitwise_pushes(split_transaction_id);
CREATE INDEX idx_split_splitwise_pushes_user_id ON split_splitwise_pushes(user_id);

-- Seed default Splitwise system settings (admin must fill in their values)
INSERT INTO system_settings (setting_key, setting_value, description)
VALUES
    ('SPLITWISE_ENABLED', 'false', 'Enable Splitwise integration for all users'),
    ('SPLITWISE_CLIENT_ID', '', 'Splitwise OAuth2 Client ID from developer.splitwise.com'),
    ('SPLITWISE_CLIENT_SECRET', '', 'Splitwise OAuth2 Client Secret from developer.splitwise.com'),
    ('SPLITWISE_REDIRECT_URI', 'http://localhost:4200/integrations/splitwise/callback', 'OAuth2 callback URL — must match what is registered in Splitwise developer portal');
