-- V0_0_24 — Create llm_configurations table for per-user LLM settings
CREATE TABLE llm_configurations (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT NOT NULL UNIQUE,
    provider     VARCHAR(50) NOT NULL DEFAULT 'GEMINI',
    api_key      VARCHAR(500),
    model_id     BIGINT,
    is_enabled   BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (model_id) REFERENCES llm_models(id) ON DELETE SET NULL
);
