package com.example.quickmeetjava.ai;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

/**
 * Handles text animation with letter-by-letter effect (ChatGPT style)
 * Optimized for smooth animation without excessive UI updates
 */
public class TextAnimator {
    
    private TextView textView;
    private String fullText;
    private Handler handler;
    private Runnable animationRunnable;
    private int currentIndex = 0;
    private long delayMillis;
    private AnimationCallback callback;
    private boolean isAnimating = false;
    
    public interface AnimationCallback {
        void onAnimationComplete();
        void onAnimationStart();
    }
    
    /**
     * Constructor with default speed (50ms per character)
     */
    public TextAnimator(TextView textView) {
        this(textView, 50);
    }
    
    /**
     * Constructor with custom animation speed
     * @param textView The TextView to animate
     * @param delayMillis Delay between each character (ms)
     */
    public TextAnimator(TextView textView, long delayMillis) {
        this.textView = textView;
        this.delayMillis = delayMillis;
        this.handler = new Handler(Looper.getMainLooper());
        this.currentIndex = 0;
    }
    
    /**
     * Starts the animation with the given text
     */
    public void animate(String text) {
        animate(text, null);
    }
    
    /**
     * Starts the animation with callback
     */
    public void animate(String text, AnimationCallback callback) {
        // Stop any existing animation
        stop();
        
        this.fullText = text;
        this.currentIndex = 0;
        this.callback = callback;
        this.isAnimating = true;
        
        textView.setText("");
        
        if (callback != null) {
            callback.onAnimationStart();
        }
        
        scheduleNextCharacter();
    }
    
    /**
     * Animates text with adaptive speed based on punctuation
     * Adds pauses at punctuation marks for natural reading
     */
    public void animateWithPacing(String text, AnimationCallback callback) {
        stop();
        
        this.fullText = text;
        this.currentIndex = 0;
        this.callback = callback;
        this.isAnimating = true;
        
        textView.setText("");
        
        if (callback != null) {
            callback.onAnimationStart();
        }
        
        scheduleNextCharacterWithPacing();
    }
    
    /**
     * Schedules the next character in the animation
     */
    private void scheduleNextCharacter() {
        if (currentIndex < fullText.length() && isAnimating) {
            handler.postDelayed(this::addNextCharacter, delayMillis);
        }
    }
    
    /**
     * Schedules next character with adaptive pacing
     */
    private void scheduleNextCharacterWithPacing() {
        if (currentIndex < fullText.length() && isAnimating) {
            long delay = delayMillis;
            
            // Add pauses at punctuation
            if (currentIndex > 0) {
                char prevChar = fullText.charAt(currentIndex - 1);
                if (prevChar == '.' || prevChar == '!' || prevChar == '?') {
                    delay = delayMillis * 8; // Long pause after sentences
                } else if (prevChar == ',' || prevChar == ';') {
                    delay = delayMillis * 4; // Medium pause after commas
                }
            }
            
            handler.postDelayed(this::addNextCharacterWithPacing, delay);
        }
    }
    
    /**
     * Adds the next character to the TextView
     */
    private void addNextCharacter() {
        if (currentIndex < fullText.length()) {
            String displayText = fullText.substring(0, currentIndex + 1);
            textView.setText(displayText);
            currentIndex++;
            scheduleNextCharacter();
        } else {
            isAnimating = false;
            if (callback != null) {
                callback.onAnimationComplete();
            }
        }
    }
    
    /**
     * Adds next character with pacing
     */
    private void addNextCharacterWithPacing() {
        if (currentIndex < fullText.length()) {
            String displayText = fullText.substring(0, currentIndex + 1);
            textView.setText(displayText);
            currentIndex++;
            scheduleNextCharacterWithPacing();
        } else {
            isAnimating = false;
            if (callback != null) {
                callback.onAnimationComplete();
            }
        }
    }
    
    /**
     * Completes animation immediately
     */
    public void completeAnimation() {
        handler.removeCallbacks(null);
        isAnimating = false;
        
        if (fullText != null) {
            textView.setText(fullText);
        }
        
        if (callback != null) {
            callback.onAnimationComplete();
        }
    }
    
    /**
     * Stops the animation
     */
    public void stop() {
        handler.removeCallbacks(null);
        isAnimating = false;
    }
    
    /**
     * Pauses the animation
     */
    public void pause() {
        handler.removeCallbacks(null);
        isAnimating = false;
    }
    
    /**
     * Resumes animation from where it was paused
     */
    public void resume() {
        if (currentIndex < fullText.length()) {
            isAnimating = true;
            scheduleNextCharacter();
        }
    }
    
    /**
     * Sets the animation delay
     */
    public void setDelay(long delayMillis) {
        this.delayMillis = delayMillis;
    }
    
    /**
     * Checks if animation is currently running
     */
    public boolean isAnimating() {
        return isAnimating;
    }
    
    /**
     * Gets current animation progress
     */
    public float getProgress() {
        if (fullText == null || fullText.isEmpty()) {
            return 0;
        }
        return (float) currentIndex / fullText.length();
    }
}
