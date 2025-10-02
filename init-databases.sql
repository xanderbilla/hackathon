-- ============================================================================
-- Database Initialization Script for Sustain-a-thon
-- ============================================================================
-- This script creates all required databases and tables with dummy data
-- Create Aadhaar Database
CREATE DATABASE IF NOT EXISTS aadhaar_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Create Bank Database  
CREATE DATABASE IF NOT EXISTS bank_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Create Local User Database
CREATE DATABASE IF NOT EXISTS local_user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aadhaar_db;
-- Create Aadhaar Users Table
CREATE TABLE IF NOT EXISTS aadhaar_users (
    aadhaar_number CHAR(12) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    gender CHAR(1),
    address TEXT
);
-- Insert dummy data for Aadhaar users (at least 12 records)
INSERT IGNORE INTO aadhaar_users (aadhaar_number, full_name, dob, gender, address)
VALUES (
        '123456789012',
        'Rahul Sharma',
        '1990-01-15',
        'M',
        '123 Gandhi Road, New Delhi, Delhi 110001'
    ),
    (
        '234567890123',
        'Priya Patel',
        '1985-03-22',
        'F',
        '456 MG Road, Mumbai, Maharashtra 400001'
    ),
    (
        '345678901234',
        'Amit Kumar',
        '1992-07-08',
        'M',
        '789 Brigade Road, Bangalore, Karnataka 560001'
    ),
    (
        '456789012345',
        'Sunita Singh',
        '1988-11-30',
        'F',
        '321 Park Street, Kolkata, West Bengal 700001'
    ),
    (
        '567890123456',
        'Rajesh Gupta',
        '1995-05-12',
        'M',
        '654 Marina Beach Road, Chennai, Tamil Nadu 600001'
    ),
    (
        '678901234567',
        'Kavya Nair',
        '1991-09-18',
        'F',
        '987 MG Road, Kochi, Kerala 682001'
    ),
    (
        '789012345678',
        'Vikram Reddy',
        '1987-12-03',
        'M',
        '147 Banjara Hills, Hyderabad, Telangana 500001'
    ),
    (
        '890123456789',
        'Neha Joshi',
        '1993-04-25',
        'F',
        '258 FC Road, Pune, Maharashtra 411001'
    ),
    (
        '901234567890',
        'Arjun Mehta',
        '1989-08-14',
        'M',
        '369 CG Road, Ahmedabad, Gujarat 380001'
    ),
    (
        '012345678901',
        'Deepika Rao',
        '1994-02-07',
        'F',
        '741 Residency Road, Mysore, Karnataka 570001'
    ),
    (
        '112233445566',
        'Sanjay Agarwal',
        '1986-06-19',
        'M',
        '852 Civil Lines, Jaipur, Rajasthan 302001'
    ),
    (
        '223344556677',
        'Anita Verma',
        '1990-10-11',
        'F',
        '963 Hazratganj, Lucknow, Uttar Pradesh 226001'
    ),
    (
        '334455667788',
        'Manoj Tiwari',
        '1992-01-28',
        'M',
        '159 Fraser Road, Patna, Bihar 800001'
    ),
    (
        '445566778899',
        'Ritu Saxena',
        '1988-07-16',
        'F',
        '357 Mall Road, Shimla, Himachal Pradesh 171001'
    );
USE bank_db;
-- Create Bank Accounts Table
CREATE TABLE IF NOT EXISTS bank_accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    aadhaar_number CHAR(12) UNIQUE NOT NULL,
    account_number BIGINT UNIQUE NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    branch_name VARCHAR(100),
    ifsc_code VARCHAR(20),
    balance DECIMAL(15, 2) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_aadhaar_number (aadhaar_number),
    INDEX idx_account_number (account_number),
    FOREIGN KEY (aadhaar_number) REFERENCES aadhaar_db.aadhaar_users(aadhaar_number)
);
-- Insert dummy bank account data corresponding to Aadhaar users
INSERT IGNORE INTO bank_accounts (
        aadhaar_number,
        account_number,
        bank_name,
        branch_name,
        ifsc_code,
        balance
    )
VALUES (
        '123456789012',
        1001234567890,
        'State Bank of India',
        'Connaught Place Branch',
        'SBIN0001234',
        15000.50
    ),
    (
        '234567890123',
        2001234567891,
        'HDFC Bank',
        'Bandra Branch',
        'HDFC0001235',
        25000.75
    ),
    (
        '345678901234',
        3001234567892,
        'ICICI Bank',
        'Koramangala Branch',
        'ICIC0001236',
        18500.25
    ),
    (
        '456789012345',
        4001234567893,
        'Axis Bank',
        'Salt Lake Branch',
        'UTIB0001237',
        32000.00
    ),
    (
        '567890123456',
        5001234567894,
        'Punjab National Bank',
        'T. Nagar Branch',
        'PUNB0001238',
        12750.30
    ),
    (
        '678901234567',
        6001234567895,
        'Canara Bank',
        'Marine Drive Branch',
        'CNRB0001239',
        28900.80
    ),
    (
        '789012345678',
        7001234567896,
        'Bank of Baroda',
        'Secunderabad Branch',
        'BARB0001240',
        19200.45
    ),
    (
        '890123456789',
        8001234567897,
        'Union Bank of India',
        'Shivaji Nagar Branch',
        'UBIN0001241',
        41500.60
    ),
    (
        '901234567890',
        9001234567898,
        'Indian Bank',
        'Navrangpura Branch',
        'IDIB0001242',
        16800.90
    ),
    (
        '012345678901',
        1101234567899,
        'Bank of India',
        'Jayanagar Branch',
        'BKID0001243',
        22300.15
    ),
    (
        '112233445566',
        1201234567800,
        'Central Bank of India',
        'MI Road Branch',
        'CBIN0001244',
        35600.40
    ),
    (
        '223344556677',
        1301234567801,
        'UCO Bank',
        'Hazratganj Branch',
        'UCBA0001245',
        13900.85
    ),
    (
        '334455667788',
        1401234567802,
        'Indian Overseas Bank',
        'Boring Road Branch',
        'IOBA0001246',
        27450.70
    ),
    (
        '445566778899',
        1501234567803,
        'Punjab & Sind Bank',
        'The Mall Branch',
        'PSIB0001247',
        31200.55
    );
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
    INDEX idx_aadhaar_number (aadhaar_number),
    FOREIGN KEY (aadhaar_number) REFERENCES aadhaar_db.aadhaar_users(aadhaar_number)
);
SELECT 'All databases and tables created successfully with dummy data!' as message;