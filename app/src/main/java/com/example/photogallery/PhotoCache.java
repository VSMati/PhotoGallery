package com.example.photogallery;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class PhotoCache extends LruCache<String, Bitmap> {

    public PhotoCache(int maxSize) {
        super(maxSize);
    }

    public Bitmap getBitmapFromMemory(String url){
        return this.get(url);
    }

    public void setBitmapToMemory(String url, Bitmap drawable) {
        if (getBitmapFromMemory(url) == null) {
            this.put(url, drawable);
            Log.d("TEST", url + " added to cache");
        }
    }
}
