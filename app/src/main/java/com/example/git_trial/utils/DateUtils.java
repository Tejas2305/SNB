package com.example.git_trial.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date and time formatting
 */
public class DateUtils {
    
    private static final long MINUTE_MILLIS = 60 * 1000;
    private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final long DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final long WEEK_MILLIS = 7 * DAY_MILLIS;
    
    /**
     * Get human-readable time ago string
     */
    public static String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        if (diff < MINUTE_MILLIS) {
            return "Just now";
        } else if (diff < HOUR_MILLIS) {
            long minutes = diff / MINUTE_MILLIS;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (diff < DAY_MILLIS) {
            long hours = diff / HOUR_MILLIS;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (diff < WEEK_MILLIS) {
            long days = diff / DAY_MILLIS;
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            // For older dates, show the actual date
            return formatDate(timestamp);
        }
    }
    
    /**
     * Format timestamp to readable date string
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp to readable date and time string
     */
    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format timestamp to time only string
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Check if two timestamps are on the same day
     */
    public static boolean isSameDay(long timestamp1, long timestamp2) {
        SimpleDateFormat dayFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dayFormatter.format(new Date(timestamp1)).equals(dayFormatter.format(new Date(timestamp2)));
    }
    
    /**
     * Get day of week from timestamp
     */
    public static String getDayOfWeek(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}