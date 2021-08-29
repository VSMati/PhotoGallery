package com.example.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "8593f78b985da225aaf2c766fc1ff151";
    private static final String FETCH_RECENT_PHOTOS = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://www.flickr.com/services/rest/")
            .buildUpon()
                    .appendQueryParameter("api_key",API_KEY)
                    .appendQueryParameter("format","json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras","url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream is = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+
                        " : with " + urlSpec);
            }

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = is.read(buffer)) > 0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchRecentPhotos(int pageNum){
        String url = buildUrl(FETCH_RECENT_PHOTOS,null,pageNum);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> searchPhotos(String query, int pageNum){
        String url = buildUrl(SEARCH_METHOD,query,pageNum);
        return downloadGalleryItems(url);
    }

    public List<GalleryItem> downloadGalleryItems(String url){
        List<GalleryItem> list = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "fetchItems: Received items " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(list,jsonBody);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "fetchItems: Failed to fetch items" );
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "fetchItems: Failed to parse JSON");
        }
        return list;
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length();i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")){
                continue;
            }

            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            items.add(item);
        }
    }

    private String buildUrl(String method, String query, int pageNum){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("page",String.valueOf(pageNum))
                .appendQueryParameter("method",method);

        if (method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text",query);
        }

        return uriBuilder.toString();
    }
}
