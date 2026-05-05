package com.example.quickmeetjava;

import android.content.Intent;
import android.net.Uri;

import java.net.URI;
import java.util.List;

public class MyItem {
    private Uri image;
    private String text;
    private int heart;
    private String date;
    private String randomId;

    private String heading;

    public MyItem(String text, Uri image, int heart, String date, String randomId, String heading) {
        this.text = text;
        this.image = image;
        this.heart = heart;
        this.date = date;
        this.randomId = randomId;
        this.heading = heading;
    }

    public MyItem(String text){
        this.text = text;
    }
    public Uri getImage() {
        return image;
    }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int isHeart() { return heart; }
    public void setImage(Uri image) {
        this.image = image;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setHeart(int heart) { this.heart = heart; }
    public int getHeart() { return heart; }
    public String getRandomId() { return randomId; }
    public void setRandomId(String randomId) { this.randomId = randomId; }
    public String getHeading() { return heading; }
    public void setHeading(String heading) { this.heading = heading; }
}
