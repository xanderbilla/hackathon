package com.hackathon.auth.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO class representing Bank Account from external bank_db
 * This is not a JPA entity as it's in a different database
 */
public class BankAccount {
    private Integer accountId;
    private String aadhaarNumber;
    private Long accountNumber;
    private String bankName;
    private String branchName;
    private String ifscCode;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    // Constructors
    public BankAccount() {
    }

    public BankAccount(Integer accountId, String aadhaarNumber, Long accountNumber, String bankName,
            String branchName, String ifscCode, BigDecimal balance, LocalDateTime createdAt) {
        this.accountId = accountId;
        this.aadhaarNumber = aadhaarNumber;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.branchName = branchName;
        this.ifscCode = ifscCode;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public Long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(Long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}