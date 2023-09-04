package com.moutamid.secretservice.utilis;

import com.moutamid.secretservice.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Window;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class Constants {

    static Dialog dialog;
    public static final String DATE_FORMAT = "dd MMMM yyyy HH:mm";
    public static final String DAY_FORMAT = "dd/MM/yyyy";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String IS_ON = "IS_ON";
    public static final String IS_TOKEN_VERIFY = "IS_TOKEN_VERIFY";
    public static final String TOKEN = "TOKEN";
    public static final String TIME = "TIME";
    public static final String SELECTED_TIME = "SELECTED_TIME";
    public static final String FROM_TIME = "FROM_TIME";
    public static final String FROM_WEEK = "FROM_WEEK";
    public static final String TO_TIME = "TO_TIME";
    public static final String TO_WEEK = "TO_WEEK";
    public static final String START_DAY_TIME = "START_DAY_TIME";
    public static final String END_DAY_TIME = "END_DAY_TIME";
    public static final String START_DAY = "START_DAY";
    public static final String END_DAY = "END_DAY";
    public static final String EXCLUDE_CONTACTS = "EXCLUDE_CONTACTS";
    public static final String UPDATED_TIME = "UPDATED_TIME";
    public static final String API_TOKEN = "https://secret-service.be/processing_JSON_token.php";
    public static final String API_STANDARD_MESSAGE = "https://secret-service.be/processing_JSON_standard_message.php";


    // 15 August 2023 10:27
    public static String getFormattedDate(long date){
        return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date);
    }
    public static String getFormattedTime(long time){
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String formattedTime = sdf.format(time);
        return formattedTime;
    }
    public static String getFormattedDay(long time){
        return new SimpleDateFormat(DAY_FORMAT, Locale.getDefault()).format(time);
    }

    public static void initDialog(Context context){
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
    }

    public static void showDialog(){
        dialog.show();
    }

    public static void dismissDialog(){
        dialog.dismiss();
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

}
