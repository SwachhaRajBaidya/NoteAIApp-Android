package com.example.quickmeetjava;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditNoteActivity extends AppCompatActivity {
    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String EXTRA_RANDOM_ID = "EXTRA_RANDOM_ID";
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_TEXT = "EXTRA_TEXT";
    public static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";
    public static final String EXTRA_HEART = "EXTRA_HEART";
    public static final String EXTRA_DATE = "EXTRA_DATE";

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextPost;
    private Button btnUploadImage;
    private Button btnSaveNote;
    private ImageView imagePreview;
    private Uri selectedImageUri;
    private MySqliteHelper mySqliteHelper;
    private boolean isEditMode;
    private String existingRandomId;
    private String existingDate;
    private int existingHeart;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageUri = uri;
                        imagePreview.setImageURI(uri);
                        imagePreview.setVisibility(View.VISIBLE);
                        takePersistablePermission(uri);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_note);

        mySqliteHelper = new MySqliteHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextPost = findViewById(R.id.editTextPost);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        imagePreview = findViewById(R.id.imagePreview);

        Intent intent = getIntent();
        if (intent != null && "EDIT".equals(intent.getStringExtra(EXTRA_MODE))) {
            isEditMode = true;
            existingRandomId = intent.getStringExtra(EXTRA_RANDOM_ID);
            existingDate = intent.getStringExtra(EXTRA_DATE);
            existingHeart = intent.getIntExtra(EXTRA_HEART, 0);
            editTextTitle.setText(intent.getStringExtra(EXTRA_TITLE));
            editTextPost.setText(intent.getStringExtra(EXTRA_TEXT));
            String imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI);
            if (imageUriString != null) {
                selectedImageUri = Uri.parse(imageUriString);
                imagePreview.setImageURI(selectedImageUri);
                imagePreview.setVisibility(View.VISIBLE);
                takePersistablePermission(selectedImageUri);
            }
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.edit_note_header));
            }
        } else {
            isEditMode = false;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.add_note_header));
            }
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        btnUploadImage.setOnClickListener(v -> {
            Intent intentPicker = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            intentPicker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentPicker.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            imagePickerLauncher.launch(intentPicker);
        });

        btnSaveNote.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = editTextTitle.getText() != null ? editTextTitle.getText().toString().trim() : "";
        String text = editTextPost.getText() != null ? editTextPost.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("Title is required");
            return;
        }

        String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;

        if (isEditMode && existingRandomId != null) {
            mySqliteHelper.updatePost(existingRandomId, existingDate, text, imageUriString, existingHeart, title);
        } else {
            String formattedDate = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
            String randId = mySqliteHelper.randomString(10);
            mySqliteHelper.insertPost(formattedDate, text, imageUriString, 0, randId, title);
        }

        setResult(RESULT_OK);
        finish();
    }

    private void takePersistablePermission(Uri uri) {
        try {
            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }
}
