package com.example.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isOn = PollService.isServiceAlarmOn(context);
        PollService.setServiceAlarm(context,isOn);
    }
}
