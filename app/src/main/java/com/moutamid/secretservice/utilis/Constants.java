package com.moutamid.secretservice.utilis;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

public class Constants {

    static Dialog dialog;
    public static final String DATE_FORMAT = "dd MMMM yyyy HH:mm";
    public static final String DAY_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String IS_ON = "IS_ON";
    public static final String IS_ALERT_ON = "IS_ALERT_ON";
    public static final String ALERT_CHECK = "ALERT_CHECK";
    public static final String ONE_TIME = "ONE_TIME";
    public static final String IS_TOKEN_VERIFY = "IS_TOKEN_VERIFY";
    public static final String TOKEN = "TOKEN";
    public static final String TIME = "TIME";
    public static final String SMS = "SMS";
    public static final String WEEK_DAYS = "WEEK_DAYS";
    public static final String SELECTED_TIME = "SELECTED_TIME";
    public static final String FROM_TIME = "FROM_TIME";
    public static final String FROM_WEEK = "FROM_WEEK";
    public static final String TO_TIME = "TO_TIME";
    public static final String TO_WEEK = "TO_WEEK";
    public static final String TELEGRAM = "TELEGRAM";
    public static final String SKYPE = "SKYPE";
    public static final String WHATSAPP = "WHATSAPP";
    public static final String MISSED_CALLS = "MISSED_CALLS";
    public static final String REFUSED_CALLS = "REFUSED_CALLS";
    public static final String START_DAY_TIME = "START_DAY_TIME";
    public static final String END_DAY_TIME = "END_DAY_TIME";
    public static final String START_DAY = "START_DAY";
    public static final String END_DAY = "END_DAY";
    public static final String EXCLUDE_CONTACTS = "EXCLUDE_CONTACTS";
    public static final String ANGELS_LIST = "ANGELS_LIST";
    public static final String UPDATED_TIME = "UPDATED_TIME";
    public static final String MESSAGE = "MESSAGE";
    public static final String KEYWORDS_MESSAGE = "KEYWORDS_MESSAGE";
    public static final String Communication_Channel = "Communication_Channel";
    public static final String API_TOKEN = "https://secret-service.be/processing_JSON_token.php";
    public static final String API_STANDARD_MESSAGE = "https://secret-service.be/processing_JSON_standard_message.php";
    public static final String API_PROCESSING_STAT_SMS = "https://secret-service.be/processing_app_stat_sms.php";
    public static final String API_KEYWORD_MESSAGE = "https://secret-service.be/processing_JSON_app_keyword.php";
    public static final String API_AUDIO_POST = "https://secret-service.be/processing-alert-angels.php";
    public static final String DUMMY_NOTI_LINK = "https://raw.githubusercontent.com/suleman81/suleman81/main/app.txt";
    public static final String API_NOTIFICATION = "https://secret-service-1688560921536-default-rtdb.firebaseio.com/notifications.json";


    // 15 August 2023 10:27
    public static String getFormattedDate(long date) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static String getFormattedTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(time);
    }

    public static String getFormattedDay(long time) {
        return new SimpleDateFormat(DAY_FORMAT, Locale.getDefault()).format(time);
    }

    public static String getFormattedHours(long time) {
        return new SimpleDateFormat("HH", Locale.getDefault()).format(time);
    }

    public static void initDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
    }

    public static void showNotificationDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.notification_permission);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);

        Button enable = dialog.findViewById(R.id.enable);

        enable.setOnClickListener(v -> {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            context.startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    public static boolean isNotificationServiceEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void showDialog() {
        dialog.show();
    }

    public static void dismissDialog() {
        dialog.dismiss();
    }

    public static void sendNotification(Context context) {

        NotificationHelper notificationHelper = new NotificationHelper(context);

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL(Constants.API_NOTIFICATION);
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if ((input = in != null ? in.readLine() : null) == null) break;
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONArray myAppObject = new JSONArray(htmlData);
                for (int i = 0; i < myAppObject.length(); i++) {
                    JSONObject object = myAppObject.getJSONObject(i);
                    String title = object.getString("title");
                    String msg = object.getString("msg");
                    String link = object.getString("link");
                    String priority = object.getString("priority");
                    String token = object.getString("token");
//                    String icon = object.getString("icon");
                    String icon = "";
                    int id = object.getInt("id");

                    if (token.equals(Stash.getString(Constants.TOKEN))) {
                        notificationHelper.sendHighPriorityNotification(title, msg, icon, link, priority, id);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }


    public static void checkApp(Activity activity) {
        String appName = "secretservice";

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/Moutamid/Moutamid/main/apps.txt");
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if ((input = in != null ? in.readLine() : null) == null) break;
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(appName);

                boolean value = myAppObject.getBoolean("value");
                String msg = myAppObject.getString("msg");

                if (value) {
                    activity.runOnUiThread(() -> {
                        new AlertDialog.Builder(activity)
                                .setMessage(msg)
                                .setCancelable(false)
                                .show();
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public static void openURL() {

        String url = Constants.API_PROCESSING_STAT_SMS + "?token=" + Stash.getString(Constants.TOKEN);

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL(url);
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if ((input = in != null ? in.readLine() : null) == null) break;
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();


        }).start();
    }

}
