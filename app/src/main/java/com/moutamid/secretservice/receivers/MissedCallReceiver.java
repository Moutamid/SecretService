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
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d("RECIEVER123", "onReceive");
            Toast.makeText(context, "onReceive", Toast.LENGTH_SHORT).show();
            if (phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

                if (Stash.getBoolean(Constants.IS_ON)) {
                    Log.d("RECIEVER123", "ISONN");
                    if (isMissedCall(context, incomingNumber)) {
                        Log.d("RECIEVER123", "isMissedCall");
                        if (isWithinTimeWindow(incomingNumber)){
                            Log.d("RECIEVER123", "isWithinTimeWindow");
                            sendAutoMessage(incomingNumber, Stash.getString(Constants.MESSAGE, ""));
                        }
                    }
                }
            }
        }
    }

    private boolean isMissedCall(Context context, String phoneNumber) {
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
    }

    private boolean isWithinTimeWindow(String phoneNumber) {
        int startTimeHour = 0;
        int endTimeHour = 0;

        if (Stash.getInt(Constants.TIME,  3) == 0) {
            startTimeHour = Integer.parseInt(Constants.getFormattedHours(Stash.getLong(Constants.FROM_TIME, 0)));
            endTimeHour = Integer.parseInt(Constants.getFormattedHours(Stash.getLong(Constants.TO_TIME, 0)));
        } else if (Stash.getInt(Constants.TIME, 3) == 1) {

        } else if (Stash.getInt(Constants.TIME, 3) == 2) {

        } else {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        ArrayList<ContactModel> list = Stash.getArrayList(Constants.EXCLUDE_CONTACTS, ContactModel.class);
        for (ContactModel model : list){
            if (model.getContactNumber().contains(phoneNumber)) {
                return false;
            }
        }
        boolean check = false;
        String[] communication_channel = Stash.getString(Constants.Communication_Channel, "").split(", ");
        for (String channel : communication_channel) {
            if (channel.equals(Constants.MISSED_CALLS)) {
                check = true;
            }
        }

        if (!check){
            return false;
        }

        return currentHour >= startTimeHour && currentHour <= endTimeHour;
    }

    private void sendAutoMessage(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}