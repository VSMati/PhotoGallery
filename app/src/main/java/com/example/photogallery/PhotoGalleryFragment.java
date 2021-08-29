package com.example.photogallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class PhotoGalleryFragment extends VisibleFragment {
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private static final String TAG = "PhotoGalleryFragment";
    private List<GalleryItem> mItems = new ArrayList<>();
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    int pageNum = 1;

    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @org.jetbrains.annotations.NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mRecyclerView = v.findViewById(R.id.recycler_photo);
        mLayoutManager = new GridLayoutManager(getActivity(),3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setupAdapter();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0){
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                    if ((visibleItemCount+pastVisiblesItems)>=totalItemCount){
                        pageNum++;
                        Log.i(TAG, "onScrolled: changing page");
                        updateItems();
                        setupAdapter();
                        }
                    }
            }
        });

        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            //todo: later add code here to adjust rows number (need im size)
            //look here:
            // https://stackoverflow.com/questions/13132832/how-does-grid-view-change-column-number-runtime
        });

        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        updateItems();

        Intent service = PollService.newIntent(getContext());
        requireActivity().startService(service);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener((target, thumbnail) -> {
            Drawable drawable = new BitmapDrawable(getResources(),thumbnail);
            target.bindDrawable(drawable);
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "onCreate: background thread started");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery,menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setStoredQuery(getActivity(),query);
                hideKeyboard(requireContext(),requireView());
                updateItems();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(v -> {
            String query = QueryPreferences.getStoredQuery(getActivity());
            searchView.setQuery(query,false);
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        }else{
            toggleItem.setTitle(R.string.start_polling);
        }
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.menu_item_clear){
            QueryPreferences.setStoredQuery(getActivity(),null);
            updateItems();
            return true;
        }
        if (id == R.id.menu_item_toggle_polling){
            boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getContext());
            PollService.setServiceAlarm(requireActivity(),shouldStartAlarm);
            requireActivity().invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemTasks(query).execute();
    }

    @SuppressWarnings("rawtypes")
    private class PhotoAdapter extends RecyclerView.Adapter{
        private final List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = getLayoutInflater();
            View v = inflater.inflate(R.layout.list_item_gallery,parent,false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            @SuppressLint("UseCompatLoadingForDrawables")
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            ((PhotoHolder) holder).bindDrawable(placeholder);
            ((PhotoHolder) holder).bindGalleryItem(galleryItem);
            mThumbnailDownloader.queueThumbnail(((PhotoHolder) holder), galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        private final ImageView mImage;
        private GalleryItem mGalleryItem;

        public PhotoHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.item_image_view);
            mImage.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable){
            mImage.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem){
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
            //todo:if isHttpUri false - start intent
            Uri webPage = mGalleryItem.getPhotoPageUri();
            Intent i;
            if (isHttpUri(webPage)){
                i = PhotoPageActivity.newIntent(getContext(),webPage);
            }else{
                i = new Intent(Intent.ACTION_VIEW,mGalleryItem.getPhotoPageUri());
            }
            startActivity(i);
        }
    }

    private class FetchItemTasks extends AsyncTask<String,Void,List<GalleryItem>>{
        private final String mQuery;

        public FetchItemTasks(String query) {
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(String... strings) {
            if (mQuery == null){
                return new FlickrFetchr().fetchRecentPhotos(pageNum);
            } else{
                return new FlickrFetchr().searchPhotos(mQuery,pageNum);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }

    private void setupAdapter(){
        if (isAdded()){
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "onDestroy: background thread is destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
        //if view gets destroyed, queue gets cleared up
    }

    private void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        view.clearFocus();
    }

    private boolean isHttpUri(Uri uri){
        String url = uri.toString();
        return URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url);
    }


}
