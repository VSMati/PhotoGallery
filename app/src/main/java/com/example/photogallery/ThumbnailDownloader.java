package com.example.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean mHasQuit = false;
    private Handler mRequestHandler; //will post download requests to ThumbnailDownloader's background thread
    private final ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    //to store and retrieve the URL associated with the request
    private final Handler mResponseHandler; //will hold handler from the main thread
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    //listener will be used when the image is downloaded

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> thumbnailDownloadListener) {
        mThumbnailDownloadListener = thumbnailDownloadListener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                    Log.i(TAG, "handleMessage: got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url){
        Log.i(TAG, "queueThumbnail: got a URL "+url);

        if (url == null){
            mRequestMap.remove(target);
        }else{
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();
        }
    }

    private void handleRequest(final T target){
        try {
            final String url = mRequestMap.get(target);
            final Bitmap bitmap;
            PhotoCache photoCache = new PhotoCache(getCacheSize());

            if (url != null){
                if (photoCache.getBitmapFromMemory(url) == null){
                    byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                    bitmap = BitmapFactory
                            .decodeByteArray(bitmapBytes,0, bitmapBytes.length);
                    photoCache.setBitmapToMemory(url,bitmap);
                    Log.i(TAG, "handleRequest: bitmap created");
                }else{
                    bitmap = photoCache.getBitmapFromMemory(url);
                    Log.i(TAG, "handleRequest: GOT bitmap from memory");
                }
                mResponseHandler.post(() -> {
                    if (!Objects.equals(mRequestMap.get(target), url) || mHasQuit){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    private int getCacheSize(){
        return (int) (Runtime.getRuntime().maxMemory() / 1024)/4;
    }
}
