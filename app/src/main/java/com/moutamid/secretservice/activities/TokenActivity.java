package com.moutamid.secretservice.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fxn.stash.Stash;
import com.moutamid.secretservice.MainActivity;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.databinding.ActivityTokenBinding;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenActivity extends AppCompatActivity {
    ActivityTokenBinding binding;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTokenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Constants.initDialog(this);

        binding.toolbar.back.setOnClickListener(v -> onBackPressed());

        binding.link.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://secret-service.be/balance-sms.php"))));

        if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)) {
            binding.validated.setVisibility(View.VISIBLE);
            binding.notValidated.setVisibility(View.GONE);
        } else {
            binding.validated.setVisibility(View.GONE);
            binding.notValidated.setVisibility(View.VISIBLE);
        }

        if (!Stash.getString(Constants.TOKEN, "").isEmpty()) {
            binding.token.setText(Stash.getString(Constants.TOKEN, ""));
        }

        requestQueue = VolleySingleton.getInstance(TokenActivity.this).getRequestQueue();

        binding.verify.setOnClickListener(v -> {
            Constants.showDialog();
            checkToken();
        });

    }

    public void checkToken() {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_TOKEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        runOnUiThread(Constants::dismissDialog);
                        Log.d("TOKEN_CHECK", response.toString());
                        try {
                            JSONObject obj = new JSONObject(response);
                            String error = obj.getString("error");
                            if (error == "false"){
                                Stash.put(Constants.TOKEN, binding.token.getText().toString().trim());
                                Stash.put(Constants.IS_TOKEN_VERIFY, true);
                                binding.validated.setVisibility(View.VISIBLE);
                                binding.notValidated.setVisibility(View.GONE);
                                Toast.makeText(TokenActivity.this, "Token Updated", Toast.LENGTH_LONG).show();
                            } else {
                                Stash.put(Constants.TOKEN, "");
                                Stash.put(Constants.IS_TOKEN_VERIFY, false);
                                binding.validated.setVisibility(View.GONE);
                                binding.notValidated.setVisibility(View.VISIBLE);
                                Toast.makeText(TokenActivity.this, "Token is not valid", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(TokenActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        runOnUiThread(Constants::dismissDialog);
                        Log.d("TOKEN_CHECK", error.getLocalizedMessage() + "");
                        Toast.makeText(TokenActivity.this, error.getLocalizedMessage() + "", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", binding.token.getText().toString().trim());
                return params;
            }
        };


        requestQueue.add(stringRequest);
    }

}