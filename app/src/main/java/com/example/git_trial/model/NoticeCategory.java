package com.example.git_trial.model;

/**
 * Enum representing different notice categories
 */
public enum NoticeCategory {
    COMMON("Common"),
    DEPARTMENT("Department"),
    ANNUAL("Annual"),
    SUBJECT_SPECIFIC("Subject-Specific");

    private final String displayName;

    NoticeCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}