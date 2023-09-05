package com.moutamid.secretservice.utilis;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import java.util.ArrayList;

public class Action implements Parcelable {

    public static final Creator CREATOR = new Creator() {
        public Action createFromParcel(Parcel in) {
            return new Action(in);
        }

        public Action[] newArray(int size) {
            return new Action[size];
        }
    };
    private final String text;
    private final String packageName;
    private final PendingIntent p;
    private final boolean isQuickReply;
    private final ArrayList<RemoteInputParcel> remoteInputs = new ArrayList<>();

    public Action(Parcel in) {
        text = in.readString();
        packageName = in.readString();
        p = in.readParcelable(PendingIntent.class.getClassLoader());
        isQuickReply = in.readByte() != 0;
        in.readTypedList(remoteInputs, RemoteInputParcel.CREATOR);
    }

    public Action(String text, String packageName, PendingIntent p, RemoteInput remoteInput, boolean isQuickReply) {
        this.text = text;
        this.packageName = packageName;
        this.p = p;
        this.isQuickReply = isQuickReply;
        remoteInputs.add(new RemoteInputParcel(remoteInput));
    }

    public Action(NotificationCompat.Action action, String packageName, boolean isQuickReply) {
        this.text = action.title.toString();
        this.packageName = packageName;
        this.p = action.actionIntent;
        if (action.getRemoteInputs() != null) {
            int size = action.getRemoteInputs().length;
            for (int i = 0; i < size; i++)
                remoteInputs.add(new RemoteInputParcel(action.getRemoteInputs()[i]));
        }
        this.isQuickReply = isQuickReply;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeString(packageName);
        dest.writeParcelable(p, flags);
        dest.writeByte((byte) (isQuickReply ? 1 : 0));
        dest.writeTypedList(remoteInputs);
    }

    public void sendReply(Context context, String msg) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        ArrayList<RemoteInput> actualInputs = new ArrayList<>();

        for (RemoteInputParcel input : remoteInputs) {
            bundle.putCharSequence(input.getResultKey(), msg);
            RemoteInput.Builder builder = new RemoteInput.Builder(input.getResultKey());
            builder.setLabel(input.getLabel());
            builder.setChoices(input.getChoices());
            builder.setAllowFreeFormInput(input.isAllowFreeFormInput());
            builder.addExtras(input.getExtras());
            actualInputs.add(builder.build());
        }

        RemoteInput[] inputs = actualInputs.toArray(new RemoteInput[actualInputs.size()]);
        RemoteInput.addResultsToIntent(inputs, intent, bundle);
        try {
            p.send(context, 0, intent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<RemoteInputParcel> getRemoteInputs() {
        return remoteInputs;
    }

    public boolean isQuickReply() {
        return isQuickReply;
    }

    public String getText() {
        return text;
    }

    public PendingIntent getQuickReplyIntent() {
        return isQuickReply ? p : null;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
