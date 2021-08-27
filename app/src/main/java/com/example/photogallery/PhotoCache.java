package com.example.photogallery;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class PhotoCache extends LruCache<String, Bitmap> {
    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
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
