package com.example.git_trial.auth;

import android.content.Context;
import com.example.git_trial.database.UserDatabase;
import com.example.git_trial.model.User;
import com.example.git_trial.model.UserRole;

/**
 * Authentication service for managing user authentication and sessions
 */
public class AuthService {
    private UserDatabase userDatabase;
    private static AuthService instance;
    
    public AuthService(Context context) {
        this.userDatabase = new UserDatabase(context);
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized AuthService getInstance(Context context) {
        if (instance == null) {
            instance = new AuthService(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Login user with username/email and password
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return null;
        }
        
        return userDatabase.authenticate(username.trim(), password);
    }
    
    /**
     * Register new user
     */
    public boolean register(User user) {
        if (user == null || user.getUsername() == null || user.getEmail() == null || user.getPassword() == null) {
            return false;
        }
        
        // Basic validation
        if (user.getUsername().trim().isEmpty() || 
            user.getEmail().trim().isEmpty() || 
            user.getPassword().trim().isEmpty()) {
            return false;
        }
        
        // Email validation (basic)
        if (!isValidEmail(user.getEmail())) {
            return false;
        }
        
        return userDatabase.addUser(user);
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return userDatabase.getCurrentUser();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        userDatabase.logout();
    }
    
    /**
     * Check if current user has admin privileges
     */
    public boolean isAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == UserRole.ADMIN;
    }
    
    /**
     * Check if current user is a teacher
     */
    public boolean isTeacher() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == UserRole.TEACHER;
    }
    
    /**
     * Check if current user is a student
     */
    public boolean isStudent() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.getRole() == UserRole.STUDENT;
    }
    
    /**
     * Check if current user can manage notices
     */
    public boolean canManageNotices() {
        return isAdmin() || isTeacher();
    }
    
    /**
     * Check if user can manage other users
     */
    public boolean canManageUsers() {
        return isAdmin();
    }
    
    /**
     * Update current user information
     */
    public boolean updateCurrentUser(User updatedUser) {
        boolean success = userDatabase.updateUser(updatedUser);
        if (success) {
            userDatabase.setCurrentUser(updatedUser);
        }
        return success;
    }
    
    /**
     * Change password for current user
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        if (!currentUser.getPassword().equals(currentPassword)) {
            return false;
        }
        
        if (newPassword == null || newPassword.trim().length() < 6) {
            return false;
        }
        
        currentUser.setPassword(newPassword);
        return updateCurrentUser(currentUser);
    }
    
    /**
     * Basic email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
    
    /**
     * Validate password strength
     */
    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Get user database instance for admin operations
     */
    public UserDatabase getUserDatabase() {
        if (isAdmin()) {
            return userDatabase;
        }
        return null;
    }
}