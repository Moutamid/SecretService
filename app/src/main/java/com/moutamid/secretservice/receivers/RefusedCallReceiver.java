package com.moutamid.secretservice.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Constants;

import java.util.ArrayList;
import java.util.Calendar;

public class RefusedCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String phoneState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (phoneState != null && phoneState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                // A call was refused, check if it's within the specified time window
                String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (Stash.getBoolean(Constants.IS_ON)){
                    if (isWithinTimeWindow(incomingNumber)) {
                        sendAutoMessage(incomingNumber, Stash.getString(Constants.MESSAGE, ""));
                    }
                }
            }
        }
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
            if (channel.equals(Constants.REFUSED_CALLS)) {
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
