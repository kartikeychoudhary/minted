-- V0_0_34: Convert bare Material icon names to PrimeNG format
-- The categories settings UI previously stored Material icon names (e.g. 'restaurant')
-- as values instead of PrimeNG classes. Also fix pi pi-shop (invalid) from V0_0_33.

-- Convert bare Material icon names in transaction_categories
UPDATE transaction_categories SET icon = 'pi pi-shopping-cart' WHERE icon = 'restaurant';
UPDATE transaction_categories SET icon = 'pi pi-shopping-bag' WHERE icon = 'shopping_bag';
UPDATE transaction_categories SET icon = 'pi pi-car' WHERE icon = 'directions_car';
UPDATE transaction_categories SET icon = 'pi pi-video' WHERE icon = 'movie';
UPDATE transaction_categories SET icon = 'pi pi-home' WHERE icon = 'home';
UPDATE transaction_categories SET icon = 'pi pi-heart' WHERE icon = 'local_hospital';
UPDATE transaction_categories SET icon = 'pi pi-book' WHERE icon = 'school';
UPDATE transaction_categories SET icon = 'pi pi-send' WHERE icon = 'flight';
UPDATE transaction_categories SET icon = 'pi pi-bolt' WHERE icon = 'bolt';
UPDATE transaction_categories SET icon = 'pi pi-dollar' WHERE icon = 'attach_money';
UPDATE transaction_categories SET icon = 'pi pi-gift' WHERE icon = 'card_giftcard';
UPDATE transaction_categories SET icon = 'pi pi-chart-line' WHERE icon = 'trending_up';

-- Same for default_categories
UPDATE default_categories SET icon = 'pi pi-shopping-cart' WHERE icon = 'restaurant';
UPDATE default_categories SET icon = 'pi pi-shopping-bag' WHERE icon = 'shopping_bag';
UPDATE default_categories SET icon = 'pi pi-car' WHERE icon = 'directions_car';
UPDATE default_categories SET icon = 'pi pi-video' WHERE icon = 'movie';
UPDATE default_categories SET icon = 'pi pi-home' WHERE icon = 'home';
UPDATE default_categories SET icon = 'pi pi-heart' WHERE icon = 'local_hospital';
UPDATE default_categories SET icon = 'pi pi-book' WHERE icon = 'school';
UPDATE default_categories SET icon = 'pi pi-send' WHERE icon = 'flight';
UPDATE default_categories SET icon = 'pi pi-bolt' WHERE icon = 'bolt';
UPDATE default_categories SET icon = 'pi pi-dollar' WHERE icon = 'attach_money';
UPDATE default_categories SET icon = 'pi pi-gift' WHERE icon = 'card_giftcard';
UPDATE default_categories SET icon = 'pi pi-chart-line' WHERE icon = 'trending_up';

-- Fix pi pi-shop (invalid icon from V0_0_33) -> pi pi-shopping-cart
UPDATE transaction_categories SET icon = 'pi pi-shopping-cart' WHERE icon = 'pi pi-shop';
UPDATE default_categories SET icon = 'pi pi-shopping-cart' WHERE icon = 'pi pi-shop';

-- Catch-all: any remaining icon that doesn't start with 'pi pi-' gets a default
UPDATE transaction_categories SET icon = 'pi pi-tag' WHERE icon NOT LIKE 'pi pi-%';
UPDATE default_categories SET icon = 'pi pi-tag' WHERE icon NOT LIKE 'pi pi-%';
