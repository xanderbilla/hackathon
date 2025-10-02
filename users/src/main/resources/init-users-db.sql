-- ============================================================================
-- Local User Database Creation Script
-- ============================================================================
-- This script creates the local_user_db database and table if they don't exist
-- Create database if not exists
CREATE DATABASE IF NOT EXISTS local_user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE local_user_db;
-- Create Local Users Table
CREATE TABLE IF NOT EXISTS local_users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    aadhaar_number CHAR(12) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_aadhaar_number (aadhaar_number)
);
-- Show success message
SELECT 'Local user database and table created successfully!' as message;