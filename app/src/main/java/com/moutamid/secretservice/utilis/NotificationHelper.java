package com.moutamid.secretservice.utilis;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.moutamid.secretservice.R;

import java.util.Random;

public class NotificationHelper extends ContextWrapper {

    private static final String TAG = "NotificationHelper";
    Context context;

    public NotificationHelper(Context base) {
        super(base);
        context = base;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    private String CHANNEL_NAME = "High priority channel";
    private String CHANNEL_ID = "com.example.notifications" + CHANNEL_NAME;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("this is the description of the channel.");
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);
    }

    public void sendHighPriorityNotification(String title, String body, String icon, String link, String priority) {

        Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        PendingIntent linkPendingIntent = PendingIntent.getActivity(this, 0, linkIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int prio = priority.equals("high") ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_LOW;

        Glide.with(this)
                .asBitmap()
                .load(icon)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        // Create a custom notification layout
                        RemoteViews customNotificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);

                        // Set the loaded image as the icon in the custom layout
                        customNotificationLayout.setImageViewBitmap(R.id.custom_notification_icon, resource);

                        // Set the title and body text
                        customNotificationLayout.setTextViewText(R.id.custom_notification_title, title);
                        customNotificationLayout.setTextViewText(R.id.custom_notification_body, body);

                        // Create the notification
                        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setPriority(prio)
                                .setContentIntent(linkPendingIntent)
                                .setCustomContentView(customNotificationLayout)
                                .setAutoCancel(true)
                                .build();

                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        NotificationManagerCompat.from(context).notify(new Random().nextInt(), notification);

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle any cleanup if needed
                    }
                });






    }

}
