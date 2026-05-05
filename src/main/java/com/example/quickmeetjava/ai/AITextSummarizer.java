package com.example.quickmeetjava.ai;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lightweight, battery-efficient AI text summarizer for Android
 * Uses extractive summarization technique with TF-IDF scoring
 * Optimized for offline use and minimal battery consumption
 */
public class AITextSummarizer {
    private static final String TAG = "AITextSummarizer";
    
    // Common stop words to exclude from importance scoring
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "will", "would",
            "could", "should", "may", "might", "must", "can", "i", "you", "he",
            "she", "it", "we", "they", "what", "which", "who", "when", "where",
            "why", "how", "all", "each", "every", "both", "few", "more", "most",
            "other", "some", "such", "this", "that", "these", "those", "as", "if",
            "no", "not", "only", "same", "so", "than", "too", "very", "just",
            "also", "about", "above", "after", "again", "before", "between", "down",
            "during", "each", "few", "further", "had", "having", "here", "his",
            "how", "its", "more", "most", "my", "myself", "no", "nor", "our",
            "ours", "ourselves", "out", "over", "own", "same", "should", "so",
            "some", "such", "than", "that", "the", "their", "theirs", "them",
            "themselves", "then", "there", "these", "they", "this", "those",
            "through", "to", "too", "under", "until", "up", "very", "was",
            "we", "were", "what", "which", "while", "who", "whom", "why", "with",
            "you", "your", "yours", "yourself", "yourselves", "or", "own", "same",
            "should", "so", "such", "than", "that", "the", "their", "theirs"
    ));
    
    private final Context context;
    private static final int COMPRESSION_RATIO = 3; // Summary = original / 3
    private static final int MIN_SENTENCES = 1;
    private static final int MAX_SENTENCES = 5;
    
    public AITextSummarizer(Context context) {
        this.context = context;
    }
    
    /**
     * Summarizes text using extractive summarization with TF-IDF scoring
     * Battery-optimized for lightweight computation
     */
    public String summarize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "No content to summarize.";
        }
        
        try {
            // Split into sentences
            List<String> sentences = splitIntoSentences(text);
            
            if (sentences.size() <= 1) {
                return text; // No summarization needed for single sentence
            }
            
            // Calculate target summary length
            int targetSentences = Math.max(MIN_SENTENCES, 
                    Math.min(MAX_SENTENCES, (sentences.size() / COMPRESSION_RATIO) + 1));
            
            if (sentences.size() <= targetSentences) {
                return text;
            }
            
            // Score sentences using TF-IDF
            Map<Integer, Double> sentenceScores = scoresentences(sentences);
            
            // Get top sentences and sort by original order
            List<Integer> topSentenceIndices = sentenceScores.entrySet()
                    .stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .limit(targetSentences)
                    .map(Map.Entry::getKey)
                    .sorted()
                    .collect(Collectors.toList());
            
            // Build summary
            StringBuilder summary = new StringBuilder();
            for (Integer index : topSentenceIndices) {
                if (summary.length() > 0) {
                    summary.append(" ");
                }
                summary.append(sentences.get(index).trim());
            }
            
            return summary.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error summarizing text", e);
            return text.substring(0, Math.min(200, text.length())) + "...";
        }
    }
    
    /**
     * Splits text into sentences more intelligently
     */
    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        
        // Split by common sentence endings, but preserve ending punctuation
        String[] roughSentences = text.split("(?<=[.!?])\\s+");
        
        for (String sentence : roughSentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 5) { // Ignore very short fragments
                sentences.add(trimmed);
            }
        }
        
        return sentences.isEmpty() ? Arrays.asList(text) : sentences;
    }
    
    /**
     * Scores sentences using TF-IDF algorithm
     * TF = term frequency in sentence
     * IDF = inverse document frequency (importance across all sentences)
     */
    private Map<Integer, Double> scoresentences(List<String> sentences) {
        Map<Integer, Double> scores = new HashMap<>();
        
        // Build word frequency map (all sentences)
        Map<String, Integer> wordFreq = new HashMap<>();
        List<Set<String>> sentenceWords = new ArrayList<>();
        
        for (String sentence : sentences) {
            Set<String> words = new HashSet<>();
            String[] tokens = sentence.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .split("\\s+");
            
            for (String token : tokens) {
                if (!token.isEmpty() && !STOP_WORDS.contains(token)) {
                    words.add(token);
                    wordFreq.put(token, wordFreq.getOrDefault(token, 0) + 1);
                }
            }
            sentenceWords.add(words);
        }
        
        // Score each sentence
        for (int i = 0; i < sentences.size(); i++) {
            double score = 0;
            Set<String> words = sentenceWords.get(i);
            
            for (String word : words) {
                // TF: term frequency in this sentence
                double tf = 1.0 / words.size();
                
                // IDF: log(total sentences / sentences containing word)
                int docsContainingWord = 0;
                for (Set<String> docWords : sentenceWords) {
                    if (docWords.contains(word)) {
                        docsContainingWord++;
                    }
                }
                
                double idf = Math.log((double) sentences.size() / (docsContainingWord + 1));
                
                // TF-IDF = TF * IDF
                score += tf * idf;
            }
            
            // Bonus for sentence position (first sentence usually important)
            if (i == 0) {
                score *= 1.2;
            }
            
            scores.put(i, score);
        }
        
        return scores;
    }
    
    /**
     * Generates a brief summary with a maximum length
     * Used for preview/preview cards
     */
    public String generateBriefSummary(String text, int maxWords) {
        String summary = summarize(text);
        String[] words = summary.split("\\s+");
        
        if (words.length <= maxWords) {
            return summary;
        }
        
        StringBuilder brief = new StringBuilder();
        for (int i = 0; i < maxWords; i++) {
            if (brief.length() > 0) {
                brief.append(" ");
            }
            brief.append(words[i]);
        }
        brief.append("...");
        
        return brief.toString();
    }
    
    /**
     * Generates keywords from text
     * Useful for tagging and search
     */
    public List<String> extractKeywords(String text, int maxKeywords) {
        Map<String, Integer> wordFreq = new HashMap<>();
        
        String[] tokens = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .split("\\s+");
        
        for (String token : tokens) {
            if (!token.isEmpty() && !STOP_WORDS.contains(token) && token.length() > 3) {
                wordFreq.put(token, wordFreq.getOrDefault(token, 0) + 1);
            }
        }
        
        return wordFreq.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(maxKeywords)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
