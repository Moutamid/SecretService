package com.moutamid.secretservice.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.utilis.Constants;

public class SmsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String[] contacts = intent.getStringArrayExtra("contacts");
        String message = Stash.getString(Constants.MESSAGE);

        if (contacts != null && message != null) {
            for (String contact : contacts) {
                // Use SmsManager to send SMS
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(contact, null, message, null, null);
            }
        }
    }
}