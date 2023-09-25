package com.moutamid.secretservice;

import static com.android.volley.VolleyLog.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fxn.stash.Stash;
import com.moutamid.secretservice.activities.AngelsListActivity;
import com.moutamid.secretservice.activities.NoContactsActivity;
import com.moutamid.secretservice.activities.ReplyActivity;
import com.moutamid.secretservice.activities.SetTimerActivity;
import com.moutamid.secretservice.activities.TokenActivity;
import com.moutamid.secretservice.activities.UpdateActivity;
import com.moutamid.secretservice.databinding.ActivityMainBinding;
import com.moutamid.secretservice.models.ContactModel;
import com.moutamid.secretservice.services.AudioRecordingService;
import com.moutamid.secretservice.services.MyPhoneStateListener;
import com.moutamid.secretservice.services.MyService;
import com.moutamid.secretservice.services.NotificationListenerService;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    MyService mYourService;
    RequestQueue requestQueue;
    String[] permissions13 = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
            Manifest.permission.RECEIVE_SMS,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        startInitService();

        askToDisableDozeMode();

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
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.SEND_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CALL_LOG);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECEIVE_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO);
                shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_PHONE_STATE);
                shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION);
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);
                ActivityCompat.requestPermissions(MainActivity.this, permissions13, 2);
            }
        } else {
            if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.SEND_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CALL_LOG);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECEIVE_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECORD_AUDIO);
                shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION);
                shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_PHONE_STATE);
                ActivityCompat.requestPermissions(MainActivity.this, permissions, 2);
            }
        }
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
            Intent intent = new Intent(this, AudioRecordingService.class);
            ContextCompat.startForegroundService(this, intent);
            binding.alert.setCardBackgroundColor(getResources().getColor(R.color.pink));
            binding.alertIco.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.alertText.setTextColor(getResources().getColor(R.color.white));
        } else {
            stopService(new Intent(this, AudioRecordingService.class));
            binding.alert.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
            binding.alertIco.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.alertText.setTextColor(getResources().getColor(R.color.text_color));
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
                            Toast.makeText(this, "Create an AUTO ANSWER message first", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Activate SET TIME first", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Select REPLY TO channels first", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }
        });
        binding.timer.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                startActivity(new Intent(this, SetTimerActivity.class));
            } else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }
        });
        binding.update.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                startActivity(new Intent(this, UpdateActivity.class));
            } else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }

        });
        binding.noContacts.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                startActivity(new Intent(this, NoContactsActivity.class));
            } else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }
        });
        binding.reply.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                startActivity(new Intent(this, ReplyActivity.class));
            } else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }
        });
        binding.angels.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                startActivity(new Intent(this, AngelsListActivity.class));
            } else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }
        });
        binding.alert.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                if (Stash.getArrayList(Constants.ANGELS_LIST, ContactModel.class).size() >= 1) {
                    if (!checkGPSStatus()) {
                        if (Stash.getBoolean(Constants.IS_ALERT_ON, false)) {
                            binding.alert.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
                            binding.alertIco.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                            binding.alertText.setTextColor(getResources().getColor(R.color.text_color));
                            Stash.put(Constants.IS_ALERT_ON, false);
                            stopService(new Intent(this, AudioRecordingService.class));
                            uploadAlertStatus();
                        } else {
                            binding.alert.setCardBackgroundColor(getResources().getColor(R.color.pink));
                            binding.alertIco.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                            binding.alertText.setTextColor(getResources().getColor(R.color.white));
                            Stash.put(Constants.IS_ALERT_ON, true);
                            Intent intent = new Intent(this, AudioRecordingService.class);
                            ContextCompat.startForegroundService(this, intent);
                        }
                    }
                } else {
                    Toast.makeText(this, "Select at least 1 contact in ANGEL'S LIST", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void uploadAlertStatus() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_AUDIO_POST,
                response -> {
                    Log.d("TOKEN_CHECK", "Response : " + response.toString());
                },
                error -> {
                    Log.e("TOKEN_CHECK",  "Error  : "+ error.getLocalizedMessage() + "");
                    Toast.makeText(MainActivity.this, error.getLocalizedMessage() + "", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, "Doze mode is active", Toast.LENGTH_SHORT).show();
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