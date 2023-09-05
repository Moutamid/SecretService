package com.moutamid.secretservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        String time = Stash.getString(Constants.UPDATED_TIME, "N/A");
        binding.time.setText(time);

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS);
            shouldShowRequestPermissionRationale(android.Manifest.permission.SEND_SMS);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS}, 2);
        }

        binding.token.setOnClickListener(v -> {
            startActivity(new Intent(this, TokenActivity.class));
        });

        requestQueue = VolleySingleton.getInstance(MainActivity.this).getRequestQueue();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(MainActivity.this);
        if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
            enableViews();
            binding.onOff.setClickable(true);
            binding.timer.setClickable(true);
            binding.update.setClickable(true);
            binding.noContacts.setClickable(true);
            binding.reply.setClickable(true);
        } else {
            binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
            binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.onOffText.setTextColor(getResources().getColor(R.color.text_color));
            Stash.put(Constants.IS_ON, false);
            binding.onOff.setClickable(false);
            binding.timer.setClickable(false);
            binding.update.setClickable(false);
            binding.noContacts.setClickable(false);
            binding.reply.setClickable(false);
        }
    }

    private void enableViews() {
        if (Stash.getBoolean(Constants.IS_ON, false)) {
            binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.text_color));
            binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
            binding.onOffText.setTextColor(getResources().getColor(R.color.bg_color));
        } else {
            binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
            binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            binding.onOffText.setTextColor(getResources().getColor(R.color.text_color));
        }

        binding.onOff.setOnClickListener(v -> {
            if (Stash.getBoolean(Constants.IS_ON, false)) {
                binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
                binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                binding.onOffText.setTextColor(getResources().getColor(R.color.text_color));
                Stash.put(Constants.IS_ON, false);
            } else {
                binding.onOff.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.onOffICO.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.onOffText.setTextColor(getResources().getColor(R.color.bg_color));
                Stash.put(Constants.IS_ON, true);
            }
        });
        binding.timer.setOnClickListener(v -> {
            startActivity(new Intent(this, SetTimerActivity.class));
        });
        binding.update.setOnClickListener(v -> {
            Constants.showDialog();
            updateMessage();
        });
        binding.noContacts.setOnClickListener(v -> {
            startActivity(new Intent(this, NoContactsActivity.class));
        });
        binding.reply.setOnClickListener(v -> {
            startActivity(new Intent(this, ReplyActivity.class));
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
                                Toast.makeText(MainActivity.this, "Message Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Message is empty", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        runOnUiThread(Constants::dismissDialog);
                        Log.d("TOKEN_CHECK", error.getMessage());
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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