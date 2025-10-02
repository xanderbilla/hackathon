package com.hackathon.auth.dto;

public class UserInfoResponse {
    private String username;
    private Integer userId;
    private String aadhaarNumber;
    private FiryInfo firyInfo;
    private BankDetails bankDetails;

    // Constructors
    public UserInfoResponse() {}

    public UserInfoResponse(String username, Integer userId, String aadhaarNumber, 
                           FiryInfo firyInfo, BankDetails bankDetails) {
        this.username = username;
        this.userId = userId;
        this.aadhaarNumber = aadhaarNumber;
        this.firyInfo = firyInfo;
        this.bankDetails = bankDetails;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public FiryInfo getFiryInfo() {
        return firyInfo;
    }

    public void setFiryInfo(FiryInfo firyInfo) {
        this.firyInfo = firyInfo;
    }

    public BankDetails getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(BankDetails bankDetails) {
        this.bankDetails = bankDetails;
    }

    // Inner classes for structured data
    public static class FiryInfo {
        private String aadhaarNumber;
        private String name;
        private String fatherName;
        private String gender;
        private String dateOfBirth;
        private String address;
        private String pincode;

        // Constructors
        public FiryInfo() {}

        public FiryInfo(String aadhaarNumber, String name, String fatherName, String gender, 
                       String dateOfBirth, String address, String pincode) {
            this.aadhaarNumber = aadhaarNumber;
            this.name = name;
            this.fatherName = fatherName;
            this.gender = gender;
            this.dateOfBirth = dateOfBirth;
            this.address = address;
            this.pincode = pincode;
        }

        // Getters and Setters
        public String getAadhaarNumber() { return aadhaarNumber; }
        public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getFatherName() { return fatherName; }
        public void setFatherName(String fatherName) { this.fatherName = fatherName; }
        
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        
        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getPincode() { return pincode; }
        public void setPincode(String pincode) { this.pincode = pincode; }
    }

    public static class BankDetails {
        private String accountNumber;
        private String accountHolderName;
        private String bankName;
        private String ifscCode;
        private String accountType;
        private Double balance;

        // Constructors
        public BankDetails() {}

        public BankDetails(String accountNumber, String accountHolderName, String bankName, 
                          String ifscCode, String accountType, Double balance) {
            this.accountNumber = accountNumber;
            this.accountHolderName = accountHolderName;
            this.bankName = bankName;
            this.ifscCode = ifscCode;
            this.accountType = accountType;
            this.balance = balance;
        }

        // Getters and Setters
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public String getAccountHolderName() { return accountHolderName; }
        public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
        
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        
        public String getIfscCode() { return ifscCode; }
        public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }
        
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        
        public Double getBalance() { return balance; }
        public void setBalance(Double balance) { this.balance = balance; }
    }
}