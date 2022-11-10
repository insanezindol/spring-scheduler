-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS appdb;

CREATE USER 'appuser'@'%' IDENTIFIED BY '123456';

GRANT ALL PRIVILEGES ON appdb.* TO 'appuser'@'%' IDENTIFIED BY '123456';

FLUSH PRIVILEGES;

USE appdb;

CREATE TABLE IF NOT EXISTS shedlock
(
    name       VARCHAR(64),
    lock_until TIMESTAMP(3) NULL,
    locked_at  TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255),
    PRIMARY KEY (name)
)
