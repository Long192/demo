CREATE TABLE IF NOT EXISTS user
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    fullname VARCHAR(50),
    address VARCHAR(100),
    etc VARCHAR(100),
    avatar VARCHAR(100),
    dob DATE,
    role ENUM("admin", "user") DEFAULT "user",
    status ENUM("active", "unactive") DEFAULT "active",
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS post
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    content TEXT,
    status ENUM("active", "unactive") DEFAULT "active",
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS image
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT ,
    url VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY(post_id) REFERENCES post(id)
);

CREATE TABLE IF NOT EXISTS favourite
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY(post_id) REFERENCES post(id),
    FOREIGN KEY(user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS otp
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    otp VARCHAR(4),
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP DEFAULT (CURRENT_TIMESTAMP + INTERVAL 5 MINUTE),
    FOREIGN KEY(user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS friend
(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_requester_id BIGINT,
    user_receiver_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY(user_requester_id) REFERENCES user(id),
    FOREIGN KEY(user_receiver_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS comment
(
    id BIGINT PRIMARY key AUTO_INCREMENT,
    user_id BIGINT,
    post_id BIGINT,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY(post_id) REFERENCES post(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
)