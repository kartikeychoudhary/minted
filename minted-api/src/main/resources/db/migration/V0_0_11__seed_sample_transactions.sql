-- Seed sample accounts for admin user (id=1)
-- Assuming account_types from V0_0_10 have IDs 1-4

INSERT INTO accounts (name, balance, currency, is_active, user_id, account_type_id) VALUES
('Chase Checking ****4422', 5420.50, 'USD', true, 1, 1),
('Chase Sapphire ****8765', -1245.75, 'USD', true, 1, 2),
('Amex Gold ****3421', -425.30, 'USD', true, 1, 2),
('Vanguard 401k ****9012', 45230.00, 'USD', true, 1, 4);

-- Seed sample transactions for admin user (id=1)
-- Assuming categories from V0_0_10:
-- ID 1: Salary (INCOME)
-- ID 2: Freelance (INCOME)
-- ID 4: Food & Dining (EXPENSE)
-- ID 5: Groceries (EXPENSE)
-- ID 6: Transport (EXPENSE)
-- ID 7: Utilities (EXPENSE)
-- ID 8: Entertainment (EXPENSE)
-- ID 9: Shopping (EXPENSE)
-- ID 10: Health (EXPENSE)
-- ID 12: Rent (EXPENSE)

-- Assuming accounts from above:
-- ID 1: Chase Checking
-- ID 2: Chase Sapphire
-- ID 3: Amex Gold
-- ID 4: Vanguard 401k

-- INCOME Transactions (February 2024)
INSERT INTO transactions (amount, type, description, notes, transaction_date, is_recurring, account_id, category_id, user_id) VALUES
(2450.00, 'INCOME', 'Gusto Payroll', 'Bi-weekly salary payment', '2024-02-15', false, 1, 1, 1),
(1200.00, 'INCOME', 'Freelance Web Design', 'Client project payment', '2024-02-10', false, 1, 2, 1),
(2450.00, 'INCOME', 'Gusto Payroll', 'Bi-weekly salary payment', '2024-02-01', false, 1, 1, 1);

-- EXPENSE Transactions - Food & Dining (February 2024)
INSERT INTO transactions (amount, type, description, notes, transaction_date, is_recurring, account_id, category_id, user_id) VALUES
(142.50, 'EXPENSE', 'Whole Foods Market', 'Weekly groceries', '2024-02-14', false, 2, 5, 1),
(45.75, 'EXPENSE', 'Chipotle Mexican Grill', 'Lunch with team', '2024-02-13', false, 2, 4, 1),
(6.45, 'EXPENSE', 'Starbucks Coffee', 'Morning coffee', '2024-02-12', false, 3, 4, 1),
(78.90, 'EXPENSE', 'Olive Garden', 'Dinner with family', '2024-02-11', false, 2, 4, 1);

-- EXPENSE Transactions - Transport (February 2024)
INSERT INTO transactions (amount, type, description, notes, transaction_date, is_recurring, account_id, category_id, user_id) VALUES
(24.15, 'EXPENSE', 'Uber Ride', 'Ride to office', '2024-02-09', false, 3, 6, 1),
(65.00, 'EXPENSE', 'Shell Gas Station', 'Fuel for car', '2024-02-08', false, 2, 6, 1);

-- EXPENSE Transactions - Utilities (February 2024)
INSERT INTO transactions (amount, type, description, notes, transaction_date, is_recurring, account_id, category_id, user_id) VALUES
(85.20, 'EXPENSE', 'PG&E Utilities', 'Monthly electricity bill', '2024-02-11', true, 1, 7, 1),
(120.00, 'EXPENSE', 'AT&T Internet', 'Monthly internet service', '2024-02-05', true, 1, 7, 1);

-- EXPENSE Transactions - Entertainment (February 2024)
INSERT INTO transactions (amount, type, description, notes, transaction_date, is_recurring, account_id, category_id, user_id) VALUES
(15.99, 'EXPENSE', 'Netflix Subscription', 'Monthly streaming', '2024-02-10', true, 2, 8, 1),
(45.00, 'EXPENSE', 'AMC Movie Theatre', 'Movie tickets and snacks', '2024-02-07', false, 3, 8, 1);

-- EXPENSE Transactions - Shopping (February 2024)
INSERT INTO transactions (amount, type, description, notes, transaction_date, is_recurring, account_id, category_id, user_id) VALUES
(89.99, 'EXPENSE', 'Amazon.com', 'Home supplies', '2024-02-13', false, 2, 9, 1),
(234.50, 'EXPENSE', 'Target', 'Clothes and household items', '2024-02-06', false, 2, 9, 1);

-- EXPENSE Transactions - Rent (February 2024)
INSERT INTO transactions (amount, type, description, notes, transaction_date, is_recurring, account_id, category_id, user_id) VALUES
(1850.00, 'EXPENSE', 'Monthly Rent Payment', 'February rent', '2024-02-01', true, 1, 12, 1);

-- EXPENSE Transactions - Health (February 2024)
INSERT INTO transactions (amount, type, description, notes, transaction_date, is_recurring, account_id, category_id, user_id) VALUES
(125.00, 'EXPENSE', 'Dr. Smith - Primary Care', 'Annual checkup copay', '2024-02-12', false, 1, 10, 1),
(45.80, 'EXPENSE', 'CVS Pharmacy', 'Prescription medications', '2024-02-08', false, 2, 10, 1);

-- Add historical transactions for January 2024
INSERT INTO transactions (amount, type, description, notes, transaction_date, is_recurring, account_id, category_id, user_id) VALUES
(2450.00, 'INCOME', 'Gusto Payroll', 'Bi-weekly salary', '2024-01-15', false, 1, 1, 1),
(2450.00, 'INCOME', 'Gusto Payroll', 'Bi-weekly salary', '2024-01-01', false, 1, 1, 1),
(1850.00, 'EXPENSE', 'Monthly Rent Payment', 'January rent', '2024-01-01', true, 1, 12, 1),
(156.30, 'EXPENSE', 'Whole Foods Market', 'Weekly groceries', '2024-01-22', false, 2, 5, 1),
(67.50, 'EXPENSE', 'Shell Gas Station', 'Fuel', '2024-01-20', false, 2, 6, 1),
(82.40, 'EXPENSE', 'PG&E Utilities', 'Monthly electricity', '2024-01-11', true, 1, 7, 1),
(120.00, 'EXPENSE', 'AT&T Internet', 'Monthly internet', '2024-01-05', true, 1, 7, 1);
