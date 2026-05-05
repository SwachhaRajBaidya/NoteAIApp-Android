package com.example.quickmeetjava.ai;

import java.util.List;

/**
 * Data model for a generated summary
 * Contains original text, summary, keywords, and metadata
 */
public class SummaryItem {
    private String originalText;
    private String summary;
    private String title;
    private String briefSummary; // 30-word preview
    private List<String> keywords;
    private long timestamp;
    private String sourceId; // Reference to original note
    private int confidence; // 0-100, how confident is the AI about this summary
    
    public SummaryItem(String originalText, String title, String sourceId) {
        this.originalText = originalText;
        this.title = title;
        this.sourceId = sourceId;
        this.timestamp = System.currentTimeMillis();
        this.confidence = 85; // Default confidence
    }
    
    // Getters and Setters
    public String getOriginalText() {
        return originalText;
    }
    
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBriefSummary() {
        return briefSummary;
    }
    
    public void setBriefSummary(String briefSummary) {
        this.briefSummary = briefSummary;
    }
    
    public List<String> getKeywords() {
        return keywords;
    }
    
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    
    public int getConfidence() {
        return confidence;
    }
    
    public void setConfidence(int confidence) {
        this.confidence = Math.max(0, Math.min(100, confidence));
    }
    
    /**
     * Gets compression ratio (original length / summary length)
     */
    public float getCompressionRatio() {
        if (summary == null || summary.isEmpty()) {
            return 0;
        }
        return (float) originalText.length() / summary.length();
    }
    
    /**
     * Checks if summary is available
     */
    public boolean isSummarized() {
        return summary != null && !summary.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SummaryItem that = (SummaryItem) o;
        return sourceId != null ? sourceId.equals(that.sourceId) : that.sourceId == null;
    }

    @Override
    public int hashCode() {
        return sourceId != null ? sourceId.hashCode() : 0;
    }
}
