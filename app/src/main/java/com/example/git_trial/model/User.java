package com.example.git_trial.model;

import java.io.Serializable;

/**
 * User model class representing all users in the system
 */
public class User implements Serializable {
    private String userId;
    private String username;
    private String email;
    private String password;
    private UserRole role;
    private String fullName;
    private String department;
    private long createdAt;
    private boolean isActive;

    // Default constructor
    public User() {
        this.createdAt = System.currentTimeMillis();
        this.isActive = true;
    }

    // Constructor for creating new users
    public User(String username, String email, String password, UserRole role, String fullName, String department) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.department = department;
        this.userId = generateUserId();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private String generateUserId() {
        return role.toString().toLowerCase() + "_" + System.currentTimeMillis();
    }
}