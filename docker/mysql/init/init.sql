-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS mydatabase;

-- myuser 생성 및 비밀번호 설정
CREATE USER IF NOT EXISTS 'myuser'@'%' IDENTIFIED WITH mysql_native_password BY 'secret';

-- 모든 IP에서 mydatabase 접근 허용
GRANT ALL PRIVILEGES ON mydatabase.* TO 'myuser'@'%';
FLUSH PRIVILEGES;