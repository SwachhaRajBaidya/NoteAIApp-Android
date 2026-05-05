package com.example.quickmeetjava;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    ImageButton btnOptions, btnSearch;
    Button btnAcceptPost;
    List<MyItem> list = new ArrayList<>();
    CustomPostAdapter adapter;
    RecyclerView recyclerView;

    EditText editTextSearch;

    MySqliteHelper mySqliteHelper;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Collections.sort(list, Comparator.comparing(MyItem::getHeading, String.CASE_INSENSITIVE_ORDER));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
//        View viewDialog = inflater.inflate(R.layout.layout_add_post_dialog, container, false);

        mySqliteHelper = new MySqliteHelper(getContext());

        adapter = new CustomPostAdapter(list, this.getContext(), mySqliteHelper);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return false;
            }
            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }
            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        // Add scroll listener to show bottom nav when scrolled to top or when scrolling after items removed
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Hide search input when scrolling starts (if search is empty)
                if (editTextSearch.getText().toString().isEmpty()){
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                        editTextSearch.setVisibility(View.INVISIBLE);
                        editTextSearch.setText(null);
                        if (adapter != null) {
                            adapter.updateList(list);
                        }
                    }
                }

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
        });

        // Add touch listener to show bottom nav on any touch when it's hidden
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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

        btnOptions = view.findViewById(R.id.btnOptions);
        btnSearch = view.findViewById(R.id.btnSearch);
        editTextSearch = view.findViewById(R.id.editTextPost);


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(list, Comparator.comparing(MyItem::getHeading, String.CASE_INSENSITIVE_ORDER));
                editTextSearch.setVisibility(View.VISIBLE);
            }
        });
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), btnOptions);
                popupMenu.getMenuInflater().inflate(R.menu.option_menu,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(getContext(), item.getTitle() + " Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                popupMenu.show();
            }
        });


        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(editTextSearch.getVisibility() == View.VISIBLE) {
                    filter(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load posts after view is created
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (adapter != null && list.isEmpty()) {
                mainActivity.loadPosts(adapter, list);
            }
        }
    }
//    void filter(String text){
//        List<MyItem> temp = new ArrayList<>();
//        for(MyItem d : list){
//            if(d.getHeading().toLowerCase().contains(text.toLowerCase())){
//                temp.add(d);
//            }
//        }
//        adapter.updateList(temp);
//    }

    void filter(String text) {
        List<MyItem> temp = new ArrayList<>();
        if (text.isEmpty()) {
            temp.addAll(list); // Show all if no text
        } else {
            // Find insertion point
            int index = Collections.binarySearch(list, new MyItem("", null, 0, "", "", text),
                    Comparator.comparing(MyItem::getHeading, String.CASE_INSENSITIVE_ORDER));

            int start = (index >= 0) ? index : ~index; // If not found, ~index is insertion point

            // Scan forward from start
            for (int i = start; i < list.size(); i++) {
                if (list.get(i).getHeading().toLowerCase().startsWith(text.toLowerCase())) {
                    temp.add(list.get(i));
                } else if (list.get(i).getHeading().toLowerCase().compareTo(text.toLowerCase()) > 0) {
                    break; // Stop if past alphabetical range
                }
            }
            // Optionally scan backward if needed
            for (int i = start - 1; i >= 0; i--) {
                if (list.get(i).getHeading().toLowerCase().startsWith(text.toLowerCase())) {
                    temp.add(0, list.get(i));
                } else {
                    break;
                }
            }
        }
        adapter.updateList(temp);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}