package com.example.quickmeetjava;

import android.content.Intent;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickmeetjava.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    MySqliteHelper mySqliteHelper;
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

    ActivityMainBinding binding;
    HomeFragment homeFragment = new HomeFragment();
    ProfileFragment profileFragment = new ProfileFragment();

    ActivityResultLauncher<Intent> noteActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (homeFragment != null && homeFragment.isAdded()) {
                        homeFragment.refreshPosts();
                    }
                }
            }
    );
    FloatingActionButton btnPostAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(homeFragment);
        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                if(item.getItemId() == R.id.home){
                    profileFragment.listFavourite.clear();
                    replaceFragment(homeFragment);
                }else if(item.getItemId() == R.id.profile){
                    profileFragment.listFavourite.clear();
                    replaceFragment(profileFragment);
                    if (profileFragment.adapterFavourite != null) {
                        loadPostsFavourite(profileFragment.adapterFavourite, profileFragment.listFavourite, homeFragment.list);
                    }
                }else if(item.getItemId() == R.id.message){
                    profileFragment.listFavourite.clear();
                    replaceFragment(new MessageFragment());
                }
            return true;
        });

        btnPostAdd = findViewById(R.id.btnAddPost);

        btnPostAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeFragment.editTextSearch.setVisibility(View.INVISIBLE);
                homeFragment.editTextSearch.setText(null);
                homeFragment.adapter.updateList(homeFragment.list);

                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                noteActivityLauncher.launch(intent);
            }
        });

        mySqliteHelper = new MySqliteHelper(MainActivity.this);
        // Note: loadPosts is now called in HomeFragment.onViewCreated after the adapter is initialized
        if (profileFragment.adapterFavourite != null) {
            loadPostsFavourite(profileFragment.adapterFavourite, profileFragment.listFavourite, homeFragment.list);
        }
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commitNow();
    }

    public void loadPosts(CustomPostAdapter customAdapter, List<MyItem> list){
        if (list != null) {
            list.clear();
        }
        Cursor cursor =  mySqliteHelper.selectPost();

        while (cursor.moveToNext()){
            String date = cursor.getString(1);
            String post_text = cursor.getString(2);
            String imageBitmap = cursor.getString(3);
            int heart = cursor.getInt(4);
            String randomId = cursor.getString(5);
            String title = cursor.getString(6);

            MyItem item;
            Uri imageUri;
            Uri uriPlaceholder = Uri.parse("android.resource://" + MainActivity.this.getPackageName() + "/drawable/" + R.drawable.icon_background);


            if(imageBitmap != null){
                imageUri = Uri.parse(imageBitmap);
                Log.d("imageUri", imageUri.toString());
                try {
                    getContentResolver().takePersistableUriPermission(imageUri,
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                } catch (SecurityException e) {
                    Log.w("URI_PERMISSION", "Could not take persistable permission", e);
                }
//                getContentResolver().takePersistableUriPermission(imageUri, (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                if(checkImageFileExists(imageUri)){
                    item = new MyItem(post_text, imageUri, heart, date, randomId, title);
                    list.add(item);
                }
            }else{
                imageUri = null;
                item = new MyItem(post_text, imageUri, heart, date, randomId, title);
                list.add(item);
            }

        }
        if (customAdapter != null) {
            customAdapter.notifyDataSetChanged();
        }
    }

    private void loadPostsFavourite(CustomPostAdapter customAdapter, List<MyItem> list, List<MyItem> list1){
        if (list != null) {
            list.clear();
        }
        Cursor cursor =  mySqliteHelper.selectPostFavourite();

        while (cursor.moveToNext()){
            String date = cursor.getString(1);
            String post_text = cursor.getString(2);
            String imageBitmap = cursor.getString(3);
            int heart = cursor.getInt(4);
            String randomId = cursor.getString(5);
            String title = cursor.getString(6);

            Uri imageUri;
            MyItem item;

            if(imageBitmap != null){
                imageUri = Uri.parse(imageBitmap);
                Log.d("imageUri", imageUri.toString());
                try {
                    getContentResolver().takePersistableUriPermission(imageUri,
                            (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                } catch (SecurityException e) {
                    Log.w("URI_PERMISSION", "Could not take persistable permission", e);
                }
//                getContentResolver().takePersistableUriPermission(imageUri, (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
                if(checkImageFileExists(imageUri)){
                    item = new MyItem(post_text, imageUri, heart, date, randomId, title);
                    list.add(item);
                }
            }else{
                imageUri = null;
                item = new MyItem(post_text, imageUri, heart, date, randomId, title);
                list.add(item);
            }

        }
        if (customAdapter != null) {
            customAdapter.notifyDataSetChanged();
        }
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        String path = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(index);
            }
            cursor.close();
        }
        // Fallback for newer Android versions or if path is null
        if (path == null || path.isEmpty()) {
            path = uri.getPath();
        }
        return path;
    }

//    public boolean checkImageFileExists(Uri uri) {
//        boolean exists = false;
//        InputStream inputStream = null;
//        try {
//            inputStream = getContentResolver().openInputStream(uri);
//            if (inputStream != null) {
//                exists = true;
//                inputStream.close(); // Close the stream if successful
//            }
//        } catch (Exception e) {
//            // Exception indicates file not found or access denied
//            e.printStackTrace();
//        }
//        return exists;
//    }

//    public boolean checkImageFileExists(Uri uri) {
//        // First, check if the URI exists in the database
//        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//        if (cursor == null || !cursor.moveToFirst()) {
//            if (cursor != null) cursor.close();
//            return false; // URI not found in database
//        }
//        cursor.close();
//
//        // Second, check if we can open a stream to the actual file
//        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
//            return inputStream != null;
//        } catch (Exception e) {
//            return false; // File not found or inaccessible
//        }
//    }

    public boolean checkImageFileExists(Uri uri) {
        // Check if the URI exists in the MediaStore database
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor == null || !cursor.moveToFirst()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        // Attempt to open a stream to the file, which is the definitive check
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            return inputStream != null;
        } catch (FileNotFoundException e) {
            // The file was deleted after the database query, or is otherwise inaccessible
            return false;
        } catch (Exception e) {
            // Handle other potential exceptions (e.g., security)
            e.printStackTrace();
            return false;
        }
    }
}