-- ============================================================================
-- Auth Database Creation Script
-- ============================================================================
-- This script only creates the auth_db database if it doesn't exist

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS auth_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Show success message
SELECT 'Auth database created successfully!' as message;