CREATE TABLE user(id BIGINT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(255), created TIMESTAMP, logged_in BIT)
CREATE TABLE outbox(id BIGINT PRIMARY KEY AUTO_INCREMENT, id VARCHAR(127), value varchar(1023))
