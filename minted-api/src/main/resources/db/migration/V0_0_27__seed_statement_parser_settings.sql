-- V0_0_27 — Seed system settings for Credit Card Statement Parser feature
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('CREDIT_CARD_PARSER_ENABLED',  'true',  'Enable/disable the Credit Card Statement Parser feature'),
('ADMIN_LLM_KEY_SHARED',        'false', 'If true, admin LLM key is available to users without their own key');
