package com.hackathon.auth.service;

import com.hackathon.auth.entity.AadhaarUser;
import com.hackathon.auth.entity.BankAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
public class DatabaseService {

    @Value("${app.datasource.aadhaar.url}")
    private String aadhaarDbUrl;

    @Value("${app.datasource.aadhaar.username}")
    private String aadhaarDbUsername;

    @Value("${app.datasource.aadhaar.password}")
    private String aadhaarDbPassword;

    @Value("${app.datasource.bank.url}")
    private String bankDbUrl;

    @Value("${app.datasource.bank.username}")
    private String bankDbUsername;

    @Value("${app.datasource.bank.password}")
    private String bankDbPassword;

    /**
     * Initialize Aadhaar and Bank databases if they don't exist
     */
    public void initializeDatabases() {
        try {
            initializeAadhaarDatabase();
            initializeBankDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize databases", e);
        }
    }

    /**
     * Get Aadhaar User by Aadhaar number
     */
    public AadhaarUser getAadhaarUserByNumber(String aadhaarNumber) {
        String query = "SELECT aadhaar_number, full_name, dob, gender, address " +
                "FROM aadhaar_users WHERE aadhaar_number = ?";

        try (Connection conn = DriverManager.getConnection(aadhaarDbUrl, aadhaarDbUsername, aadhaarDbPassword);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, aadhaarNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AadhaarUser(
                            rs.getString("aadhaar_number"),
                            rs.getString("full_name"),
                            rs.getDate("dob").toLocalDate(),
                            rs.getString("gender"),
                            rs.getString("address"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch Aadhaar user", e);
        }

        return null;
    }

    /**
     * Get Bank Account by Aadhaar Number
     */
    public BankAccount getBankAccountByAadhaarNumber(String aadhaarNumber) {
        String query = "SELECT account_id, aadhaar_number, account_number, bank_name, " +
                "branch_name, ifsc_code, balance, created_at " +
                "FROM bank_accounts WHERE aadhaar_number = ?";

        try (Connection conn = DriverManager.getConnection(bankDbUrl, bankDbUsername, bankDbPassword);
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, aadhaarNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new BankAccount(
                            rs.getInt("account_id"),
                            rs.getString("aadhaar_number"),
                            rs.getLong("account_number"),
                            rs.getString("bank_name"),
                            rs.getString("branch_name"),
                            rs.getString("ifsc_code"),
                            rs.getBigDecimal("balance"),
                            rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch bank account", e);
        }

        return null;
    }

    private void initializeAadhaarDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                aadhaarDbUrl.replace("/aadhaar_db", ""), aadhaarDbUsername, aadhaarDbPassword);
                Statement stmt = conn.createStatement()) {

            // Create database
            stmt.execute("CREATE DATABASE IF NOT EXISTS aadhaar_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        // Now connect to the specific database and create table
        try (Connection conn = DriverManager.getConnection(aadhaarDbUrl, aadhaarDbUsername, aadhaarDbPassword);
                Statement stmt = conn.createStatement()) {

            // Create table
            String createTableQuery = """
                    CREATE TABLE IF NOT EXISTS aadhaar_users (
                        aadhaar_user_id INT AUTO_INCREMENT PRIMARY KEY,
                        aadhaar_number CHAR(12) UNIQUE NOT NULL,
                        full_name VARCHAR(100) NOT NULL,
                        dob DATE NOT NULL,
                        gender CHAR(1),
                        address TEXT,
                        INDEX idx_aadhaar_number (aadhaar_number)
                    )
                    """;
            stmt.execute(createTableQuery);

            // Insert dummy data if table is empty
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM aadhaar_users")) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    insertAadhaarDummyData(conn);
                }
            }
        }
    }

    private void initializeBankDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                bankDbUrl.replace("/bank_db", ""), bankDbUsername, bankDbPassword);
                Statement stmt = conn.createStatement()) {

            // Create database
            stmt.execute("CREATE DATABASE IF NOT EXISTS bank_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        // Now connect to the specific database and create table
        try (Connection conn = DriverManager.getConnection(bankDbUrl, bankDbUsername, bankDbPassword);
                Statement stmt = conn.createStatement()) {

            // Create table
            String createTableQuery = """
                    CREATE TABLE IF NOT EXISTS bank_accounts (
                        account_id INT AUTO_INCREMENT PRIMARY KEY,
                        aadhaar_user_id INT UNIQUE NOT NULL,
                        account_number BIGINT UNIQUE NOT NULL,
                        bank_name VARCHAR(100) NOT NULL,
                        branch_name VARCHAR(100),
                        ifsc_code VARCHAR(20),
                        balance DECIMAL(15,2) DEFAULT 0.0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_aadhaar_user_id (aadhaar_user_id),
                        INDEX idx_account_number (account_number)
                    )
                    """;
            stmt.execute(createTableQuery);

            // Insert dummy data if table is empty
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM bank_accounts")) {
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    insertBankDummyData(conn);
                }
            }
        }
    }

    private void insertAadhaarDummyData(Connection conn) throws SQLException {
        String insertQuery = """
                INSERT IGNORE INTO aadhaar_users (aadhaar_number, full_name, dob, gender, address) VALUES
                ('123456789012', 'Rahul Sharma', '1990-01-15', 'M', '123 Gandhi Road, New Delhi, Delhi 110001'),
                ('234567890123', 'Priya Patel', '1985-03-22', 'F', '456 MG Road, Mumbai, Maharashtra 400001'),
                ('345678901234', 'Amit Kumar', '1992-07-08', 'M', '789 Brigade Road, Bangalore, Karnataka 560001'),
                ('456789012345', 'Sunita Singh', '1988-11-30', 'F', '321 Park Street, Kolkata, West Bengal 700001'),
                ('567890123456', 'Rajesh Gupta', '1995-05-12', 'M', '654 Marina Beach Road, Chennai, Tamil Nadu 600001'),
                ('678901234567', 'Kavya Nair', '1991-09-18', 'F', '987 MG Road, Kochi, Kerala 682001'),
                ('789012345678', 'Vikram Reddy', '1987-12-03', 'M', '147 Banjara Hills, Hyderabad, Telangana 500001'),
                ('890123456789', 'Neha Joshi', '1993-04-25', 'F', '258 FC Road, Pune, Maharashtra 411001'),
                ('901234567890', 'Arjun Mehta', '1989-08-14', 'M', '369 CG Road, Ahmedabad, Gujarat 380001'),
                ('012345678901', 'Deepika Rao', '1994-02-07', 'F', '741 Residency Road, Mysore, Karnataka 570001'),
                ('112233445566', 'Sanjay Agarwal', '1986-06-19', 'M', '852 Civil Lines, Jaipur, Rajasthan 302001'),
                ('223344556677', 'Anita Verma', '1990-10-11', 'F', '963 Hazratganj, Lucknow, Uttar Pradesh 226001'),
                ('334455667788', 'Manoj Tiwari', '1992-01-28', 'M', '159 Fraser Road, Patna, Bihar 800001'),
                ('445566778899', 'Ritu Saxena', '1988-07-16', 'F', '357 Mall Road, Shimla, Himachal Pradesh 171001')
                """;

        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.executeUpdate();
        }
    }

    private void insertBankDummyData(Connection conn) throws SQLException {
        String insertQuery = """
                INSERT IGNORE INTO bank_accounts (aadhaar_user_id, account_number, bank_name, branch_name, ifsc_code, balance) VALUES
                (1, 1001234567890, 'State Bank of India', 'Connaught Place Branch', 'SBIN0001234', 15000.50),
                (2, 2001234567891, 'HDFC Bank', 'Bandra Branch', 'HDFC0001235', 25000.75),
                (3, 3001234567892, 'ICICI Bank', 'Koramangala Branch', 'ICIC0001236', 18500.25),
                (4, 4001234567893, 'Axis Bank', 'Salt Lake Branch', 'UTIB0001237', 32000.00),
                (5, 5001234567894, 'Punjab National Bank', 'T. Nagar Branch', 'PUNB0001238', 12750.30),
                (6, 6001234567895, 'Canara Bank', 'Marine Drive Branch', 'CNRB0001239', 28900.80),
                (7, 7001234567896, 'Bank of Baroda', 'Secunderabad Branch', 'BARB0001240', 19200.45),
                (8, 8001234567897, 'Union Bank of India', 'Shivaji Nagar Branch', 'UBIN0001241', 41500.60),
                (9, 9001234567898, 'Indian Bank', 'Navrangpura Branch', 'IDIB0001242', 16800.90),
                (10, 1101234567899, 'Bank of India', 'Jayanagar Branch', 'BKID0001243', 22300.15),
                (11, 1201234567800, 'Central Bank of India', 'MI Road Branch', 'CBIN0001244', 35600.40),
                (12, 1301234567801, 'UCO Bank', 'Hazratganj Branch', 'UCBA0001245', 13900.85),
                (13, 1401234567802, 'Indian Overseas Bank', 'Boring Road Branch', 'IOBA0001246', 27450.70),
                (14, 1501234567803, 'Punjab & Sind Bank', 'The Mall Branch', 'PSIB0001247', 31200.55)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.executeUpdate();
        }
    }

    /**
     * Get AadhaarUser by aadhaar number - convenience method
     */
    public AadhaarUser getAadhaarUser(String aadhaarNumber) {
        return getAadhaarUserByNumber(aadhaarNumber);
    }

    /**
     * Get BankAccount by aadhaar number - convenience method
     */
    public BankAccount getBankAccount(String aadhaarNumber) {
        return getBankAccountByAadhaarNumber(aadhaarNumber);
    }
}