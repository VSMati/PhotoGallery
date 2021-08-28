package com.example.photogallery;

import android.content.Context;
import androidx.preference.*;

public class QueryPreferences {
    private static final String PREP_SEARCH_QUERY = "search_query";

    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREP_SEARCH_QUERY,null);
    }

    public static void setStoredQuery(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREP_SEARCH_QUERY,query)
                .apply();
    }
}
