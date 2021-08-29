package com.example.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class PhotoPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_page);

        FragmentManager fm = getSupportFragmentManager();
        PhotoPageFragment fragment = PhotoPageFragment.newInstance(getIntent().getData());
        fm.beginTransaction().add(R.id.web_fragment_container,fragment).commit();
    }

    public static Intent newIntent(Context context, Uri pageUri){
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(pageUri);

        return i;
    }
}
