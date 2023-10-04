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
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.moutamid.secretservice.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
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
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle the received FCM message here
        if (remoteMessage.getNotification() != null) {

            Log.d("MyFirebaseMessagingService", "Message Received");

            // Notification message received
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            String link = remoteMessage.getNotification().getLink().toString();
            // Handle the notification content as needed

            Log.d("MyFirebaseMessagingService", "title \t\t" + title);
            Log.d("MyFirebaseMessagingService", "body \t\t" + body);
            Log.d("MyFirebaseMessagingService", "link \t\t" + link);


            pendingIntent = createPendingIntent(link);

            int priority = remoteMessage.getPriority();

            Log.d("MyFirebaseMessagingService", "priority \t\t" + priority);

            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(2));
            r.play();
            if (Build.VERSION.SDK_INT >= 28) {
                r.setLooping(false);
            }

            ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{100, 300, 300, 300}, -1);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Your_channel_id");

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
            this.mNotificationManager.notify(100, builder.build());

        }
    }

    private PendingIntent createPendingIntent(String link) {
        Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        return PendingIntent.getActivity(this, 0, linkIntent, PendingIntent.FLAG_IMMUTABLE);
    }
}