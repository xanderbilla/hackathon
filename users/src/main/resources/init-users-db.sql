-- ============================================================================
-- Users Database Creation Script
-- ============================================================================
-- This script only creates the users_db database if it doesn't exist

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS users_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Show success message
SELECT 'Users database created successfully!' as message;