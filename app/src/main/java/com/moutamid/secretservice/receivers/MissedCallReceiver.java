package com.moutamid.secretservice.receivers;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.models.MessageModel;
import com.moutamid.secretservice.services.MyPhoneStateListener;
import com.moutamid.secretservice.utilis.Constants;

import java.io.Console;
import java.util.ArrayList;
import java.util.Calendar;

public class MissedCallReceiver extends BroadcastReceiver {
    String TAG = "MyPhoneStateListener";
    static MyPhoneStateListener listener;

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        this.context = context;
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "onReceive CALL");
            if(listener == null){
                listener = new MyPhoneStateListener(context);
            }
            TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        } else if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") || intent.getAction().equals("com.google.android.gms.rcs.RECEIVE_RCS_MESSAGE")) {
            Log.d(TAG, "onReceive SMS");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress();
                        String notifMessage = smsMessage.getMessageBody();

                        String message = Stash.getString(Constants.MESSAGE, "N/A");

                        ArrayList<MessageModel> keywordList = Stash.getArrayList(Constants.KEYWORDS_MESSAGE, MessageModel.class);
                        for (MessageModel keyword : keywordList) {
                            message = "";
                            if (notifMessage.contains(keyword.getKeyword())) {
                                message += keyword.getMsg() + "\n\n";
                            }
                        }

                        Log.d(TAG, "onReceive sender   " + sender);
                        if (Stash.getBoolean(Constants.IS_ON)) {
                            Log.d(TAG, "SEND SMS");
                            if (isWithinTimeWindow(sender, Constants.SMS)) {
                                sendAutoMessage(sender, message);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isWithinTimeWindow(String phoneNumber, String source) {
        int startTimeHour = 0;
        int endTimeHour = 0;
        boolean isValid = false;
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

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
            if (model.getContactNumber().contains(phoneNumber)) {
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

        Log.d(TAG, "check 2  " + (currentHour >= startTimeHour && currentHour <= endTimeHour));

        return isValid;
    }

    private void sendAutoMessage(String phoneNumber, String message) {
        Log.d(TAG, "inside sendAutoMessage");
        try {
            String SENT = "SMS_SENT";
            PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), PendingIntent.FLAG_IMMUTABLE);

            SmsManager sms = SmsManager.getDefault();

            ArrayList<String> parts = sms.divideMessage(message);

            ArrayList<PendingIntent> sendList = new ArrayList<>();
            sendList.add(sentPI);

            ArrayList<PendingIntent> deliverList = new ArrayList<>();
            deliverList.add(sentPI);

            sms.sendMultipartTextMessage(phoneNumber, null, parts, sendList, deliverList);
            String url = Constants.API_PROCESSING_STAT_SMS + "?token=" + Stash.getString(Constants.TOKEN);
            Constants.openURL();

            Log.d(TAG, "SMS sent successfully");
        } catch (ActivityNotFoundException ae) {
            ae.printStackTrace();
            Log.d(TAG, "ActivityNotFoundException \t " + ae.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Missed Calll E \t " + e.getMessage());
        }
    }

}