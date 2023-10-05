package com.moutamid.secretservice.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.moutamid.secretservice.R;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingService";
    PendingIntent pendingIntent;
    NotificationManager mNotificationManager;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    public void onNewToken(String s) {
        super.onNewToken(s);
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, 0).edit();
        editor.putString("name", s);
        editor.apply();
        Log.d("MyFirebaseMessagingService", "onNewToken: " + s);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d("MyFirebaseMessagingService", "onDeletedMessages");
    }

    @Override
    public void onMessageSent(@NonNull String msgId) {
        super.onMessageSent(msgId);
        Log.d("MyFirebaseMessagingService", "onMessageSent");
    }

    @Override
    public void onSendError(@NonNull String msgId, @NonNull Exception exception) {
        super.onSendError(msgId, exception);
        Log.d("MyFirebaseMessagingService", "onSendError");
    }

    @Override
    public void handleIntent(Intent intent) {
//        super.handleIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Log.e(TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
            }
        }
        RemoteMessage remoteMessage = new RemoteMessage(bundle);
        onMessageReceived(remoteMessage);
        Log.d("MyFirebaseMessagingService", "handleIntent");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MyFirebaseMessagingService", "onDestroy");
    }

    @Override
    public boolean handleIntentOnMainThread(Intent intent) {
        Log.d("MyFirebaseMessagingService", "handleIntentOnMainThread");
        return false;
    }

    public MyFirebaseMessagingService() {
        super();
        Log.d("MyFirebaseMessagingService", "MyFirebaseMessagingService");
    }

    @Override
    protected Intent getStartCommandIntent(Intent originalIntent) {
        Log.d("MyFirebaseMessagingService", "getStartCommandIntent");
        return super.getStartCommandIntent(originalIntent);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("MyFirebaseMessagingService", "onMessageReceived");
        // Handle the received FCM message here
        if (remoteMessage != null) {
            if (remoteMessage.getNotification() != null) {

                Log.d("MyFirebaseMessagingService", "Message Received");
                Log.d("MyFirebaseMessagingService", "RemoteMessage " + remoteMessage.toString());

                // Notification message received
                String title = remoteMessage.getNotification().getTitle();
                String body = remoteMessage.getNotification().getBody();
                Uri link = remoteMessage.getNotification().getLink();
                // Handle the notification content as needed

                Log.d("MyFirebaseMessagingService", "title \t\t" + title);
                Log.d("MyFirebaseMessagingService", "body \t\t" + body);
                Log.d("MyFirebaseMessagingService", "link \t\t" + link.toString());

                Intent linkIntent = new Intent(Intent.ACTION_VIEW, link);
                pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, linkIntent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                int priority = remoteMessage.getPriority();

                Log.d("MyFirebaseMessagingService", "priority \t\t" + priority);

//            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(2));
//            r.play();
//            if (Build.VERSION.SDK_INT >= 28) {
//                r.setLooping(false);
//            }

                ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{100, 300, 300, 300}, -1);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "Your_channel_id");

                builder.setSmallIcon(R.mipmap.ic_launcher_round);
                builder.setContentTitle(title);
                builder.setContentText(body);
                builder.setContentIntent(pendingIntent);
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
                builder.setAutoCancel(true);
                builder.setPriority(priority);
                this.mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= 26) {
                    this.mNotificationManager.createNotificationChannel(new NotificationChannel("Your_channel_id", "Channel human readable title", NotificationManager.IMPORTANCE_HIGH));
                    builder.setChannelId("Your_channel_id");
                }
                this.mNotificationManager.notify(5, builder.build());

            } else {
                Log.d("MyFirebaseMessagingService", "message is null");
            }
        } else {
            Log.d("MyFirebaseMessagingService", "RemoteMessage is null");
        }
    }
}