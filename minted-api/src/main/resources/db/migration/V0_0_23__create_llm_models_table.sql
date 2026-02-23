-- V0_0_23 — Create llm_models table for LLM provider model configuration
CREATE TABLE llm_models (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    provider     VARCHAR(50) NOT NULL DEFAULT 'GEMINI',
    model_key    VARCHAR(200) NOT NULL,
    description  VARCHAR(255),
    is_active    BOOLEAN DEFAULT TRUE,
    is_default   BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_llm_model_provider_key (provider, model_key)
);

-- Seed default Gemini models
INSERT INTO llm_models (name, provider, model_key, description, is_active, is_default) VALUES
('Gemini 2.0 Flash',      'GEMINI', 'gemini-2.0-flash',      'Fast and efficient, recommended for most users',          TRUE,  TRUE),
('Gemini 2.0 Flash Lite', 'GEMINI', 'gemini-2.0-flash-lite', 'Lightest Gemini model, lowest cost',                      TRUE,  FALSE),
('Gemini 1.5 Pro',        'GEMINI', 'gemini-1.5-pro',        'Most capable Gemini model for complex statement formats',  TRUE,  FALSE);
