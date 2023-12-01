package com.moutamid.secretservice;

import static com.android.volley.VolleyLog.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fxn.stash.Stash;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.moutamid.secretservice.activities.AngelsListActivity;
import com.moutamid.secretservice.activities.NoContactsActivity;
import com.moutamid.secretservice.activities.ReplyActivity;
import com.moutamid.secretservice.activities.SetTimerActivity;
import com.moutamid.secretservice.activities.TokenActivity;
import com.moutamid.secretservice.activities.UpdateActivity;
import com.moutamid.secretservice.databinding.ActivityMainBinding;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.services.AudioRecordingService;
import com.moutamid.secretservice.services.FcmNotificationsSender;
import com.moutamid.secretservice.services.MyAccessibilityService;
import com.moutamid.secretservice.services.MyPhoneStateListener;
import com.moutamid.secretservice.services.MyService;
import com.moutamid.secretservice.services.NotificationListenerService;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    MyService mYourService;
    RequestQueue requestQueue;
    private MediaRecorder mRecorder;
    private static String mFileName = null;
    FusedLocationProviderClient fusedLocationProviderClient;
    private PowerManager.WakeLock wakeLock;
    Timer timer;
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    String[] permissions13 = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.POST_NOTIFICATIONS,
    };
    String[] permissions = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECEIVE_SMS,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        // In your Application class or main activity
        FirebaseApp.initializeApp(this);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "SecretService::WakeLockTag");

        askToDisableDozeMode();
/*

        findViewById(R.id.send).setOnClickListener(v -> {
                    new FcmNotificationsSender("/topics/" + Stash.getString(Constants.TOKEN, ""), "Title Text From App",
                "Description Text From App", getApplicationContext(), this).SendNotifications();
        });
*/


/*        String time = Stash.getString(Constants.UPDATED_TIME, "N/A");
        binding.time.setText(time);*/

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        askForPermissions();

        binding.token.setOnClickListener(v -> {
            startActivity(new Intent(this, TokenActivity.class));
        });

    }

    private void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (above13Check()) {
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.SEND_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CALL_LOG);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECEIVE_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO);
                shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_PHONE_STATE);
                shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_AUDIO);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO);
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
                ActivityCompat.requestPermissions(MainActivity.this, permissions13, 2);
            }
        } else {
            if (below13Check()) {
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.SEND_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CALL_LOG);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECEIVE_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO);
                shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION);
                shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_PHONE_STATE);
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 2);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean above13Check() {
        return ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED;
    }

    private boolean below13Check() {
        return ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!Constants.isNotificationServiceEnabled(MainActivity.this)) {
                    Constants.showNotificationDialog(MainActivity.this);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
            binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
            binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.onOffText.setTextColor(getResources().getColor(R.color.text_color));
            Stash.put(Constants.IS_ON, false);
        }

        if (Stash.getBoolean(Constants.IS_ALERT_ON, false)) {
            binding.alert.setCardBackgroundColor(getResources().getColor(R.color.pink));
            binding.alertIco.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.alertText.setTextColor(getResources().getColor(R.color.white));

            wakeLock.acquire();

        } else {
            binding.alert.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
            binding.alertIco.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.alertText.setTextColor(getResources().getColor(R.color.text_color));

            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }

        if (!Stash.getString(Constants.TOKEN, "").isEmpty()) {
            FirebaseMessaging.getInstance().subscribeToTopic(Stash.getString(Constants.TOKEN, ""))
                    .addOnSuccessListener(unused -> {
                    });
        }

        enableViews();
    }

    private void enableViews() {
        if (Stash.getBoolean(Constants.IS_ON, false)) {
            binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.pink));
            binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.onOffText.setTextColor(getResources().getColor(R.color.white));
        } else {
            binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
            binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.onOffText.setTextColor(getResources().getColor(R.color.text_color));
        }

        binding.onOff.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                if (!Stash.getString(Constants.Communication_Channel, "").isEmpty() || Stash.getBoolean(Constants.IS_ON, false)) {
                    if (Stash.getInt(Constants.TIME, 3) < 3) {
                        if (!Stash.getString(Constants.MESSAGE, "").isEmpty()) {
                            if (Stash.getBoolean(Constants.IS_ON, false)) {
                                binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
                                binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                                binding.onOffText.setTextColor(getResources().getColor(R.color.text_color));
                                Stash.put(Constants.IS_ON, false);
                            } else {
                                binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.pink));
                                binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                                binding.onOffText.setTextColor(getResources().getColor(R.color.white));
                                Stash.put(Constants.IS_ON, true);
                            }
                        } else {
                            Constants.showToast(MainActivity.this, "Create an AUTO ANSWER message first");
                        }
                    } else {
                        Constants.showToast(MainActivity.this, "Activate SET TIME first");
                    }
                } else {
                    Constants.showToast(MainActivity.this, "Select REPLY TO channels first");
                }
            } else {
                Constants.showToast(MainActivity.this, "Validate your TOKEN first");
            }
        });
        binding.timer.setOnClickListener(v -> {
            if (!Stash.getBoolean(Constants.IS_ALERT_ON, false)) {
                if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                    startActivity(new Intent(this, SetTimerActivity.class));
                } else {
                    Constants.showToast(MainActivity.this, "Validate your TOKEN first");
                }
            } else {
                showAlert();
            }
        });
        binding.update.setOnClickListener(v -> {
            if (!Stash.getBoolean(Constants.IS_ALERT_ON, false)) {
                if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                    startActivity(new Intent(this, UpdateActivity.class));
                } else {
                    Constants.showToast(MainActivity.this, "Validate your TOKEN first");
                }
            } else {
                showAlert();
            }
        });
        binding.noContacts.setOnClickListener(v -> {
            if (!Stash.getBoolean(Constants.IS_ALERT_ON, false)) {
                if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                    startActivity(new Intent(this, NoContactsActivity.class));
                } else {
                    Constants.showToast(MainActivity.this, "Validate your TOKEN first");
                }
            } else {
                showAlert();
            }
        });
        binding.reply.setOnClickListener(v -> {
            if (!Stash.getBoolean(Constants.IS_ALERT_ON, false)) {
                if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                    startActivity(new Intent(this, ReplyActivity.class));
                } else {
                    Constants.showToast(MainActivity.this, "Validate your TOKEN first");
                }
            } else {
                showAlert();
            }

        });
        binding.angels.setOnClickListener(v -> {
            if (!Stash.getBoolean(Constants.IS_ALERT_ON, false)) {
                if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                    startActivity(new Intent(this, AngelsListActivity.class));
                } else {
                    Constants.showToast(MainActivity.this, "Validate your TOKEN first");
                }
            } else {
                showAlert();
            }
        });
        binding.alert.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                if (Stash.getArrayList(Constants.ANGELS_LIST, ContactModel.class).size() >= 1) {
                    if (!checkGPSStatus()) {

                        if (Stash.getBoolean(Constants.ALERT_CHECK, false)) {
                            if (Stash.getBoolean(Constants.IS_ALERT_ON, false)) {
                                binding.alert.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
                                binding.alertIco.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                                binding.alertText.setTextColor(getResources().getColor(R.color.text_color));

                                if (wakeLock.isHeld()) {
                                    wakeLock.release();
                                }

                                Stash.put(Constants.IS_ALERT_ON, false);
//                            stopService(new Intent(this, AudioRecordingService.class));
                                Stash.put(Constants.ONE_TIME, false);
                                stopRecording();
                                if (timer != null) {
                                    timer.cancel();
                                }
                                uploadAlertStatus();
                                Constants.showToast(MainActivity.this, "Alert Status OFF");
                            } else {
                                binding.alert.setCardBackgroundColor(getResources().getColor(R.color.pink));
                                binding.alertIco.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                                binding.alertText.setTextColor(getResources().getColor(R.color.white));

                                wakeLock.acquire();

                                Stash.put(Constants.IS_ALERT_ON, true);
/*                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Log.i("onReceive: ", "        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {");
                                startForegroundService(new Intent(this, AudioRecordingService.class));
                            } else {
                                Log.i("onReceive: ", "} else {");
                                startService(new Intent(this, AudioRecordingService.class));
                            }*/
                                Stash.put(Constants.ONE_TIME, true);
                                startRecording();
                                Constants.showToast(MainActivity.this, "Alert Status ON");
                                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                                ArrayList<ContactModel> contactModels = Stash.getArrayList(Constants.ANGELS_LIST, ContactModel.class);
                                if (Stash.getBoolean(Constants.ONE_TIME)) {
                                    for (ContactModel contactModel : contactModels) {
                                        String message = "ALERT ANGEL ACTIVATE : see the position and listen to what's going on at https://secret-service.be/alert.php?k=" + Stash.getString(Constants.TOKEN);
                                        sendAutoMessage(contactModel.getContactNumber(), message);
                                    }
                                }

                                timer = new Timer();
                                timer.scheduleAtFixedRate(new TimerTask() {
                                    @Override
                                    public void run() {
                                        stopRecording();
//                                    startRecording();
                                        uploadAudio();
                                    }
                                }, 30000, 30000);
                            }
                        } else {
                            showAlertCheck();
                        }
                    }
                } else {
                    Constants.showToast(MainActivity.this, "Select at least 1 contact in ANGEL'S LIST");
                }
            } else {
                Constants.showToast(MainActivity.this, "Validate your TOKEN first");
            }
        });

    }

    private void showAlertCheck() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_check);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);

        Button confirm = dialog.findViewById(R.id.confirm);
        MaterialCheckBox checkBox = dialog.findViewById(R.id.check);

        confirm.setOnClickListener(v -> {
            if (checkBox.isChecked()) {
                Stash.put(Constants.ALERT_CHECK, true);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please check the Alert Button Notice", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void showAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Alert is ON")
                .setMessage("Turn off the Alert status to use other functionalities of the app")
                .setPositiveButton("Ok", ((dialog, which) -> dialog.dismiss()))
                .show();
    }

    Location currentLocation;

    private void uploadAudio() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                                RequestBody.create(MediaType.parse("audio/3gp"), new File(mFileName))
                        )
                        .build();

                Log.d(TAG, "filename 2.0  " + filename);

                Log.d(TAG, "requestBody  " + requestBody.toString());

                // Create the request
                okhttp3.Request request = new okhttp3.Request.Builder()
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
                        Constants.showToast(MainActivity.this, e.getMessage());
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

    String filename;

    private void startRecording() {
        filename = "Audio" + System.currentTimeMillis() + ".3gp";
        mFileName = getFilePath() + filename;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setOutputFile(mFileName);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            Log.e("TAG", "prepare() failed" + e.getMessage());
            Log.e("TAG", "prepare() failed" + e.getLocalizedMessage());
            Log.e("TAG", "prepare() failed" + e.getStackTrace().toString());
            timer.cancel();
            runOnUiThread(() -> {
                Constants.showToast(MainActivity.this, "Cancelled with error: " + e.getLocalizedMessage());
            });
        }
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

    public void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        Stash.put(Constants.IS_ALERT_ON, false);
        Stash.put(Constants.ONE_TIME, false);
        stopRecording();
        if (timer != null) {
            timer.cancel();
        }
        uploadAlertStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        Stash.put(Constants.IS_ALERT_ON, false);
        Stash.put(Constants.ONE_TIME, false);
        stopRecording();
        if (timer != null) {
            timer.cancel();
        }
        uploadAlertStatus();
    }

    private String getFilePath() {
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
        File newFile2 = new File(path_save_vid);
        newFile2.mkdir();
        newFile2.mkdirs();

        return path_save_vid;
    }

    private void uploadAlertStatus() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_AUDIO_POST,
                response -> {
                    Log.d("TOKEN_CHECK", "Response : " + response.toString());
                },
                error -> {
                    Log.e("TOKEN_CHECK", "Error  : " + error.getLocalizedMessage() + "");
                    Constants.showToast(MainActivity.this, error.getLocalizedMessage() + "");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", Stash.getString(Constants.TOKEN));
                params.put("alert_activate", "false");
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void askToDisableDozeMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG, "askToDisableDozeMode: ");
                Intent intent = new Intent();
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                    intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                } else {
                    Constants.showToast(MainActivity.this, "Doze mode is active");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkGPSStatus() {
        LocationManager locationManager;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(this).setTitle("GPS not enabled")
                    .setMessage("GPS is required for alerting the angel's list")
                    .setCancelable(true)
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }).show();
        }
        return !gps_enabled && !network_enabled;
    }

    private void startInitService() {
        mYourService = new MyService();
        Intent mServiceIntent = new Intent(this, mYourService.getClass());
        if (!isMyServiceRunning(mYourService.getClass())) {
            startService(mServiceIntent);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

}