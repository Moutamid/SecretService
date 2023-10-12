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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fxn.stash.Stash;
import com.github.squti.androidwaverecorder.WaveRecorder;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.activities.UpdateActivity;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.utilis.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.moutamid.secretservice.utilis.VolleySingleton;

public class AudioRecordingService extends Service {
    private MediaRecorder mediaRecorder;
    private String outputFile;
    private Timer recordingTimer;
    private final long RECORDING_INTERVAL = 10 * 1000;
    String TAG = "AudioRecordingService";
    Context context;
    RequestQueue requestQueue;
    Location currentLocation;
    WaveRecorder waveRecorder;
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

        context = this;
        requestQueue = Volley.newRequestQueue(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        startRecording();

        Log.d(TAG, "onStartCommand");

        return START_STICKY;
    }


    private void startRecording() {
        mediaRecorder = new MediaRecorder();
/*        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        } else {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        }*/
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        outputFile = getFilePathString();
        File f = new File(outputFile);
        f.mkdirs();
        // Create the output directory if it does not exist.

        String timestamp = String.valueOf(System.currentTimeMillis());
        String name = new SimpleDateFormat("ddMMyyyy").format(new Date());
        name = "AUD_" + name + "_";

        String filename = (name + timestamp) + ".3gp";
        outputFile = outputFile + filename;

        Log.d(TAG, "startRecording");
        Log.d(TAG, "name  " + name);
        Log.d(TAG, "filename  " + filename);
        Log.d(TAG, "outputFile  " + outputFile);

        ArrayList<ContactModel> contactModels = Stash.getArrayList(Constants.ANGELS_LIST, ContactModel.class);

        if (Stash.getBoolean(Constants.ONE_TIME)) {
            for (ContactModel contactModel : contactModels) {
                String message = "ALERT ANGEL ACTIVATE : see the position and listen to what's going on at https://secret-service.be/alert.php?k=" + Stash.getString(Constants.TOKEN);
                sendAutoMessage(contactModel.getContactNumber(), message);
            }
        }

//        waveRecorder = new WaveRecorder(outputFile);
//        waveRecorder.startRecording();

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
//                waveRecorder.stopRecording();
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //         ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
                Task<Location> task = fusedLocationProviderClient.getLastLocation();
                task.addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                        OkHttpClient client = new OkHttpClient.Builder()
                                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                                .build();

                        Log.d(TAG, "currentLocation  " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                        RequestBody requestBody = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("token", Stash.getString(Constants.TOKEN, ""))
                                .addFormDataPart("latitude", String.valueOf(currentLocation.getLatitude()))
                                .addFormDataPart("longitude", String.valueOf(currentLocation.getLongitude()))
                                .addFormDataPart(
                                        "record_alert",
                                        filename,
                                        RequestBody.create(MediaType.parse("audio/3gp"), new File(outputFile))
                                )
                                .build();

                        Log.d(TAG, "filename 2.0  " + filename);

                        Log.d(TAG, "requestBody  " + requestBody.toString());

                        // Create the request
                        Request request = new Request.Builder()
                                .url(Constants.API_AUDIO_POST)
                                .post(requestBody)
                                .build();


                        Log.d(TAG, "request  " + request.body().toString());

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                // Handle error
                                e.printStackTrace();
                                Log.e(TAG, "e.getMessage()   " + e.getMessage());
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String responseBody = response.message();
                                Log.d(TAG, "responseBody   " + responseBody);
                                startRecording();
                            }
                        });
                    }
                });
            }
        }, RECORDING_INTERVAL);

    }


    private String getFilePathString() {
        String path_save_vid = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            path_save_vid =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                            File.separator +
                            getResources().getString(R.string.app_name) +
                            File.separator + "Audio" +
                            File.separator;

        } else {
            path_save_vid =
                    Environment.getExternalStorageDirectory().getAbsolutePath() +
                            File.separator +
                            getResources().getString(R.string.app_name) +
                            File.separator + "Audio" +
                            File.separator;

        }

        return path_save_vid;
//        final File newFile2 = new File(path_save_aud);
//        newFile2.mkdir();
//        newFile2.mkdirs();
//
//        final File newFile4 = new File(path_save_vid);
//        newFile4.mkdir();
//        newFile4.mkdirs();
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
            Stash.put(Constants.ONE_TIME, false);
        } catch (ActivityNotFoundException ae) {
            ae.printStackTrace();
            Log.d(TAG, "ActivityNotFoundException \t " + ae.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Missed Calll E \t " + e.getMessage());
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null && mediaRecorder.getMaxAmplitude() > 0) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            if (recordingTimer != null) {
                recordingTimer.cancel();
            }
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
        stopRecording();
//        if (waveRecorder != null){
//            waveRecorder.stopRecording();
//        }
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
