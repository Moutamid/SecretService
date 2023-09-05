package com.moutamid.secretservice.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.activities.ReplyActivity;
import com.moutamid.secretservice.receivers.NotificationActionReceiver;
import com.moutamid.secretservice.utilis.Action;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.NotificationUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NotificationListenerService extends android.service.notification.NotificationListenerService {

    private static final String TAG = "RECIEVER123";
    Context context;
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_PACK = "EXTRA_PACK";
    public static final String ACTION_REPLY = "ACTION_REPLY";
    public static final String ACTION_READ = "ACTION_READ";
    private static final String CHANNEL_ID = "CHANNEL_ID";
    public static Map<String, Action> replyActions = new HashMap<>();
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Notification notification = sbn.getNotification();
        // Check if the notification is from WhatsApp
        if (sbn.getPackageName().equals("com.whatsapp")) {
            // Check the time range (1 PM to 5 PM)
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (hour >= 13 && hour <= 20) {
                // You may want to exclude some contacts here
                // For example, if you want to exclude a contact with a specific name:
                String excludedContactName = "John Doe";
                if (!sbn.getNotification().extras.getString("android.title").equals(excludedContactName)) {
                    // Send a message to the sender
                    sendMessageToContact(notification, sbn.getNotification().extras, sbn.getPackageName());
                }
            }
        }
    }

    private void sendMessageToContact(Notification notification, Bundle extras, String pack) {
        // Implement the logic to send a message to the contact using WhatsApp here
        // You can use the WhatsApp API or simulate user actions to send a message
        // For demonstration purposes, we'll log a message here.
        String title = extras.getString("android.title");
        String message = Stash.getString(Constants.MESSAGE, "HEllo");
        long id = notification.when;
        int userId = title.hashCode();
        String text = Stash.getString(Constants.MESSAGE, "HEllo");

        Action action = NotificationUtils.getQuickReplyAction(notification, "com.moutamid.secretservice"); //Issue
        if (action != null && replyActions != null)
            replyActions.put(title, action);

        Action action1 = replyActions.get(title);
        if (action1 != null)
            action1.sendReply(context, message);

/*
        Intent replyIntent = new Intent(context, NotificationActionReceiver.class);
        replyIntent.setAction(ACTION_REPLY);
        replyIntent.putExtra(EXTRA_TITLE, title);
        replyIntent.putExtra(EXTRA_PACK, pack);

        int specificFlag = getSpecificFlag();

        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(
                context, userId, replyIntent, specificFlag);

        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_launcher_foreground,
                        "REPLY", replyPendingIntent)
                        .addRemoteInput(getRemoteInput(context))
                        .build();


        Intent resultIntent = ReplyActivity.getStartIntent(context, title, pack, true);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, specificFlag);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setGroup(title)
                .addAction(replyAction)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(notificationManager, NotificationManager.IMPORTANCE_LOW);

        Notification build = builder.build();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(userId, build);
*/


        Log.d(TAG, "Sending a message to " + title + ": " + message);
    }

    private static int getSpecificFlag() {
        int specificFlag;
        if (Build.VERSION.SDK_INT >= 31)
            specificFlag = PendingIntent.FLAG_MUTABLE;
        else specificFlag = PendingIntent.FLAG_UPDATE_CURRENT;
        return specificFlag;
    }

    @NotNull
    private static RemoteInput getRemoteInput(Context context) {
        String replyLabel = "REPLY";
        return new RemoteInput.Builder(ACTION_REPLY)
                .setLabel(replyLabel)
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private  void createNotificationChannel(NotificationManagerCompat notificationManager, int importance) {
        CharSequence name = "Channel Name";
        String description = "Channel Desc";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        // Handle notification removal if needed
    }
}
