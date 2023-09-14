package com.moutamid.secretservice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
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
import com.moutamid.secretservice.activities.NoContactsActivity;
import com.moutamid.secretservice.activities.ReplyActivity;
import com.moutamid.secretservice.activities.SetTimerActivity;
import com.moutamid.secretservice.activities.TokenActivity;
import com.moutamid.secretservice.databinding.ActivityMainBinding;
import com.moutamid.secretservice.services.MyPhoneStateListener;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    RequestQueue requestQueue;
    String[] permissions = new String[] {
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        String time = Stash.getString(Constants.UPDATED_TIME, "N/A");
        binding.time.setText(time);

        if (    ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED )
        {
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS);
            shouldShowRequestPermissionRationale(android.Manifest.permission.SEND_SMS);
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CALL_LOG);
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_PHONE_STATE);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 2);
        }

        binding.token.setOnClickListener(v -> {
            startActivity(new Intent(this, TokenActivity.class));
        });

        requestQueue = VolleySingleton.getInstance(MainActivity.this).getRequestQueue();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (!Constants.isNotificationServiceEnabled(MainActivity.this)){
                    Constants.showNotificationDialog(MainActivity.this);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(MainActivity.this);
        if (!Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
            binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
            binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.onOffText.setTextColor(getResources().getColor(R.color.text_color));
            Stash.put(Constants.IS_ON, false);
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

                            if (Stash.getString(Constants.UPDATED_TIME, "N/A").equals("N/A")) {
                                Toast.makeText(this, "Please UPDATE your message first", Toast.LENGTH_LONG).show();
                            }

                        }

                    } else {
                        Toast.makeText(this, "Activate SET TIME first", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Select REPLY TO channels first", Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }
        });
        binding.timer.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                startActivity(new Intent(this, SetTimerActivity.class));
            }else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }
        });
        binding.update.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                Constants.showDialog();
                updateMessage();
            }else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }

        });
        binding.noContacts.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                startActivity(new Intent(this, NoContactsActivity.class));
            }else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }
        });
        binding.reply.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
                startActivity(new Intent(this, ReplyActivity.class));
            }else {
                Toast.makeText(this, "Validate your TOKEN first", Toast.LENGTH_LONG).show();
            }

        });

    }

    private void updateMessage() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_STANDARD_MESSAGE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        runOnUiThread(Constants::dismissDialog);
                        Log.d("TOKEN_CHECK", response.toString());
                        try {
                            JSONObject obj = new JSONObject(response);
                            String msg = obj.getString("msg");
                            Log.d("TOKEN_CHECK", msg);
                            if (!msg.isEmpty()) {
                                Stash.put(Constants.MESSAGE, msg);
                                String date = Constants.getFormattedDate(new Date().getTime());
                                Stash.put(Constants.UPDATED_TIME, date);
                                binding.time.setText(date);
                                Toast.makeText(MainActivity.this, "Message Updated", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Message is empty", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        runOnUiThread(Constants::dismissDialog);
                        Log.d("TOKEN_CHECK", error.getLocalizedMessage() + "");
                        Toast.makeText(MainActivity.this, error.getLocalizedMessage()+"", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", Stash.getString(Constants.TOKEN));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

}