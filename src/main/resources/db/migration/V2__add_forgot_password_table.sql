CREATE TABLE IF NOT EXISTS forgot_password(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    token varchar(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL 5 MINUTE),
    FOREIGN KEY(user_id) REFERENCES user(id)
)