-- Create transaction categories table
CREATE TABLE transaction_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type ENUM('INCOME', 'EXPENSE', 'TRANSFER') NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(7),
    parent_id BIGINT,
    user_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES transaction_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_category_name_type (user_id, name, type)
);

-- Create indexes for faster lookups
CREATE INDEX idx_transaction_categories_user_id ON transaction_categories(user_id);
CREATE INDEX idx_transaction_categories_type ON transaction_categories(type);
CREATE INDEX idx_transaction_categories_parent_id ON transaction_categories(parent_id);
