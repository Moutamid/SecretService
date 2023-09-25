package com.moutamid.secretservice.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.fxn.stash.Stash;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;

import com.google.android.gms.location.FusedLocationProviderClient;

import omrecorder.Recorder;

public class AudioRecordingService extends Service {
    private MediaRecorder mediaRecorder;
    private String outputFile;
    private Timer recordingTimer;
    private final long RECORDING_INTERVAL = 60 * 1000;
    String TAG = "AudioRecordingService";
    Context context;

    Recorder recorder;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(100, new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        context = this;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        return START_STICKY;
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.VOICE_COMMUNICATION, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }


    private void startRecord() {

        ArrayList<ContactModel> contactModels = Stash.getArrayList(Constants.ANGELS_LIST, ContactModel.class);

        for (ContactModel contactModel : contactModels) {
            String message = "ALERT ANGEL ACTIVATE : see the position and listen to what's going on at https://secret-service.be/alert.php?k=" + Stash.getString(Constants.TOKEN);
            sendAutoMessage(contactModel.getContactNumber(), message);
        }

        File file = file();
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), audioChunk -> {
                    Log.d(TAG, "onAudioChunkPulled");
                }), file);

        recordingTimer = new Timer();
        recordingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    recorder.stopRecording();
                    recorder.startRecording();

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //         ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                    Task<Location> task = fusedLocationProviderClient.getLastLocation();
                    task.addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation = location;
                        }
                    });
                    Log.d(TAG, "STARTED");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                }
            }
        }, RECORDING_INTERVAL);

    }

    @NonNull
    private File file() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String name = new SimpleDateFormat("ddMMyyyy").format(new Date());
        name = "AUD_" + name + "_";
        String n = (name + timestamp) + ".wav";
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Audio/"), n);
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        outputFile = getOutputFile();
        File out = new File(outputFile);
        out.mkdir();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String name = new SimpleDateFormat("ddMMyyyy").format(new Date());
        name = "AUD_" + name + "_";
        outputFile = outputFile + (name + timestamp) + ".3gpp";

        ArrayList<ContactModel> contactModels = Stash.getArrayList(Constants.ANGELS_LIST, ContactModel.class);

        for (ContactModel contactModel : contactModels) {
            String message = "ALERT ANGEL ACTIVATE : see the position and listen to what's going on at https://secret-service.be/alert.php?k=" + Stash.getString(Constants.TOKEN);
            sendAutoMessage(contactModel.getContactNumber(), message);
        }

        mediaRecorder.setOutputFile(outputFile);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recordingTimer = new Timer();
        recordingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopRecording();
                startRecording();

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //         ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
                Task<Location> task = fusedLocationProviderClient.getLastLocation();
                task.addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                    //    Toast.makeText(context, "currentLocation  " + currentLocation.getLatitude() + " , " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                });
                
            }
        }, RECORDING_INTERVAL);

    }

    private void sendAutoMessage(String phoneNumber, String message) {
        Log.d(TAG, "inside sendAutoMessage");
        try {
            String SENT = "SMS_SENT";
            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), PendingIntent.FLAG_IMMUTABLE);

            SmsManager sms = SmsManager.getDefault();

            ArrayList<String> parts = sms.divideMessage(message);

            ArrayList<PendingIntent> sendList = new ArrayList<>();
            sendList.add(sentPI);

            ArrayList<PendingIntent> deliverList = new ArrayList<>();
            deliverList.add(sentPI);

            sms.sendMultipartTextMessage(phoneNumber, null, parts, sendList, deliverList);

            Log.d(TAG, "SMS sent successfully");
        } catch (ActivityNotFoundException ae) {
            ae.printStackTrace();
            Log.d(TAG, "ActivityNotFoundException \t " + ae.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Missed Calll E \t " + e.getMessage());
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private String getOutputFile() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Audio/";
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Recording Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Audio Recording Service Active")
                .setContentText("")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(100, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        stopRecording();
//        try {
//            recorder.stopRecording();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
