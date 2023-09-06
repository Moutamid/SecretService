package com.moutamid.secretservice.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.CallLog;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Constants;

import java.io.Console;
import java.util.ArrayList;
import java.util.Calendar;

public class MissedCallReceiver extends BroadcastReceiver {
    String TAG = "MissedCallReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "onReceive");
            if (phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {  // EXTRA_STATE_IDLE
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG, "incomingNumber   " + incomingNumber);
                if (Stash.getBoolean(Constants.IS_ON)) {
                    Log.d(TAG, "ISONN");
                    if (isMissedCall(context, incomingNumber)) {
                        Log.d(TAG, "isMissedCall");
                        if (isWithinTimeWindow(incomingNumber, Constants.MISSED_CALLS)){
                            Log.d(TAG, "isWithinTimeWindow");
                            sendAutoMessage(incomingNumber, Stash.getString(Constants.MESSAGE, ""));
                        }
                    } else {
                        Log.d(TAG, "CALL");
                        if (isWithinTimeWindow(incomingNumber, Constants.REFUSED_CALLS)){
                            Log.d(TAG, "isWithinTimeWindow");
                            sendAutoMessage(incomingNumber, Stash.getString(Constants.MESSAGE, ""));
                        }
                    }
                }
            }
        }
    }

/*    private boolean isMissedCall(Context context, String phoneNumber) {
        String[] projection = {CallLog.Calls.TYPE};
        String selection = CallLog.Calls.NUMBER + " = ? AND " + CallLog.Calls.TYPE + " = ?";
        String[] selectionArgs = {phoneNumber, String.valueOf(CallLog.Calls.MISSED_TYPE)};
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null) {
            boolean isMissed = cursor.moveToNext();
            cursor.close();
            return isMissed;
        }
        return false;
    }*/

    private boolean isMissedCall(Context context, String phoneNumber) {
        String[] projection = {CallLog.Calls.TYPE};
        String selection = CallLog.Calls.NUMBER + " = ? AND " + CallLog.Calls.TYPE + " = ?";
        String[] selectionArgs = {phoneNumber, String.valueOf(CallLog.Calls.MISSED_TYPE)};

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(
                    android.provider.CallLog.Calls.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                // Check if there is at least one missed call with the specified phone number
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return false;
    }


    private boolean isWithinTimeWindow(String phoneNumber, String source) {
        int startTimeHour = 0;
        int endTimeHour = 0;
        boolean isValid = false;
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

        Log.d(TAG, "startTimeHour   " + startTimeHour + "\t\tendTimeHour    " + endTimeHour);

        if (Stash.getInt(Constants.TIME,  3) == 0) {
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
        for (ContactModel model : list){
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
        if (!check){
            return false;
        }

        Log.d(TAG, "check 2  " + (currentHour >= startTimeHour && currentHour <= endTimeHour));

        return isValid;
    }

    private void sendAutoMessage(String phoneNumber, String message) {
        Log.d(TAG, "inside sendAutoMessage");
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d(TAG, "SMS sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG , "Missed Calll E \t " + e.getMessage());
        }
    }
}