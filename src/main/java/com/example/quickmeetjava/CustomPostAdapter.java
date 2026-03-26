package com.example.quickmeetjava;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickmeetjava.R;

import java.io.File;
import java.util.Base64;
import java.util.List;

public class CustomPostAdapter extends RecyclerView.Adapter<CustomPostAdapter.MyViewHolder> {
    private List<MyItem> itemList;
    private Context context;

    private MySqliteHelper mySqliteHelper;
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewDate;
        public ImageButton btnEdit;
        public CardView cardView;
        public TextView textView;
        public ImageButton btnPostLike;
        public ImageView imageView;
        public ImageButton btnDelete;
        public TextView textViewHeading;

        public MyViewHolder(View v) {
            super(v);
            textViewDate = v.findViewById(R.id.post_username);
            cardView = v.findViewById(R.id.card_post);
            textView = v.findViewById(R.id.post_title);
            imageView = v.findViewById(R.id.post_image);
            btnPostLike = v.findViewById(R.id.btnPostLike);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
            textViewHeading = v.findViewById(R.id.post_heading);
        }
    }

    public CustomPostAdapter(List<MyItem> itemList, Context context, MySqliteHelper mySqliteHelper) {
        this.itemList = itemList;
        this.context = context;
        this.mySqliteHelper = mySqliteHelper;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_post, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

//        holder.textView.setText(itemList.get(position).getText());
//        if(itemList.get(position).getImage() != null){
//            Bitmap bitmap = BitmapFactory.decodeByteArray(itemList.get(position).getImage(), 0,  itemList.get(position).getImage().length);
//            holder.imageView.setImageBitmap(bitmap);
//        }

        holder.textViewHeading.setText(itemList.get(position).getHeading());
        holder.textViewDate.setText(itemList.get(position).getDate());
        holder.btnPostLike.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int positions = holder.getAbsoluteAdapterPosition();
                if(positions == RecyclerView.NO_POSITION){
                    return;
                }
                for(int i = 0; i < itemList.size(); i++) {
                    String randomId = itemList.get(i).getRandomId();
                    if (randomId.equals(itemList.get(positions).getRandomId())) {
                        if (itemList.get(positions).isHeart() == 1) {
                            itemList.get(positions).setHeart(0);
                            holder.btnPostLike.setImageResource(R.drawable.heart);
                            mySqliteHelper.updateHeart(randomId, 0);
                        } else {
                            itemList.get(positions).setHeart(1);
                            holder.btnPostLike.setImageResource(R.drawable.heart_on);
                            mySqliteHelper.updateHeart(randomId, 1);
                        }
                    }
                }
            }
        });

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int positions = holder.getAbsoluteAdapterPosition();
                if(positions == RecyclerView.NO_POSITION){
                    return;
                }
                showAlertEdit(itemList.get(positions).getRandomId(), itemList.get(positions).getText());
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int positions = holder.getAbsoluteAdapterPosition();
                if(positions == RecyclerView.NO_POSITION){
                    return;
                }
                showAlertDelete(itemList.get(positions).getRandomId());
            }
        });

        switch (holder.getItemViewType()){
            case 0:
//              2 byte[] data = new byte[0];
//                holder.textView.setText(itemList.get(position).getText());
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    data = Base64.getDecoder().decode(itemList.get(position).getImage());
//                }
//
//                BitmapFactory.Options opt = new BitmapFactory.Options();
//                opt.inMutable = true;
//                opt.inPreferredConfig = Bitmap.Config.RGB_565;
//
//                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,  data.length, opt);

                holder.textView.setText(itemList.get(position).getText());

                if(context.getContentResolver().getType(itemList.get(position).getImage()) != null){
                    holder.imageView.setImageURI(itemList.get(position).getImage());
                }else{
                    holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
                }

                if(itemList.get(position).isHeart() == 1){
                    holder.btnPostLike.setImageResource(R.drawable.heart_on);
                }else{
                    holder.btnPostLike.setImageResource(R.drawable.heart);
                }
//             1   Bitmap bitmap = BitmapFactory.decodeByteArray(itemList.get(position).getImage(), 0,  itemList.get(position).getImage().length);
//                holder.imageView.setImageBitmap(bitmap);
                break;
            case 1:
                holder.textView.setText(itemList.get(position).getText());

                if(itemList.get(position).getImage() == null && holder.imageView != null) {
                    ViewGroup parent = (ViewGroup) holder.imageView.getParent();
                    if (parent != null) {
                        parent.removeView(holder.imageView);
                    }
                }

                if(itemList.get(position).isHeart() == 1){
                    holder.btnPostLike.setImageResource(R.drawable.heart_on);
                }else{
                    holder.btnPostLike.setImageResource(R.drawable.heart);
                }
                break;
        }

//        if(itemList.get(position).getImage() == null && holder.imageView != null){
//            ViewGroup parent = (ViewGroup) holder.imageView.getParent();
//
//            if(parent != null){
//                parent.removeView(holder.imageView);
//            }
//        }
    }

    @Override
    public int getItemViewType(int position) {
        if(itemList.get(position).getImage() != null){
            return 0;
        }

        if(itemList.get(position).getImage() == null){
            return 1;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    public void updateList(List<MyItem> list){
        itemList = list;
        notifyDataSetChanged();
    }

    public void updateHeart(int position, MyViewHolder holder) {


    }
    private boolean checkImageExists(Context context, Uri uri){
        ContentResolver cr = context.getContentResolver();
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = cr.query(uri, projection, null, null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                String filePath = cursor.getString(0);
                boolean exists = new File(filePath).exists();
                cursor.close();
                return exists;
            }
            cursor.close();
        }
        return false;
    }

    private void showAlertEdit(String pos, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog alertDialog = builder.create();
        builder.setTitle("Edit");
        builder.setMessage("Edit the note");

        EditText editText = new EditText(context);
        editText.setText(message);
        builder.setView(editText);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(int i = 0; i < itemList.size(); i++) {
                    String randomId = itemList.get(i).getRandomId();
                    if (randomId.equals(pos)) {
                        String text = editText.getText().toString();
                        itemList.get(i).setText(text);
                        mySqliteHelper.updatePost(pos, text);
                        notifyDataSetChanged();
                        alertDialog.dismiss();
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
            }
        });

        builder.show();
    }

    private void showAlertDelete(String pos){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog alertDialog = builder.create();
        builder.setTitle("Are you Sure?");
        builder.setMessage("If you want to continue, click ok button otherwise click cancel");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mySqliteHelper.deletePost(pos);

                int itemPos;

                for(int i = 0; i < itemList.size(); i++){
                    String randomId = itemList.get(i).getRandomId();
                    if(randomId.equals(pos)){
                        mySqliteHelper.deletePost(randomId);
                        itemList.remove(i);
                        notifyDataSetChanged();
                        break;
                    }
                }
                alertDialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
            }
        });

        builder.show();
    }
}