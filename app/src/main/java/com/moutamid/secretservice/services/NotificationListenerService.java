package com.moutamid.secretservice.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.models.MessageModel;
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

    private static final long TIME_THRESHOLD = 5000; // 5 seconds in milliseconds
    private String lastNotificationKey = null;
    private String lastMessage = null;
    private long lastNotificationTime = 0;
    Context context;
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_PACK = "EXTRA_PACK";
    public static final String ACTION_REPLY = "ACTION_REPLY";
    public static final String ACTION_READ = "ACTION_READ";
    private static final String CHANNEL_ID = "CHANNEL_ID";
    public static Map<String, Action> replyActions = new HashMap<>();
    private Set<String> processedNotificationKeys = new HashSet<>();
    int i = 0;
    private boolean check = false;

    @Override
    public void onCreate() {
        super.onCreate();
//        context = this;
    }

    public NotificationListenerService() {
        super();
        Log.d(TAG, "NotificationListenerService: Constructor");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        super.onNotificationRemoved(sbn, rankingMap, reason);
        Log.d(TAG, "onNotificationRemoved: ");
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "onListenerConnected: ");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "onListenerDisconnected: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        context = getApplicationContext();
        // Check if the notification is from WhatsApp
        String notificationKey = sbn.getKey();
        Notification notification = sbn.getNotification();
        Log.d(TAG, "lastNotificationKey\t\t" + lastNotificationKey);
        String packageName = sbn.getPackageName();

        if (isAllowedPlatform(packageName)) {
            Bundle extras = sbn.getNotification().extras;
            String sender = extras.getString("android.title");
            String message = extras.getString("android.text");

            if (message.equals("Messages is doing work in the background")) {
                return;
            }

            String currentNotificationKey = sender + message;

            long currentTime = System.currentTimeMillis();

            if (lastNotificationKey == null || !lastNotificationKey.equals(currentNotificationKey)) {
                Log.d(TAG, "First Time");
                check = true;
            }
            if (check) {
                if (currentTime - lastNotificationTime >= TIME_THRESHOLD) {
                    Log.d(TAG, "5 sec Time");
                    if (!TextUtils.isEmpty(message) && !message.equals(lastMessage)) {
                        Log.d(TAG, "different Time");
                        handleAction(sbn);
                        lastMessage = message;
                    }
                }
            }

            lastNotificationKey = currentNotificationKey;
            lastNotificationTime = currentTime;

        }
        /*

        if (isAllowedPlatform(packageName)) {
            Log.d(TAG, "isAllowedPlatform");
            if (currentNotificationKey != null) {
                long currentTime = System.currentTimeMillis();
                Log.d(TAG, "currentTime\t\t" + currentTime);
                Log.d(TAG, "TIME_THRESHOLD\t\t" + TIME_THRESHOLD);
                Log.d(TAG, "currentTime\t\t" + (currentTime - lastNotificationTime));
                Log.d(TAG, "BOOL\t\t" + (currentTime - lastNotificationTime >= TIME_THRESHOLD));

                // Handle the first message or when the sender changes
                if (lastNotificationKey != null && lastNotificationKey.equals(currentNotificationKey) && currentTime - lastNotificationTime >= TIME_THRESHOLD) {
                    Log.d(TAG, "onNotificationPosted");
                    handleAction(sbn);
                }

                lastNotificationKey = currentNotificationKey;
                lastNotificationTime = currentTime;
            }
        }
        */
    }

    private void handleAction(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if (Stash.getBoolean(Constants.IS_ON, false)) {
            Log.d(TAG, "onNotificationPosted 22");
            Log.d(TAG, "onNotificationPosted getPackageName  " + sbn.getPackageName());
            if (sbn.getPackageName().equals("com.whatsapp")) {
                Log.d(TAG, "onNotificationPosted whatsapp");
                String name = sbn.getNotification().extras.getString("android.title");
                if (getHour(name, Constants.WHATSAPP)) {
                    sendMessageToContact(notification, sbn.getNotification().extras, sbn.getPackageName(), sbn.getKey());
                }
            } else if (sbn.getPackageName().equals("org.telegram.messenger")) {
                Log.d(TAG, "onNotificationPosted telegram");
                String name = sbn.getNotification().extras.getString("android.title");
                if (getHour(name, Constants.TELEGRAM)) {
                    sendMessageToContact(notification, sbn.getNotification().extras, sbn.getPackageName(), sbn.getKey());
                }
            } else if (sbn.getPackageName().equals("com.skype.raider")) {
                Log.d(TAG, "onNotificationPosted skype");
                String name = sbn.getNotification().extras.getString("android.title");
                if (getHour(name, Constants.SKYPE)) {
                    sendMessageToContact(notification, sbn.getNotification().extras, sbn.getPackageName(), sbn.getKey());
                }
            } else if (sbn.getPackageName().equals("com.google.android.apps.messaging")) {
                Log.d(TAG, "onNotificationPosted google");
                String name = sbn.getNotification().extras.getString("android.title");
                Log.d(TAG, "onNotificationPosted c  " + getHour(name, Constants.SMS));
                if (getHour(name, Constants.SMS)) {
                    sendMessageToContact(notification, sbn.getNotification().extras, sbn.getPackageName(), sbn.getKey());
                }
            }
        }
    }

    public static String getDefaultSmsAppPackageName(Context context) {
        return Telephony.Sms.getDefaultSmsPackage(context);
    }

    private boolean isAllowedPlatform(String packageName) {
//        return packageName.equals("com.whatsapp") || packageName.equals("org.telegram.messenger") || packageName.equals("com.skype.raider") || packageName.equals(getDefaultSmsAppPackageName(context));
        return packageName.equals("com.whatsapp") || packageName.equals("org.telegram.messenger") || packageName.equals("com.skype.raider") || packageName.equals("com.google.android.apps.messaging");
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
            return false;
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
        Log.d(TAG, "isValid   " + isValid);

        if (!check) {
            return false;
        }

        Log.d(TAG, "check   " + (currentHour >= startTimeHour && currentHour <= endTimeHour));

        return isValid;
    }

    private void sendMessageToContact(Notification notification, Bundle extras, String pack, String key) {

        String title = extras.getString("android.title");
        String notifMessage = extras.getString("android.text");
        String message = Stash.getString(Constants.MESSAGE, "N/A");
        String keywordReply = "";

        Log.d(TAG, "message \t " + message);
        Log.d(TAG, "notifMessage \t " + notifMessage);
        if (notifMessage == null) {
            notifMessage = "";
        }

        ArrayList<MessageModel> keywordList = Stash.getArrayList(Constants.KEYWORDS_MESSAGE, MessageModel.class);
        for (MessageModel keyword : keywordList) {
            if (notifMessage.contains(keyword.getKeyword())) {
                keywordReply += keyword.getMsg() + "\n\n";
            }
        }

        Log.d(TAG, "message \t " + message);
        Log.d(TAG, "keywordReply \t " + keywordReply);

        if (!keywordReply.isEmpty()) {
            message = keywordReply;
        }

        Action action = NotificationUtils.getQuickReplyAction(notification, "com.moutamid.secretservice");
        if (action != null && replyActions != null)
            replyActions.put(title, action);

        Action action1 = replyActions.get(title);
        if (action1 != null)
            action1.sendReply(context, message);

        cancelNotification(key);
        Log.d(TAG, "cancelNotification \t ");

        Log.d(TAG, "i \t " + i);
        i++;
        Log.d(TAG, "i2 \t " + i);

        if (i >= 2) {
            i = 0;


            try {
                Log.d(TAG, "i3 \t " + i);
                Log.d(TAG, "TRYYY \t ");
                String url = Constants.API_PROCESSING_STAT_SMS + "?token=" + Stash.getString(Constants.TOKEN);
                Log.d(TAG, "url \t " + url);
                /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);*/

                Constants.openURL();

            } catch (ActivityNotFoundException ae) {
                ae.printStackTrace();
                Log.d(TAG, "ActivityNotFoundException \t " + ae.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception \t " + e.getMessage());
            }

        }

        Log.d(TAG, "Sending a message to " + title + ": " + message);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        // Handle notification removal if needed
    }
}
