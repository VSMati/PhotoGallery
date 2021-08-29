package com.example.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.fragment.app.Fragment;

public abstract class VisibleFragment extends Fragment {
    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        requireActivity().registerReceiver(mOnShowNotification,filter, PollService.PERM_PRIVATE,null);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().unregisterReceiver(mOnShowNotification);
    }

    private final BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: cancelling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
}
