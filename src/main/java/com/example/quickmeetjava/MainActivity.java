package com.example.quickmeetjava;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    MySqliteHelper mySqliteHelper;
    Intent resultData;
    Calendar calendar = Calendar.getInstance();
    int mYear = calendar.get(Calendar.YEAR);
    int mMonth = calendar.get(Calendar.MONTH);
    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();

                        resultData = data;

                        Toast.makeText(MainActivity.this, "Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
    public static int GET_FROM_GALLERY = 3;
    Dialog dialog;
    ActivityMainBinding binding;
    HomeFragment homeFragment = new HomeFragment();
    Button btnAcceptPost;
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
                    replaceFragment(homeFragment);
                }else if(item.getItemId() == R.id.profile){
                    replaceFragment(new ProfileFragment());
                }else if(item.getItemId() == R.id.message){
                    replaceFragment(new MessageFragment());
                }
            return true;
        });

        btnPostAdd = findViewById(R.id.btnAddPost);

        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.layout_add_post_dialog);

        btnAcceptPost = dialog.findViewById(R.id.btnAcceptPost);

        btnPostAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeFragment.editTextSearch.setVisibility(View.INVISIBLE);
                homeFragment.editTextSearch.setText(null);
                homeFragment.adapter.updateList(homeFragment.list);

                showPostAddDialog();
            }
        });

        mySqliteHelper = new MySqliteHelper(MainActivity.this);
        loadPosts(homeFragment.adapter, homeFragment.list);
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void showPostAddDialog(){
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.layout_add_post_dialog);
        Button buttonOK = dialog.findViewById(R.id.btnAcceptPost);
        Button buttonUpload = dialog.findViewById(R.id.btnUploadImage);

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPost(homeFragment.recyclerView, homeFragment.adapter, homeFragment.list, dialog, resultData);
                resultData = null;
                Toast.makeText(MainActivity.this, "Post Added", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                someActivityResultLauncher.launch(intent);
            }
        });
        dialog.show();
    }

    private Bitmap uriToBitmap(Uri uri){
        Bitmap bitmap = null;

        try{
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        }catch (IOException ex){
            ex.printStackTrace();
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
        return bitmap;
    }
    private void addPost(RecyclerView rv, CustomPostAdapter customAdapter, List<MyItem> list, Dialog postDialog, Intent resultSet){
        EditText editText = postDialog.findViewById(R.id.editTextPost);
        MyItem item;
        String date = mDay +"-"+mMonth+"-"+ mYear;
        String randId = mySqliteHelper.randomString(10);

        if(resultSet != null){
//            String base64Image = "";
//            Bitmap src = uriToBitmap(resultSet.getData());
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            src.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
//            }
//
//            item = new MyItem(editText.getText().toString(), base64Image);
//            mySqliteHelper.insertPost("Profile", editText.getText().toString(), base64Image);


            Uri imageUri = resultSet.getData();
            getContentResolver().takePersistableUriPermission(imageUri, (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));


            item = new MyItem(editText.getText().toString(), imageUri, 0, date, randId);
            mySqliteHelper.insertPost(date, editText.getText().toString(), resultSet.getData().toString(), 0, randId);

//            item = new MyItem(editText.getText().toString(), baos.toByteArray());
//            mySqliteHelper.insertPost("Profile", editText.getText().toString(), baos.toByteArray());
        }else{
            item = new MyItem(editText.getText().toString(), null, 0, date, randId);
            mySqliteHelper.insertPost(date, editText.getText().toString(), null, 0, randId);
        }

        list.add(item);
        customAdapter.notifyItemInserted(list.size());
        rv.scrollToPosition(list.size()-1);
    }

    private void loadPosts(CustomPostAdapter customAdapter, List<MyItem> list){
        Cursor cursor =  mySqliteHelper.selectPost();

        while (cursor.moveToNext()){
            String date = cursor.getString(1);
            String post_text = cursor.getString(2);
            String imageBitmap = cursor.getString(3);
            int heart = cursor.getInt(4);
            String randomId = cursor.getString(5);


            Uri imageUri;
            if(imageBitmap != null){
                imageUri = Uri.parse(imageBitmap);
                getContentResolver().takePersistableUriPermission(imageUri, (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
            }else{
                imageUri = null;
            }

            MyItem item = new MyItem(post_text, imageUri, heart, date, randomId);
            list.add(item);
        }
    }
}