-- Create dashboard cards table
CREATE TABLE dashboard_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    chart_type ENUM('BAR', 'LINE', 'PIE', 'DOUGHNUT', 'AREA', 'STACKED_BAR') NOT NULL,
    x_axis_measure VARCHAR(50) NOT NULL,
    y_axis_measure VARCHAR(50) NOT NULL,
    filters JSON,
    position_order INT DEFAULT 0,
    width ENUM('HALF', 'FULL') DEFAULT 'HALF',
    user_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_dashboard_cards_user_id ON dashboard_cards(user_id);
CREATE INDEX idx_dashboard_cards_position_order ON dashboard_cards(position_order);
