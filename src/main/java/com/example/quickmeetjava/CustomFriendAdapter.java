package com.example.quickmeetjava;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickmeetjava.R;

import org.w3c.dom.Text;

import java.util.List;

public class CustomFriendAdapter extends RecyclerView.Adapter<CustomFriendAdapter.MyViewHolder> {
    private List<Friend> itemList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ImageView imageView;
        public TextView textViewFriend;
        public TextView textLatestMessage;

        public MyViewHolder(View v) {
            super(v);
            cardView = v.findViewById(R.id.card_friend);
            imageView = v.findViewById(R.id.imgRecyclerFriend);
            textViewFriend = v.findViewById(R.id.tvRecyclerFirend);
            textLatestMessage = v.findViewById(R.id.tvLatestMessage);
        }
    }

    public CustomFriendAdapter(List<Friend> itemList) {
        this.itemList = itemList;
    }

    @Override
    public CustomFriendAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recycler_friend, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.textViewFriend.setText(itemList.get(position).getUsername());
        holder.textLatestMessage.setText(itemList.get(position).getLatestMessage());

        if(itemList.get(position).getProfilePic() != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(itemList.get(position).getProfilePic(), 0,  itemList.get(position).getProfilePic().length);
            holder.imageView.setImageBitmap(bitmap);
        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}