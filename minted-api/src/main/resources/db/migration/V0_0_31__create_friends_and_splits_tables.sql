-- Create friends table
CREATE TABLE friends (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    avatar_color VARCHAR(7) DEFAULT '#6366f1',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_friend_name (user_id, name)
);

CREATE INDEX idx_friends_user_id ON friends(user_id);
CREATE INDEX idx_friends_is_active ON friends(is_active);

-- Create split_transactions table
CREATE TABLE split_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source_transaction_id BIGINT,
    description VARCHAR(500) NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    split_type VARCHAR(20) NOT NULL,
    transaction_date DATE NOT NULL,
    is_settled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (source_transaction_id) REFERENCES transactions(id) ON DELETE SET NULL
);

CREATE INDEX idx_split_transactions_user_id ON split_transactions(user_id);
CREATE INDEX idx_split_transactions_is_settled ON split_transactions(is_settled);
CREATE INDEX idx_split_transactions_date ON split_transactions(transaction_date);

-- Create split_shares table
CREATE TABLE split_shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    split_transaction_id BIGINT NOT NULL,
    friend_id BIGINT,
    share_amount DECIMAL(15,2) NOT NULL,
    share_percentage DECIMAL(5,2),
    is_payer BOOLEAN DEFAULT FALSE,
    is_settled BOOLEAN DEFAULT FALSE,
    settled_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (split_transaction_id) REFERENCES split_transactions(id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES friends(id) ON DELETE SET NULL
);

CREATE INDEX idx_split_shares_split_transaction_id ON split_shares(split_transaction_id);
CREATE INDEX idx_split_shares_friend_id ON split_shares(friend_id);
