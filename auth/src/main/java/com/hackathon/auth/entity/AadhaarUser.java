package com.hackathon.auth.entity;

import java.time.LocalDate;

/**
 * DTO class representing Aadhaar User from external aadhaar_db
 * This is not a JPA entity as it's in a different database
 */
public class AadhaarUser {
    private String aadhaarNumber;
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String address;

    // Constructors
    public AadhaarUser() {
    }

    public AadhaarUser(String aadhaarNumber, String fullName, LocalDate dob, String gender, String address) {
        this.aadhaarNumber = aadhaarNumber;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
    }

    // Getters and Setters

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}