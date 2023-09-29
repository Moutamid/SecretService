package com.moutamid.secretservice.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.moutamid.secretservice.services.MyService;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            Log.i("onReceive: ", "Context is null");
            return;
        }
//        context.getApplicationContext()
/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i("onReceive: ", "        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {");
            context.startForegroundService(new Intent(context, MyService.class));
        } else {
            Log.i("onReceive: ", "} else {");
            context.startService(new Intent(context, MyService.class));
        }*/
    }
}
