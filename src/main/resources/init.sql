-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS appdb;

-- MySQL 8.x 부터는 GRANT ... IDENTIFIED BY 구문이 제거되어 사용자 생성과 권한 부여를 분리
CREATE USER IF NOT EXISTS 'appuser'@'%' IDENTIFIED BY '123456';

GRANT ALL PRIVILEGES ON appdb.* TO 'appuser'@'%';

FLUSH PRIVILEGES;

USE appdb;

CREATE TABLE IF NOT EXISTS shedlock
(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);
