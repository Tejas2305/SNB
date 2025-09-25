package com.example.git_trial.database;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.git_trial.model.Notice;
import com.example.git_trial.model.NoticeCategory;
import com.example.git_trial.model.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Database manager for handling notice data using SharedPreferences
 * In a production app, this would be replaced with SQLite or Room database
 */
public class NoticeDatabase {
    private static final String PREFS_NAME = "snb_notice_prefs";
    private static final String NOTICES_KEY = "notices";
    
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public NoticeDatabase(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        initializeSampleNotices();
    }
    
    /**
     * Initialize some sample notices for demonstration
     */
    private void initializeSampleNotices() {
        List<Notice> notices = getAllNotices();
        if (notices.isEmpty()) {
            // Create sample notices
            Notice commonNotice = new Notice(
                "Welcome to Smart Notice Board",
                "This is the new digital notice board system for our college. Please check regularly for updates.",
                NoticeCategory.COMMON,
                "admin_1",
                "System Administrator"
            );
            commonNotice.setDepartment("All");
            commonNotice.setPriority(5);
            
            Notice deptNotice = new Notice(
                "CS Department Meeting",
                "All Computer Science faculty and students are invited to the department meeting on Friday at 3 PM in Room 201.",
                NoticeCategory.DEPARTMENT,
                "hod_cs",
                "Head of Department"
            );
            deptNotice.setDepartment("Computer Science");
            deptNotice.setPriority(4);
            
            Notice annualNotice = new Notice(
                "Annual College Fest 2024",
                "The annual college fest will be held from March 15-17, 2024. Registration is now open for all events.",
                NoticeCategory.ANNUAL,
                "admin_1",
                "System Administrator"
            );
            annualNotice.setDepartment("All");
            annualNotice.setPriority(5);
            
            Notice subjectNotice = new Notice(
                "Data Structures Assignment Due",
                "The Data Structures assignment is due next Monday. Please submit your work on time.",
                NoticeCategory.SUBJECT_SPECIFIC,
                "teacher1",
                "John Doe"
            );
            subjectNotice.setSubject("Data Structures");
            subjectNotice.setDepartment("Computer Science");
            subjectNotice.setPriority(3);
            
            notices.add(commonNotice);
            notices.add(deptNotice);
            notices.add(annualNotice);
            notices.add(subjectNotice);
            
            saveAllNotices(notices);
        }
    }
    
    /**
     * Get all notices from database
     */
    public List<Notice> getAllNotices() {
        String json = sharedPreferences.getString(NOTICES_KEY, "[]");
        Type listType = new TypeToken<List<Notice>>(){}.getType();
        List<Notice> notices = gson.fromJson(json, listType);
        return notices != null ? notices : new ArrayList<>();
    }
    
    /**
     * Save all notices to database
     */
    public void saveAllNotices(List<Notice> notices) {
        String json = gson.toJson(notices);
        sharedPreferences.edit().putString(NOTICES_KEY, json).apply();
    }
    
    /**
     * Add a new notice
     */
    public boolean addNotice(Notice notice) {
        List<Notice> notices = getAllNotices();
        notices.add(notice);
        saveAllNotices(notices);
        return true;
    }
    
    /**
     * Update a notice
     */
    public boolean updateNotice(Notice updatedNotice) {
        List<Notice> notices = getAllNotices();
        for (int i = 0; i < notices.size(); i++) {
            if (notices.get(i).getNoticeId().equals(updatedNotice.getNoticeId())) {
                updatedNotice.updateTimestamp();
                notices.set(i, updatedNotice);
                saveAllNotices(notices);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Delete a notice
     */
    public boolean deleteNotice(String noticeId) {
        List<Notice> notices = getAllNotices();
        notices.removeIf(notice -> notice.getNoticeId().equals(noticeId));
        saveAllNotices(notices);
        return true;
    }
    
    /**
     * Get notices by category
     */
    public List<Notice> getNoticesByCategory(NoticeCategory category) {
        List<Notice> notices = getAllNotices();
        List<Notice> filteredNotices = new ArrayList<>();
        for (Notice notice : notices) {
            if (notice.getCategory() == category && !notice.isArchived()) {
                filteredNotices.add(notice);
            }
        }
        sortNoticesByDateDesc(filteredNotices);
        return filteredNotices;
    }
    
    /**
     * Get notices for a specific user based on their role and department
     */
    public List<Notice> getNoticesForUser(User user) {
        List<Notice> notices = getAllNotices();
        List<Notice> userNotices = new ArrayList<>();
        
        for (Notice notice : notices) {
            if (notice.isArchived()) continue;
            
            // Common notices are visible to all users
            if (notice.getCategory() == NoticeCategory.COMMON) {
                userNotices.add(notice);
                continue;
            }
            
            // Annual notices are visible to all users
            if (notice.getCategory() == NoticeCategory.ANNUAL) {
                userNotices.add(notice);
                continue;
            }
            
            // Department notices are visible to users in the same department
            if (notice.getCategory() == NoticeCategory.DEPARTMENT &&
                user.getDepartment().equals(notice.getDepartment())) {
                userNotices.add(notice);
                continue;
            }
            
            // Subject-specific notices are visible to students in that department
            if (notice.getCategory() == NoticeCategory.SUBJECT_SPECIFIC &&
                user.getDepartment().equals(notice.getDepartment())) {
                userNotices.add(notice);
            }
        }
        
        sortNoticesByDateDesc(userNotices);
        return userNotices;
    }
    
    /**
     * Get notices created by a specific user (for teachers and admins)
     */
    public List<Notice> getNoticesByCreator(String userId) {
        List<Notice> notices = getAllNotices();
        List<Notice> userNotices = new ArrayList<>();
        for (Notice notice : notices) {
            if (notice.getCreatedBy().equals(userId)) {
                userNotices.add(notice);
            }
        }
        sortNoticesByDateDesc(userNotices);
        return userNotices;
    }
    
    /**
     * Search notices by title or description
     */
    public List<Notice> searchNotices(String query, User user) {
        List<Notice> userNotices = getNoticesForUser(user);
        List<Notice> searchResults = new ArrayList<>();
        
        String lowercaseQuery = query.toLowerCase();
        for (Notice notice : userNotices) {
            if (notice.getTitle().toLowerCase().contains(lowercaseQuery) ||
                notice.getDescription().toLowerCase().contains(lowercaseQuery)) {
                searchResults.add(notice);
            }
        }
        
        return searchResults;
    }
    
    /**
     * Archive/unarchive a notice
     */
    public boolean archiveNotice(String noticeId, boolean archive) {
        List<Notice> notices = getAllNotices();
        for (Notice notice : notices) {
            if (notice.getNoticeId().equals(noticeId)) {
                notice.setArchived(archive);
                notice.updateTimestamp();
                saveAllNotices(notices);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get archived notices
     */
    public List<Notice> getArchivedNotices() {
        List<Notice> notices = getAllNotices();
        List<Notice> archivedNotices = new ArrayList<>();
        for (Notice notice : notices) {
            if (notice.isArchived()) {
                archivedNotices.add(notice);
            }
        }
        sortNoticesByDateDesc(archivedNotices);
        return archivedNotices;
    }
    
    /**
     * Sort notices by creation date (newest first)
     */
    private void sortNoticesByDateDesc(List<Notice> notices) {
        Collections.sort(notices, new Comparator<Notice>() {
            @Override
            public int compare(Notice n1, Notice n2) {
                return Long.compare(n2.getCreatedAt(), n1.getCreatedAt());
            }
        });
    }
    
    /**
     * Get notice by ID
     */
    public Notice getNoticeById(String noticeId) {
        List<Notice> notices = getAllNotices();
        for (Notice notice : notices) {
            if (notice.getNoticeId().equals(noticeId)) {
                return notice;
            }
        }
        return null;
    }
}