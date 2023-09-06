package com.moutamid.secretservice.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Action;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.NotificationUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NotificationListenerService extends android.service.notification.NotificationListenerService {

    private static final String TAG = "RECIEVER123";
    Context context;
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_PACK = "EXTRA_PACK";
    public static final String ACTION_REPLY = "ACTION_REPLY";
    public static final String ACTION_READ = "ACTION_READ";
    private static final String CHANNEL_ID = "CHANNEL_ID";
    public static Map<String, Action> replyActions = new HashMap<>();
    private Set<String> processedNotificationKeys = new HashSet<>();

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
        String notificationKey = sbn.getKey();
        Log.d(TAG, "onNotificationPosted");
        if (Stash.getBoolean(Constants.IS_ON, false)) {
            Log.d(TAG, "onNotificationPosted 22");
            if (sbn.getPackageName().equals("com.whatsapp")) {
                String name = sbn.getNotification().extras.getString("android.title");
                if (getHour(name, Constants.WHATSAPP)) {
                    sendMessageToContact(notification, sbn.getNotification().extras, sbn.getPackageName(), sbn.getKey());
                }
            } else if (sbn.getPackageName().equals("org.telegram.messenger")) {
                String name = sbn.getNotification().extras.getString("android.title");
                if (getHour(name, Constants.TELEGRAM)) {
                    sendMessageToContact(notification, sbn.getNotification().extras, sbn.getPackageName(), sbn.getKey());
                }
            } else if (sbn.getPackageName().equals("com.skype.raider")) {
                String name = sbn.getNotification().extras.getString("android.title");
                if (getHour(name, Constants.SKYPE)) {
                    sendMessageToContact(notification, sbn.getNotification().extras, sbn.getPackageName(), sbn.getKey());
                }
            }
        }
    }

    private boolean getHour(String name, String source) {

        int startTimeHour = 0;
        int endTimeHour = 0;
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
        boolean isValid = false;

        Log.d(TAG, "startTimeHour   " + startTimeHour + "\t\tendTimeHour    " + endTimeHour);

        if (Stash.getInt(Constants.TIME, 3) == 0) {
            startTimeHour = Integer.parseInt(Constants.getFormattedHours(Stash.getLong(Constants.FROM_TIME, 0)));
            endTimeHour = Integer.parseInt(Constants.getFormattedHours(Stash.getLong(Constants.TO_TIME, 0)));
            isValid = currentHour >= startTimeHour && currentHour <= endTimeHour;
        } else if (Stash.getInt(Constants.TIME, 3) == 1) {

            long startDateMillis = Stash.getLong(Constants.START_DAY, 0);
            long endDateMillis = Stash.getLong(Constants.END_DAY, 0);
            long startTimeMillis = Stash.getLong(Constants.START_DAY_TIME, 0);
            long endTimeMillis = Stash.getLong(Constants.END_DAY_TIME, 0);

            Calendar startDateTime = Calendar.getInstance();
            startDateTime.setTimeInMillis(startDateMillis);
            startDateTime.set(Calendar.HOUR_OF_DAY, (int) (startTimeMillis / (60 * 60 * 1000)));
            startDateTime.set(Calendar.MINUTE, (int) ((startTimeMillis / (60 * 1000)) % 60));

            Calendar endDateTime = Calendar.getInstance();
            endDateTime.setTimeInMillis(endDateMillis);
            endDateTime.set(Calendar.HOUR_OF_DAY, (int) (endTimeMillis / (60 * 60 * 1000)));
            endDateTime.set(Calendar.MINUTE, (int) ((endTimeMillis / (60 * 1000)) % 60));

            isValid = calendar.after(startDateTime) && calendar.before(endDateTime);

        } else if (Stash.getInt(Constants.TIME, 3) == 2) {
            String[] daysOfWeek = {"", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            String currentDayName = daysOfWeek[currentDay];

            long currentTimeMillis = System.currentTimeMillis();

            long startTimeMillis = Stash.getLong(Constants.START_DAY, 0);
            long endTimeMillis = Stash.getLong(Constants.START_DAY, 0);
            ArrayList<String> allowedDays = Stash.getArrayList(Constants.WEEK_DAYS, String.class);

            boolean isAllowedDay = false;
            for (String day : allowedDays) {
                if (day.equalsIgnoreCase(currentDayName)) {
                    isAllowedDay = true;
                    break;
                }
            }
            boolean isWithinTimeRange = currentTimeMillis >= startTimeMillis && currentTimeMillis <= endTimeMillis;

            isValid = isAllowedDay && isWithinTimeRange;
        } else {
            return isValid;
        }


        Log.d(TAG, "startTimeHour   " + startTimeHour + "\t\tendTimeHour    " + endTimeHour);


        Log.d(TAG, "currentHour   " + currentHour);
        ArrayList<ContactModel> list = Stash.getArrayList(Constants.EXCLUDE_CONTACTS, ContactModel.class);
        for (ContactModel model : list) {
            if (model.getContactName().contains(name)) {
                return false;
            }
        }
        boolean check = false;
        String[] communication_channel = Stash.getString(Constants.Communication_Channel, "").split(", ");
        for (String channel : communication_channel) {
            if (channel.equals(source)) {
                check = true;
            }
        }


        Log.d(TAG, "check   " + check);

        if (!check) {
            return false;
        }

        Log.d(TAG, "check   " + (currentHour >= startTimeHour && currentHour <= endTimeHour));

        return isValid;
    }

    private void sendMessageToContact(Notification notification, Bundle extras, String pack, String key) {

        String title = extras.getString("android.title");
        String message = Stash.getString(Constants.MESSAGE, "N/A");

        Action action = NotificationUtils.getQuickReplyAction(notification, "com.moutamid.secretservice"); //Issue
        if (action != null && replyActions != null)
            replyActions.put(title, action);

        Action action1 = replyActions.get(title);
        if (action1 != null)
            action1.sendReply(context, message);

        cancelNotification(key);

        Log.d(TAG, "Sending a message to " + title + ": " + message);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        // Handle notification removal if needed
    }
}
