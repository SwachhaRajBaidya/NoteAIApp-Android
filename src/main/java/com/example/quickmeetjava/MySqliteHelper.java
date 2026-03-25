package com.example.quickmeetjava;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.Random;

public class MySqliteHelper extends SQLiteOpenHelper {
    private static String DATABASE_NAME="posts";
    private static int DATABASE_VERSION=1;

    public MySqliteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE posts(id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, post_text TEXT, image TEXT, heart INTEGER, randomId TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String upgradeQuery = "DROP TABLE IF EXISTS posts";
        db.execSQL(upgradeQuery);
    }
    public void insertPost(String date, String post_text, String image, int heart, String randomId){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("post_text",post_text);
        contentValues.put("image",image);
        contentValues.put("heart",heart);
        contentValues.put("randomId",randomId);


        sqLiteDatabase.insert("posts",null,contentValues);
    }

    public Cursor selectPost(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM posts";
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        return cursor;
    }

    public Cursor selectPostRand(String randomId){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String selectQuery = "SELECT randomId FROM posts WHERE randomId = ?";
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        return cursor;
    }
    public Cursor selectId(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String selectQuery = "SELECT id FROM posts";
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        return cursor;
    }

    public void updatePost(int id,String date, String post_text, String image, int heart, String randomId){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("post_text", post_text);
        contentValues.put("image", image);
        contentValues.put("heart", heart);
        contentValues.put("randomId", randomId);

        sqLiteDatabase.update("posts", contentValues, "id=?", new String[]{String.valueOf(id)});
    }

    public void updateHeart(int id, int heart){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("heart", heart);

        sqLiteDatabase.update("posts", contentValues, "id=?", new String[]{String.valueOf(id)});
    }

    public void updatePost(String randomId, String post_text){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("post_text", post_text);

        sqLiteDatabase.update("posts", contentValues, "randomId=?", new String[]{String.valueOf(randomId)});
    }
    public void deletePost(String randomId){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete("posts", "randomId=?", new String[]{String.valueOf(randomId)});
    }
    public String randomString(int len) {
        final String DATA = "0123489ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz|!£$%&/=@#";
        Random RANDOM = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }
        return sb.toString();
    }
}
