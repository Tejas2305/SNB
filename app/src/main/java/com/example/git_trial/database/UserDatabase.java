package com.example.git_trial.database;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.git_trial.model.User;
import com.example.git_trial.model.UserRole;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Database manager for handling user data using SharedPreferences
 * In a production app, this would be replaced with SQLite or Room database
 */
public class UserDatabase {
    private static final String PREFS_NAME = "snb_user_prefs";
    private static final String USERS_KEY = "users";
    private static final String CURRENT_USER_KEY = "current_user";
    
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public UserDatabase(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        initializeDefaultUsers();
    }
    
    /**
     * Initialize default users including hardcoded admin accounts
     */
    private void initializeDefaultUsers() {
        List<User> users = getAllUsers();
        if (users.isEmpty()) {
            // Create default admin accounts
            User admin1 = new User("admin", "admin@snb.edu", "admin123", UserRole.ADMIN, "System Administrator", "Administration");
            User admin2 = new User("hod_cs", "hod@cs.edu", "hod123", UserRole.ADMIN, "Head of Department", "Computer Science");
            
            // Create sample teacher
            User teacher = new User("teacher1", "teacher@cs.edu", "teacher123", UserRole.TEACHER, "John Doe", "Computer Science");
            
            // Create sample student
            User student = new User("student1", "student@cs.edu", "student123", UserRole.STUDENT, "Jane Smith", "Computer Science");
            
            users.add(admin1);
            users.add(admin2);
            users.add(teacher);
            users.add(student);
            
            saveAllUsers(users);
        }
    }
    
    /**
     * Get all users from database
     */
    public List<User> getAllUsers() {
        String json = sharedPreferences.getString(USERS_KEY, "[]");
        Type listType = new TypeToken<List<User>>(){}.getType();
        List<User> users = gson.fromJson(json, listType);
        return users != null ? users : new ArrayList<>();
    }
    
    /**
     * Save all users to database
     */
    public void saveAllUsers(List<User> users) {
        String json = gson.toJson(users);
        sharedPreferences.edit().putString(USERS_KEY, json).apply();
    }
    
    /**
     * Add a new user
     */
    public boolean addUser(User user) {
        List<User> users = getAllUsers();
        
        // Check if username or email already exists
        for (User existingUser : users) {
            if (existingUser.getUsername().equals(user.getUsername()) || 
                existingUser.getEmail().equals(user.getEmail())) {
                return false;
            }
        }
        
        users.add(user);
        saveAllUsers(users);
        return true;
    }
    
    /**
     * Authenticate user
     */
    public User authenticate(String username, String password) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if ((user.getUsername().equals(username) || user.getEmail().equals(username)) && 
                user.getPassword().equals(password) && user.isActive()) {
                setCurrentUser(user);
                return user;
            }
        }
        return null;
    }
    
    /**
     * Set current logged-in user
     */
    public void setCurrentUser(User user) {
        String json = gson.toJson(user);
        sharedPreferences.edit().putString(CURRENT_USER_KEY, json).apply();
    }
    
    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        String json = sharedPreferences.getString(CURRENT_USER_KEY, null);
        if (json != null) {
            return gson.fromJson(json, User.class);
        }
        return null;
    }
    
    /**
     * Logout current user
     */
    public void logout() {
        sharedPreferences.edit().remove(CURRENT_USER_KEY).apply();
    }
    
    /**
     * Update user information
     */
    public boolean updateUser(User updatedUser) {
        List<User> users = getAllUsers();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(updatedUser.getUserId())) {
                users.set(i, updatedUser);
                saveAllUsers(users);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Delete user
     */
    public boolean deleteUser(String userId) {
        List<User> users = getAllUsers();
        users.removeIf(user -> user.getUserId().equals(userId));
        saveAllUsers(users);
        return true;
    }
    
    /**
     * Get users by role
     */
    public List<User> getUsersByRole(UserRole role) {
        List<User> users = getAllUsers();
        List<User> filteredUsers = new ArrayList<>();
        for (User user : users) {
            if (user.getRole() == role && user.isActive()) {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }
    
    /**
     * Get users by department
     */
    public List<User> getUsersByDepartment(String department) {
        List<User> users = getAllUsers();
        List<User> filteredUsers = new ArrayList<>();
        for (User user : users) {
            if (department.equals(user.getDepartment()) && user.isActive()) {
                filteredUsers.add(user);
            }
        }
        return filteredUsers;
    }
}