package com.moutamid.secretservice.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.RemoteInput;

import com.moutamid.secretservice.services.NotificationListenerService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NotificationActionReceiver extends BroadcastReceiver {
    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_PACK = "EXTRA_PACK";
    public static final String ACTION_REPLY = "ACTION_REPLY";
    public static final String ACTION_READ = "ACTION_READ";
    @Nullable
    private String getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            CharSequence message = remoteInput.getCharSequence(ACTION_REPLY);
            if (message != null) {
                return message.toString();
            }
        }
        return null;
    }

    @Override
    public void onReceive(Context context, @NotNull Intent intent) {
        String action = intent.getAction();
//        Timber.i("Action : %s", action);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String pack = intent.getStringExtra(EXTRA_PACK);
//        Timber.i("Title : %s", title);
        if (action == null || title == null)
            return;
        if (action.equals(ACTION_REPLY)) {
//            Timber.i("Replying to : %s", title);
            String messageText = getMessageText(intent);
//            Timber.i("Message : " + messageText);
//            NotificationListenerService.reply(context, title, messageText,pack);
        } else if (action.equals(ACTION_READ)) {
//            Timber.i("Marking as read : %s", title);
        }
//        NotificationHelper.cancelNotification(context, title.hashCode());
    }

}
