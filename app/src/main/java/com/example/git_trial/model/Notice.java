package com.example.git_trial.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Notice model class representing all notices in the system
 */
public class Notice implements Serializable {
    private String noticeId;
    private String title;
    private String description;
    private NoticeCategory category;
    private String createdBy; // User ID who created the notice
    private String createdByName; // Full name for display
    private long createdAt;
    private long updatedAt;
    private boolean isArchived;
    private List<String> attachments; // File paths/URLs for attachments
    private String department; // Applicable department
    private String subject; // For subject-specific notices
    private int priority; // 1 (low) to 5 (high)

    // Default constructor
    public Notice() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = this.createdAt;
        this.isArchived = false;
        this.attachments = new ArrayList<>();
        this.priority = 3; // Medium priority by default
    }

    // Constructor for creating new notices
    public Notice(String title, String description, NoticeCategory category, String createdBy, String createdByName) {
        this();
        this.title = title;
        this.description = description;
        this.category = category;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.noticeId = generateNoticeId();
    }

    // Getters and Setters
    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NoticeCategory getCategory() {
        return category;
    }

    public void setCategory(NoticeCategory category) {
        this.category = category;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(String attachment) {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.add(attachment);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = Math.max(1, Math.min(5, priority)); // Ensure priority is between 1 and 5
    }

    private String generateNoticeId() {
        return "notice_" + System.currentTimeMillis();
    }

    public void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }
}