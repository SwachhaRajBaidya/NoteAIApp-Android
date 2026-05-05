package com.example.quickmeetjava;

import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.quickmeetjava.adapter.SummaryAdapter;
import com.example.quickmeetjava.ai.AITextSummarizer;
import com.example.quickmeetjava.ai.SummaryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Modern AI-powered fragment for displaying note summaries
 * Features:
 * - Real-time text animation (letter-by-letter)
 * - Offline AI summarization
 * - Battery-optimized processing
 * - Modern chat-like UI
 */
public class MessageFragment extends Fragment implements SummaryAdapter.OnSummaryClickListener {
    
    private static final String TAG = "MessageFragment";
    private RecyclerView recyclerViewSummaries;
    private SummaryAdapter adapter;
    private List<SummaryItem> summaryList = new ArrayList<>();
    private MySqliteHelper mySqliteHelper;
    private AITextSummarizer summarizer;
    private EditText editTextSearch;
    private ImageButton btnRefresh;
    private ImageButton btnSettings;
    private LinearLayout emptyStateView;
    private ExecutorService backgroundExecutor;
    
    public MessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backgroundExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        
        // Initialize components
        initializeViews(view);
        setupRecyclerView();
        setupEventListeners();
        
        // Initialize AI components
        mySqliteHelper = new MySqliteHelper(getContext());
        summarizer = new AITextSummarizer(getContext());
        
        // Load initial summaries from database
        loadSummariesFromDatabase();
        
        return view;
    }
    
    private void initializeViews(View view) {
        recyclerViewSummaries = view.findViewById(R.id.recyclerViewSummaries);
        editTextSearch = view.findViewById(R.id.editTextSearch);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        btnSettings = view.findViewById(R.id.btnSettings);
        emptyStateView = view.findViewById(R.id.emptyStateView);
    }
    
    private void setupRecyclerView() {
        adapter = new SummaryAdapter(getContext(), summaryList);
        adapter.setOnSummaryClickListener(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewSummaries.setLayoutManager(layoutManager);
        recyclerViewSummaries.setAdapter(adapter);
        recyclerViewSummaries.setHasFixedSize(false);

        // Add scroll listener to show bottom nav when scrolled to top or when scrolling after items removed
        recyclerViewSummaries.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Show bottom nav when scrolled to the very top
                if (!recyclerView.canScrollVertically(-1)) {
                    if (getActivity() instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        com.google.android.material.bottomappbar.BottomAppBar bottomAppBar =
                            mainActivity.findViewById(R.id.bottomAppBar);

                        if (bottomAppBar != null && bottomAppBar.getVisibility() != View.VISIBLE) {
                            bottomAppBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Show bottom nav whenever user starts dragging/scrolling
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (getActivity() instanceof MainActivity) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        com.google.android.material.bottomappbar.BottomAppBar bottomAppBar =
                            mainActivity.findViewById(R.id.bottomAppBar);

                        if (bottomAppBar != null && bottomAppBar.getVisibility() != View.VISIBLE) {
                            bottomAppBar.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        // Add touch listener to show bottom nav on any touch when it's hidden
        recyclerViewSummaries.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                // Show bottom nav on any touch event when it's not visible
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    com.google.android.material.bottomappbar.BottomAppBar bottomAppBar =
                        mainActivity.findViewById(R.id.bottomAppBar);

                    if (bottomAppBar != null && bottomAppBar.getVisibility() != View.VISIBLE) {
                        bottomAppBar.setVisibility(View.VISIBLE);
                    }
                }
                return false; // Don't consume the touch event
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                // Not needed since we're not consuming the event
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                // Not needed
            }
        });
    }
    
    private void setupEventListeners() {
        // Search functionality
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSummaries(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Refresh button
        btnRefresh.setOnClickListener(v -> {
            showToast("Regenerating summaries...");
            regenerateAllSummaries();
        });
        
        // Settings button
        btnSettings.setOnClickListener(v -> {
            showSummarySettingsMenu(v);
        });
    }
    
    /**
     * Loads all notes from database and generates summaries
     */
    private void loadSummariesFromDatabase() {
        backgroundExecutor.execute(() -> {
            try {
                Cursor cursor = mySqliteHelper.selectPost();
                summaryList.clear();
                
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String postText = cursor.getString(2); // post_text column
                        String title = cursor.getString(6); // title column
                        String randomId = cursor.getString(5); // randomId column
                        String date = cursor.getString(1); // date column
                        
                        // Create summary item
                        SummaryItem item = new SummaryItem(postText, title, randomId);
                        item.setTimestamp(System.currentTimeMillis());
                        
                        // Generate summary asynchronously
                        generateSummary(item);
                        
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                
                // Update UI on main thread
                getActivity().runOnUiThread(this::updateEmptyState);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Generates summary for a single note
     */
    private void generateSummary(SummaryItem item) {
        backgroundExecutor.execute(() -> {
            try {
                // Generate full summary
                String summary = summarizer.summarize(item.getOriginalText());
                item.setSummary(summary);
                
                // Generate brief summary (preview)
                String briefSummary = summarizer.generateBriefSummary(
                        item.getOriginalText(), 30);
                item.setBriefSummary(briefSummary);
                
                // Extract keywords
                List<String> keywords = summarizer.extractKeywords(
                        item.getOriginalText(), 5);
                item.setKeywords(keywords);
                
                // Add/update UI on main thread
                getActivity().runOnUiThread(() -> {
                    int existingIndex = findSummaryIndexBySourceId(item.getSourceId());
                    if (existingIndex >= 0) {
                        summaryList.set(existingIndex, item);
                        adapter.notifyItemChanged(existingIndex);
                    } else {
                        summaryList.add(item);
                        adapter.notifyItemInserted(summaryList.size() - 1);
                    }
                    updateEmptyState();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private int findSummaryIndexBySourceId(String sourceId) {
        if (sourceId == null) {
            return -1;
        }
        for (int i = 0; i < summaryList.size(); i++) {
            SummaryItem current = summaryList.get(i);
            if (sourceId.equals(current.getSourceId())) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Filters summaries based on search query
     */
    private void filterSummaries(String query) {
        if (query.isEmpty()) {
            adapter.updateList(summaryList);
            return;
        }
        
        List<SummaryItem> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        
        for (SummaryItem item : summaryList) {
            if (item.getTitle().toLowerCase().contains(lowerQuery) ||
                item.getSummary().toLowerCase().contains(lowerQuery) ||
                (item.getKeywords() != null && 
                 item.getKeywords().stream().anyMatch(k -> k.contains(lowerQuery)))) {
                filtered.add(item);
            }
        }
        
        adapter.updateList(filtered);
    }
    
    /**
     * Regenerates summaries for all notes
     */
    private void regenerateAllSummaries() {
        backgroundExecutor.execute(() -> {
            try {
                // Clear current summaries
                summaryList.clear();
                
                // Reload from database
                Cursor cursor = mySqliteHelper.selectPost();
                
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String postText = cursor.getString(2);
                        String title = cursor.getString(6);
                        String randomId = cursor.getString(5);
                        
                        SummaryItem item = new SummaryItem(postText, title, randomId);
                        generateSummary(item);
                        
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                
                getActivity().runOnUiThread(this::updateEmptyState);
                showToast("Summaries regenerated!");
                
            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error regenerating summaries");
            }
        });
    }
    
    /**
     * Updates empty state visibility
     */
    private void updateEmptyState() {
        if (summaryList.isEmpty()) {
            emptyStateView.setVisibility(View.VISIBLE);
            recyclerViewSummaries.setVisibility(View.GONE);
        } else {
            emptyStateView.setVisibility(View.GONE);
            recyclerViewSummaries.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Shows settings popup menu
     */
    private void showSummarySettingsMenu(View anchorView) {
        PopupMenu menu = new PopupMenu(getContext(), anchorView);
        menu.getMenuInflater().inflate(R.menu.menu_summary_options, menu.getMenu());
        
        menu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_animation_speed) {
                showAnimationSpeedDialog();
                return true;
            } else if (itemId == R.id.action_summary_length) {
                showSummaryLengthDialog();
                return true;
            } else if (itemId == R.id.action_about) {
                showAboutDialog();
                return true;
            }
            return false;
        });
        
        menu.show();
    }
    
    private void showAnimationSpeedDialog() {
        showToast("Animation speed settings");
        // TODO: Implement animation speed selector
    }
    
    private void showSummaryLengthDialog() {
        showToast("Summary length settings");
        // TODO: Implement summary length selector
    }
    
    private void showAboutDialog() {
        showToast("AI Note Summarizer - Powered by offline NLP");
    }
    
    /**
     * Shows a brief toast message
     */
    private void showToast(String message) {
        getActivity().runOnUiThread(() -> 
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
        );
    }
    
    // SummaryAdapter.OnSummaryClickListener implementations
    
    @Override
    public void onCopySummary(SummaryItem summary) {
        ClipboardManager clipboard = (ClipboardManager) 
                getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        
        android.content.ClipData clip = android.content.ClipData.newPlainText(
                "Summary", summary.getSummary());
        clipboard.setPrimaryClip(clip);
        
        showToast("Summary copied to clipboard!");
    }
    
    @Override
    public void onMoreOptions(SummaryItem summary, View view) {
        PopupMenu menu = new PopupMenu(getContext(), view);
        menu.getMenuInflater().inflate(R.menu.menu_summary_item, menu.getMenu());
        
        menu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_share) {
                shareSummary(summary);
                return true;
            } else if (itemId == R.id.action_regenerate) {
                generateSummary(summary);
                showToast("Regenerating summary...");
                return true;
            } else if (itemId == R.id.action_view_original) {
                showToast("Opening original note...");
                return true;
            }
            return false;
        });
        
        menu.show();
    }
    
    private void shareSummary(SummaryItem summary) {
        android.content.Intent shareIntent = new android.content.Intent();
        shareIntent.setAction(android.content.Intent.ACTION_SEND);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, 
                "📝 " + summary.getTitle() + "\n\n" + summary.getSummary());
        shareIntent.setType("text/plain");
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Summary"));
    }
    
    @Override
    public void onSummaryClicked(SummaryItem summary) {
        showToast("Summary: " + summary.getTitle());
        // TODO: Open detailed view or full summary view
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh summaries when fragment resumes
        if (summaryList.isEmpty()) {
            loadSummariesFromDatabase();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        backgroundExecutor.shutdownNow();
    }
}