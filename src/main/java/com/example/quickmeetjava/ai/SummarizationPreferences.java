package com.example.quickmeetjava.ai;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user preferences for AI summarization feature
 */
public class SummarizationPreferences {
    
    private static final String PREFS_NAME = "cute_notes_ai_prefs";
    private static final String KEY_ANIMATION_SPEED = "animation_speed";
    private static final String KEY_SUMMARY_LENGTH = "summary_length";
    private static final String KEY_COMPRESSION_RATIO = "compression_ratio";
    private static final String KEY_AUTO_SUMMARIZE = "auto_summarize";
    private static final String KEY_SHOW_KEYWORDS = "show_keywords";
    private static final String KEY_ANIMATION_ENABLED = "animation_enabled";
    private static final String KEY_PACING_ENABLED = "pacing_enabled";
    
    // Default values
    private static final int DEFAULT_ANIMATION_SPEED = 30; // ms
    private static final int DEFAULT_SUMMARY_LENGTH = 3; // 1/3 compression
    private static final int DEFAULT_COMPRESSION_RATIO = 3;
    private static final boolean DEFAULT_AUTO_SUMMARIZE = true;
    private static final boolean DEFAULT_SHOW_KEYWORDS = true;
    private static final boolean DEFAULT_ANIMATION_ENABLED = true;
    private static final boolean DEFAULT_PACING_ENABLED = true;
    
    private final SharedPreferences prefs;
    
    public SummarizationPreferences(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Animation Speed (in milliseconds)
    public int getAnimationSpeed() {
        return prefs.getInt(KEY_ANIMATION_SPEED, DEFAULT_ANIMATION_SPEED);
    }
    
    public void setAnimationSpeed(int speedMs) {
        prefs.edit().putInt(KEY_ANIMATION_SPEED, speedMs).apply();
    }
    
    // Summary Length (compression ratio)
    public int getCompressionRatio() {
        return prefs.getInt(KEY_COMPRESSION_RATIO, DEFAULT_COMPRESSION_RATIO);
    }
    
    public void setCompressionRatio(int ratio) {
        prefs.edit().putInt(KEY_COMPRESSION_RATIO, ratio).apply();
    }
    
    // Auto-summarize on app launch
    public boolean isAutoSummarizeEnabled() {
        return prefs.getBoolean(KEY_AUTO_SUMMARIZE, DEFAULT_AUTO_SUMMARIZE);
    }
    
    public void setAutoSummarizeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_SUMMARIZE, enabled).apply();
    }
    
    // Show keywords in summary cards
    public boolean isShowKeywordsEnabled() {
        return prefs.getBoolean(KEY_SHOW_KEYWORDS, DEFAULT_SHOW_KEYWORDS);
    }
    
    public void setShowKeywordsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SHOW_KEYWORDS, enabled).apply();
    }
    
    // Enable text animation
    public boolean isAnimationEnabled() {
        return prefs.getBoolean(KEY_ANIMATION_ENABLED, DEFAULT_ANIMATION_ENABLED);
    }
    
    public void setAnimationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_ANIMATION_ENABLED, enabled).apply();
    }
    
    // Enable adaptive pacing in animation
    public boolean isPacingEnabled() {
        return prefs.getBoolean(KEY_PACING_ENABLED, DEFAULT_PACING_ENABLED);
    }
    
    public void setPacingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PACING_ENABLED, enabled).apply();
    }
    
    // Reset to defaults
    public void resetToDefaults() {
        prefs.edit()
                .putInt(KEY_ANIMATION_SPEED, DEFAULT_ANIMATION_SPEED)
                .putInt(KEY_COMPRESSION_RATIO, DEFAULT_COMPRESSION_RATIO)
                .putBoolean(KEY_AUTO_SUMMARIZE, DEFAULT_AUTO_SUMMARIZE)
                .putBoolean(KEY_SHOW_KEYWORDS, DEFAULT_SHOW_KEYWORDS)
                .putBoolean(KEY_ANIMATION_ENABLED, DEFAULT_ANIMATION_ENABLED)
                .putBoolean(KEY_PACING_ENABLED, DEFAULT_PACING_ENABLED)
                .apply();
    }
    
    // Get all settings as a readable string (for debugging)
    public String getAllSettingsAsString() {
        return "Animation Speed: " + getAnimationSpeed() + "ms\n" +
               "Compression Ratio: 1/" + getCompressionRatio() + "\n" +
               "Auto-summarize: " + isAutoSummarizeEnabled() + "\n" +
               "Show Keywords: " + isShowKeywordsEnabled() + "\n" +
               "Animation Enabled: " + isAnimationEnabled() + "\n" +
               "Pacing Enabled: " + isPacingEnabled();
    }
}
