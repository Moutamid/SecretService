package com.moutamid.secretservice.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fxn.stash.Stash;
import com.moutamid.secretservice.MainActivity;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.databinding.ActivityUpdateBinding;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdateActivity extends AppCompatActivity {
    ActivityUpdateBinding binding;
    RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.initDialog(this);

        binding.toolbar.back.setOnClickListener(v -> onBackPressed());

        requestQueue = VolleySingleton.getInstance(UpdateActivity.this).getRequestQueue();

        binding.save.setOnClickListener(v -> updateMessage());

        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.setWebViewClient(new WebViewClient());
        String url = "https://secret-service.be/app_standard_message.php?token=" + Stash.getString(Constants.TOKEN, "");
        binding.webview.loadUrl(url);

    }

    private void updateMessage() {
        Constants.showDialog();
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
                                Toast.makeText(UpdateActivity.this, "Message Updated", Toast.LENGTH_LONG).show();
                            } else {
                                Stash.put(Constants.MESSAGE, "");
                                Toast.makeText(UpdateActivity.this, "Message is empty", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(UpdateActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        runOnUiThread(Constants::dismissDialog);
                        Log.d("TOKEN_CHECK", error.getLocalizedMessage() + "");
                        Toast.makeText(UpdateActivity.this, error.getLocalizedMessage()+"", Toast.LENGTH_LONG).show();
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