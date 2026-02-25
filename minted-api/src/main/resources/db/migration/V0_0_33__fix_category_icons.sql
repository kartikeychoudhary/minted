-- V0_0_33: Fix specific category icons to better-matching PrimeNG icons

-- Food & Dining -> pi pi-shop
UPDATE default_categories SET icon = 'pi pi-shop' WHERE name = 'Food & Dining';
UPDATE transaction_categories SET icon = 'pi pi-shop' WHERE name = 'Food & Dining';

-- Groceries -> pi pi-shopping-cart
UPDATE default_categories SET icon = 'pi pi-shopping-cart' WHERE name = 'Groceries';
UPDATE transaction_categories SET icon = 'pi pi-shopping-cart' WHERE name = 'Groceries';

-- EMI -> pi pi-money-bill
UPDATE default_categories SET icon = 'pi pi-money-bill' WHERE name = 'EMI';
UPDATE transaction_categories SET icon = 'pi pi-money-bill' WHERE name = 'EMI';
