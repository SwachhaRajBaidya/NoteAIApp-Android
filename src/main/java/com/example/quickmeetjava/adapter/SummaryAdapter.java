package com.example.quickmeetjava.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickmeetjava.R;
import com.example.quickmeetjava.ai.SummaryItem;
import com.example.quickmeetjava.ai.TextAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder> {
    
    private List<SummaryItem> summaryList;
    private Context context;
    private OnSummaryClickListener listener;
    
    public interface OnSummaryClickListener {
        void onCopySummary(SummaryItem summary);
        void onMoreOptions(SummaryItem summary, View view);
        void onSummaryClicked(SummaryItem summary);
    }
    
    public SummaryAdapter(Context context, List<SummaryItem> summaryList) {
        this.context = context;
        this.summaryList = summaryList != null ? summaryList : new ArrayList<>();
    }
    
    public void setOnSummaryClickListener(OnSummaryClickListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_summary_item, parent, false);
        return new SummaryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        SummaryItem item = summaryList.get(position);
        holder.bind(item, listener);
    }
    
    @Override
    public int getItemCount() {
        return summaryList.size();
    }
    
    public void updateList(List<SummaryItem> newList) {
        summaryList.clear();
        summaryList.addAll(newList);
        notifyDataSetChanged();
    }
    
    public void addItem(SummaryItem item) {
        summaryList.add(0, item); // Add to top for recent items first
        notifyItemInserted(0);
    }
    
    public void removeItem(int position) {
        if (position >= 0 && position < summaryList.size()) {
            summaryList.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public static class SummaryViewHolder extends RecyclerView.ViewHolder {
        
        private final TextView textTitle;
        private final TextView textDate;
        private final TextView textContent;
        private final TextView textConfidence;
        private final TextView textCompressionRatio;
        private final TextView textReadingTime;
        private final ImageButton btnCopy;
        private final ImageButton btnMore;
        private final LinearLayout keywordsLayout;
        private final ProgressBar progressAnimation;
        private final View cardView;
        private TextAnimator animator;
        
        public SummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = itemView;
            textTitle = itemView.findViewById(R.id.textSummaryTitle);
            textDate = itemView.findViewById(R.id.textSummaryDate);
            textContent = itemView.findViewById(R.id.textSummaryContent);
            textConfidence = itemView.findViewById(R.id.textConfidence);
            textCompressionRatio = itemView.findViewById(R.id.textCompressionRatio);
            textReadingTime = itemView.findViewById(R.id.textReadingTime);
            btnCopy = itemView.findViewById(R.id.btnCopySummary);
            btnMore = itemView.findViewById(R.id.btnMore);
            keywordsLayout = itemView.findViewById(R.id.keywordsLayout);
            progressAnimation = itemView.findViewById(R.id.progressAnimation);
        }
        
        public void bind(SummaryItem item, OnSummaryClickListener listener) {
            // Set title
            textTitle.setText(item.getTitle());
            
            // Set date
            textDate.setText(formatDate(item.getTimestamp()));
            
            // Set confidence
            textConfidence.setText(item.getConfidence() + "%");
            
            // Set compression ratio
            float ratio = item.getCompressionRatio();
            textCompressionRatio.setText(String.format(Locale.US, "%.1fx", ratio));
            
            // Calculate and set reading time
            int words = item.getSummary() != null ? 
                    item.getSummary().split("\\s+").length : 0;
            int readingTime = Math.max(1, words / 200); // Average 200 words per minute
            textReadingTime.setText(readingTime + " min");
            
            // Setup animator with pacing for natural reading experience
            if (animator != null) {
                animator.stop();
            }
            animator = new TextAnimator(textContent, 30); // 30ms per character
            
            // Animate the summary text if available
            if (item.isSummarized()) {
                animator.animateWithPacing(item.getSummary(), new TextAnimator.AnimationCallback() {
                    @Override
                    public void onAnimationComplete() {
                        progressAnimation.setVisibility(View.GONE);
                    }
                    
                    @Override
                    public void onAnimationStart() {
                        progressAnimation.setVisibility(View.VISIBLE);
                        progressAnimation.setProgress(0);
                    }
                });
            } else {
                textContent.setText("Summary unavailable");
            }
            
            // Setup keywords
            displayKeywords(item);
            
            // Setup click listeners
            btnCopy.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCopySummary(item);
                }
            });
            
            btnMore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMoreOptions(item, v);
                }
            });
            
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSummaryClicked(item);
                }
            });
        }
        
        private void displayKeywords(SummaryItem item) {
            keywordsLayout.removeAllViews();
            
            if (item.getKeywords() != null && !item.getKeywords().isEmpty()) {
                for (String keyword : item.getKeywords()) {
                    TextView keywordView = createKeywordChip(keyword);
                    keywordsLayout.addView(keywordView);
                }
            } else {
                TextView noKeywords = new TextView(itemView.getContext());
                noKeywords.setText("No keywords");
                noKeywords.setTextSize(12);
                noKeywords.setAlpha(0.5f);
                keywordsLayout.addView(noKeywords);
            }
        }
        
        private TextView createKeywordChip(String keyword) {
            TextView chip = new TextView(itemView.getContext());
            chip.setText(keyword);
            chip.setTextSize(11);
            chip.setPadding(12, 6, 12, 6);
            chip.setBackgroundResource(R.drawable.badge_background);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            chip.setLayoutParams(params);
            
            return chip;
        }
        
        private String formatDate(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            if (diff < 60_000) {
                return "Just now";
            } else if (diff < 3_600_000) {
                long minutes = diff / 60_000;
                return minutes + " min ago";
            } else if (diff < 86_400_000) {
                long hours = diff / 3_600_000;
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.US);
                return sdf.format(new Date(timestamp));
            }
        }
    }
}
