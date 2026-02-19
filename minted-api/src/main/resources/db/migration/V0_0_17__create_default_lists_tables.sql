-- ============================================================
-- Default Categories & Account Types (admin-managed global lists)
-- ============================================================

-- Default categories suggested to users when creating their own
CREATE TABLE default_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    icon VARCHAR(50) NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'EXPENSE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Default account types suggested to users
CREATE TABLE default_account_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed default categories
INSERT INTO default_categories (name, icon, type) VALUES
    ('Food & Dining', 'pi pi-shopping-cart', 'EXPENSE'),
    ('Transportation', 'pi pi-car', 'EXPENSE'),
    ('Housing & Rent', 'pi pi-home', 'EXPENSE'),
    ('Utilities', 'pi pi-bolt', 'EXPENSE'),
    ('Healthcare', 'pi pi-heart', 'EXPENSE'),
    ('Entertainment', 'pi pi-play', 'EXPENSE'),
    ('Shopping', 'pi pi-shopping-bag', 'EXPENSE'),
    ('Education', 'pi pi-book', 'EXPENSE'),
    ('Personal Care', 'pi pi-user', 'EXPENSE'),
    ('Insurance', 'pi pi-shield', 'EXPENSE'),
    ('Subscriptions', 'pi pi-sync', 'EXPENSE'),
    ('Gifts & Donations', 'pi pi-gift', 'EXPENSE'),
    ('Travel', 'pi pi-map', 'EXPENSE'),
    ('Groceries', 'pi pi-apple', 'EXPENSE'),
    ('Salary', 'pi pi-wallet', 'INCOME'),
    ('Freelance', 'pi pi-briefcase', 'INCOME'),
    ('Investments', 'pi pi-chart-line', 'INCOME'),
    ('Refunds', 'pi pi-replay', 'INCOME'),
    ('Other Income', 'pi pi-money-bill', 'INCOME');

-- Seed default account types
INSERT INTO default_account_types (name) VALUES
    ('Savings Account'),
    ('Current Account'),
    ('Credit Card'),
    ('Wallet'),
    ('Cash'),
    ('Fixed Deposit'),
    ('Loan Account'),
    ('Investment Account');
