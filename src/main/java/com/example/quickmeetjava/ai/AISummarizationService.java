package com.example.quickmeetjava.ai;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.example.quickmeetjava.MySqliteHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background service for efficient AI summarization
 * Handles batch processing of multiple notes with optimal resource management
 */
public class AISummarizationService {
    
    private static final String TAG = "AISummarizationService";
    private static final int BATCH_SIZE = 5;
    private static final int THREAD_POOL_SIZE = 2; // Keep battery efficient
    
    private final Context context;
    private final AITextSummarizer summarizer;
    private final MySqliteHelper dbHelper;
    private final ExecutorService executor;
    private SummarizationCallback callback;
    private boolean isProcessing = false;
    
    public interface SummarizationCallback {
        void onSummaryGenerated(SummaryItem item);
        void onBatchComplete(int totalProcessed);
        void onError(Exception e);
    }
    
    public AISummarizationService(Context context) {
        this.context = context;
        this.summarizer = new AITextSummarizer(context);
        this.dbHelper = new MySqliteHelper(context);
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }
    
    /**
     * Set callback for summarization events
     */
    public void setCallback(SummarizationCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Start background summarization of all notes
     */
    public void summarizeAllNotes() {
        if (isProcessing) {
            Log.w(TAG, "Summarization already in progress");
            return;
        }
        
        isProcessing = true;
        executor.execute(this::processBatch);
    }
    
    /**
     * Summarize specific note by ID
     */
    public void summarizeNote(String randomId) {
        executor.execute(() -> {
            try {
                Cursor cursor = dbHelper.selectPostRand(randomId);
                
                if (cursor != null && cursor.moveToFirst()) {
                    String text = cursor.getString(2);
                    String title = cursor.getString(6);
                    
                    SummaryItem item = new SummaryItem(text, title, randomId);
                    generateSummaryInternal(item);
                    
                    if (callback != null) {
                        callback.onSummaryGenerated(item);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error summarizing note", e);
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
    
    /**
     * Process notes in batches for optimal resource usage
     */
    private void processBatch() {
        try {
            Cursor cursor = dbHelper.selectPost();
            List<SummaryItem> batch = new ArrayList<>();
            
            if (cursor != null && cursor.moveToFirst()) {
                int processed = 0;
                
                do {
                    String text = cursor.getString(2);
                    String title = cursor.getString(6);
                    String randomId = cursor.getString(5);
                    
                    SummaryItem item = new SummaryItem(text, title, randomId);
                    generateSummaryInternal(item);
                    batch.add(item);
                    
                    if (callback != null) {
                        callback.onSummaryGenerated(item);
                    }
                    
                    processed++;
                    
                    // Report batch completion periodically
                    if (processed % BATCH_SIZE == 0 && callback != null) {
                        callback.onBatchComplete(processed);
                        
                        // Small delay between batches to conserve battery
                        Thread.sleep(100);
                    }
                    
                } while (cursor.moveToNext());
                
                cursor.close();
                
                if (callback != null) {
                    callback.onBatchComplete(processed);
                }
                
                isProcessing = false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in batch processing", e);
            if (callback != null) {
                callback.onError(e);
            }
            isProcessing = false;
        }
    }
    
    /**
     * Internal method to generate full summary with all metadata
     */
    private void generateSummaryInternal(SummaryItem item) {
        try {
            // Generate summary
            String summary = summarizer.summarize(item.getOriginalText());
            item.setSummary(summary);
            
            // Generate brief version
            String brief = summarizer.generateBriefSummary(item.getOriginalText(), 30);
            item.setBriefSummary(brief);
            
            // Extract keywords
            List<String> keywords = summarizer.extractKeywords(item.getOriginalText(), 5);
            item.setKeywords(keywords);
            
            // Calculate confidence (simple heuristic)
            int confidence = calculateConfidence(item);
            item.setConfidence(confidence);
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating summary", e);
            item.setSummary("Summary generation failed");
        }
    }
    
    /**
     * Calculate confidence score for the generated summary
     * Based on various heuristics
     */
    private int calculateConfidence(SummaryItem item) {
        int confidence = 85; // Base confidence
        
        // Adjust based on text length
        int originalLength = item.getOriginalText().length();
        if (originalLength < 100) {
            confidence -= 15; // Low confidence for very short texts
        } else if (originalLength > 5000) {
            confidence -= 5; // Slightly lower for very long texts
        }
        
        // Adjust based on compression ratio
        float ratio = item.getCompressionRatio();
        if (ratio < 2.0f) {
            confidence -= 10; // Poor compression
        } else if (ratio > 5.0f) {
            confidence -= 5; // Over-compression
        }
        
        // Adjust based on keyword extraction
        if (item.getKeywords() != null && item.getKeywords().size() >= 5) {
            confidence += 5; // Good keywords extracted
        }
        
        return Math.max(60, Math.min(100, confidence));
    }
    
    /**
     * Stop summarization process
     */
    public void stopSummarization() {
        isProcessing = false;
    }
    
    /**
     * Check if summarization is currently running
     */
    public boolean isRunning() {
        return isProcessing;
    }
    
    /**
     * Shutdown the service
     */
    public void shutdown() {
        executor.shutdownNow();
        isProcessing = false;
    }
    
    /**
     * Get summary statistics
     */
    public SummarizationStats getStats() {
        return new SummarizationStats(isProcessing);
    }
    
    /**
     * Statistics about summarization
     */
    public static class SummarizationStats {
        public boolean isRunning;
        
        public SummarizationStats(boolean isRunning) {
            this.isRunning = isRunning;
        }
    }
}
