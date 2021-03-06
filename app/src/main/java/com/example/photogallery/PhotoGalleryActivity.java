package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class PhotoGalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        FragmentManager fm = getSupportFragmentManager();
        PhotoGalleryFragment pgf = new PhotoGalleryFragment();
        fm.beginTransaction().add(R.id.fragment_container,pgf).commit();
    }

    public static Intent newIntent(Context context){
        return new Intent(context, PhotoGalleryActivity.class);
    }
}