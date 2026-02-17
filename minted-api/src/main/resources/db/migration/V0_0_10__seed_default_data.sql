-- Seed default account types for admin user (id=1)
INSERT INTO account_types (name, description, icon, user_id) VALUES
('Bank Account', 'Savings and current bank accounts', 'fa-building-columns', 1),
('Credit Card', 'Credit card accounts', 'fa-credit-card', 1),
('Wallet', 'Cash and digital wallets', 'fa-wallet', 1),
('Investment', 'Investment and trading accounts', 'fa-chart-line', 1);

-- Seed default transaction categories for admin user (id=1)
INSERT INTO transaction_categories (name, type, icon, color, user_id) VALUES
('Salary', 'INCOME', 'fa-briefcase', '#4CAF50', 1),
('Freelance', 'INCOME', 'fa-laptop', '#8BC34A', 1),
('Interest', 'INCOME', 'fa-percent', '#CDDC39', 1),
('Food & Dining', 'EXPENSE', 'fa-utensils', '#FF5722', 1),
('Groceries', 'EXPENSE', 'fa-cart-shopping', '#FF9800', 1),
('Transport', 'EXPENSE', 'fa-car', '#2196F3', 1),
('Utilities', 'EXPENSE', 'fa-bolt', '#FFC107', 1),
('Entertainment', 'EXPENSE', 'fa-film', '#9C27B0', 1),
('Shopping', 'EXPENSE', 'fa-bag-shopping', '#E91E63', 1),
('Health', 'EXPENSE', 'fa-heart-pulse', '#00BCD4', 1),
('Education', 'EXPENSE', 'fa-graduation-cap', '#3F51B5', 1),
('Rent', 'EXPENSE', 'fa-house', '#795548', 1),
('EMI', 'EXPENSE', 'fa-money-bill-transfer', '#607D8B', 1),
('Transfer', 'TRANSFER', 'fa-arrow-right-arrow-left', '#9E9E9E', 1);

-- Seed default dashboard cards for admin user (id=1)
INSERT INTO dashboard_cards (title, chart_type, x_axis_measure, y_axis_measure, position_order, width, user_id) VALUES
('Monthly Expenses', 'BAR', 'month', 'total_amount', 1, 'HALF', 1),
('Category Breakdown', 'DOUGHNUT', 'category', 'total_amount', 2, 'HALF', 1),
('Income vs Expense', 'LINE', 'month', 'total_amount', 3, 'FULL', 1),
('Top Spending Categories', 'PIE', 'category', 'total_amount', 4, 'HALF', 1);
