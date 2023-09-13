package com.moutamid.secretservice.services;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Constants;

import java.util.ArrayList;
import java.util.Calendar;

public class MyPhoneStateListener extends PhoneStateListener {
    private Context context;
    String TAG = "MyPhoneStateListener";
    int i = 0;
    int lastState = TelephonyManager.CALL_STATE_IDLE;
    boolean isIncoming;

    public MyPhoneStateListener(Context context) {
        this.context = context;
    }


    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);

        if (lastState == state) {
            //No change
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                // incoming call started
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing down on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    //outgoing call started
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //End of call(Idle).  The type depends on the previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //toast here for missed call
                    if (Stash.getBoolean(Constants.IS_ON)) {
                        Log.d(TAG, "isMissedCall");
                        if (isWithinTimeWindow(incomingNumber, Constants.MISSED_CALLS)) {
                            Log.d(TAG, "isWithinTimeWindow");
                            sendAutoMessage(incomingNumber, Stash.getString(Constants.MESSAGE, ""));
                        }
                    }
                } else if (isIncoming) {
                    /*
                    //incoming call ended
                    if (Stash.getBoolean(Constants.IS_ON)) {
                        if (isWithinTimeWindow(incomingNumber, Constants.REFUSED_CALLS)){
                            Log.d(TAG, "isWithinTimeWindow");
                            sendAutoMessage(incomingNumber, Stash.getString(Constants.MESSAGE, ""));
                        }
                    } */
                } else {
                    //outgoing call ended
                }
                break;
        }
        lastState = state;

    /*
        if (state == TelephonyManager.CALL_STATE_RINGING) {
            // A call is ringing, check if it's a missed call
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
        } */
    }

//    private boolean isMissedCall(Context context, String phoneNumber) {
//        String[] projection = {CallLog.Calls.TYPE};
//        String selection = CallLog.Calls.NUMBER + " = ? AND " + CallLog.Calls.TYPE + " = ?";
//        String[] selectionArgs = {phoneNumber, String.valueOf(CallLog.Calls.MISSED_TYPE)};
//        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null);
//        if (cursor != null) {
//            boolean isMissed = cursor.moveToNext();
//            cursor.close();
//            return isMissed;
//        }
//        return false;
//    }

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

//            sms.sendTextMessage(phoneNumber, null, message, sentPI, null);

            String url = Constants.API_PROCESSING_STAT_SMS + "?token=" + Stash.getString(Constants.TOKEN);
/*            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);*/

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
