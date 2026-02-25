-- V0_0_32: Migrate all fa-* icons in seed data to PrimeNG pi pi-* format

-- Account Types (default_account_types -> used by DataInitializer for new users)
-- No icon column in default_account_types, but account_types has icon column seeded by DataInitializer

-- Update account_types icons from fa-* to pi pi-*
UPDATE account_types SET icon = 'pi pi-building-columns' WHERE icon = 'fa-building-columns';
UPDATE account_types SET icon = 'pi pi-credit-card' WHERE icon = 'fa-credit-card';
UPDATE account_types SET icon = 'pi pi-wallet' WHERE icon = 'fa-wallet';
UPDATE account_types SET icon = 'pi pi-chart-line' WHERE icon = 'fa-chart-line';

-- Also update any old short-form icons from DataInitializer defaults
UPDATE account_types SET icon = 'pi pi-building-columns' WHERE icon = 'bank';
UPDATE account_types SET icon = 'pi pi-credit-card' WHERE icon = 'credit-card';
UPDATE account_types SET icon = 'pi pi-wallet' WHERE icon = 'wallet';
UPDATE account_types SET icon = 'pi pi-chart-line' WHERE icon = 'chart';

-- Update default_categories icons from fa-* to pi pi-*
UPDATE default_categories SET icon = 'pi pi-briefcase' WHERE icon = 'fa-briefcase';
UPDATE default_categories SET icon = 'pi pi-desktop' WHERE icon = 'fa-laptop';
UPDATE default_categories SET icon = 'pi pi-percentage' WHERE icon = 'fa-percent';
UPDATE default_categories SET icon = 'pi pi-shopping-cart' WHERE icon = 'fa-utensils';
UPDATE default_categories SET icon = 'pi pi-shopping-cart' WHERE icon = 'fa-cart-shopping';
UPDATE default_categories SET icon = 'pi pi-car' WHERE icon = 'fa-car';
UPDATE default_categories SET icon = 'pi pi-bolt' WHERE icon = 'fa-bolt';
UPDATE default_categories SET icon = 'pi pi-play' WHERE icon = 'fa-film';
UPDATE default_categories SET icon = 'pi pi-shopping-bag' WHERE icon = 'fa-bag-shopping';
UPDATE default_categories SET icon = 'pi pi-heart' WHERE icon = 'fa-heart-pulse';
UPDATE default_categories SET icon = 'pi pi-book' WHERE icon = 'fa-graduation-cap';
UPDATE default_categories SET icon = 'pi pi-home' WHERE icon = 'fa-house';
UPDATE default_categories SET icon = 'pi pi-calculator' WHERE icon = 'fa-money-bill-transfer';
UPDATE default_categories SET icon = 'pi pi-arrow-right-arrow-left' WHERE icon = 'fa-arrow-right-arrow-left';
UPDATE default_categories SET icon = 'pi pi-building-columns' WHERE icon = 'fa-building-columns';
UPDATE default_categories SET icon = 'pi pi-credit-card' WHERE icon = 'fa-credit-card';
UPDATE default_categories SET icon = 'pi pi-wallet' WHERE icon = 'fa-wallet';
UPDATE default_categories SET icon = 'pi pi-chart-line' WHERE icon = 'fa-chart-line';

-- Update transaction_categories icons from fa-* to pi pi-*
UPDATE transaction_categories SET icon = 'pi pi-briefcase' WHERE icon = 'fa-briefcase';
UPDATE transaction_categories SET icon = 'pi pi-desktop' WHERE icon = 'fa-laptop';
UPDATE transaction_categories SET icon = 'pi pi-percentage' WHERE icon = 'fa-percent';
UPDATE transaction_categories SET icon = 'pi pi-shopping-cart' WHERE icon = 'fa-utensils';
UPDATE transaction_categories SET icon = 'pi pi-shopping-cart' WHERE icon = 'fa-cart-shopping';
UPDATE transaction_categories SET icon = 'pi pi-car' WHERE icon = 'fa-car';
UPDATE transaction_categories SET icon = 'pi pi-bolt' WHERE icon = 'fa-bolt';
UPDATE transaction_categories SET icon = 'pi pi-play' WHERE icon = 'fa-film';
UPDATE transaction_categories SET icon = 'pi pi-shopping-bag' WHERE icon = 'fa-bag-shopping';
UPDATE transaction_categories SET icon = 'pi pi-heart' WHERE icon = 'fa-heart-pulse';
UPDATE transaction_categories SET icon = 'pi pi-book' WHERE icon = 'fa-graduation-cap';
UPDATE transaction_categories SET icon = 'pi pi-home' WHERE icon = 'fa-house';
UPDATE transaction_categories SET icon = 'pi pi-calculator' WHERE icon = 'fa-money-bill-transfer';
UPDATE transaction_categories SET icon = 'pi pi-arrow-right-arrow-left' WHERE icon = 'fa-arrow-right-arrow-left';
UPDATE transaction_categories SET icon = 'pi pi-building-columns' WHERE icon = 'fa-building-columns';
UPDATE transaction_categories SET icon = 'pi pi-credit-card' WHERE icon = 'fa-credit-card';
UPDATE transaction_categories SET icon = 'pi pi-wallet' WHERE icon = 'fa-wallet';
UPDATE transaction_categories SET icon = 'pi pi-chart-line' WHERE icon = 'fa-chart-line';
