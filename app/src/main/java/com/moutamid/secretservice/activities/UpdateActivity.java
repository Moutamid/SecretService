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
import com.moutamid.secretservice.models.MessageModel;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
                response -> {
                    Log.d("TOKEN_CHECK", response.toString());
                    try {
                        JSONObject obj = new JSONObject(response);
                        String msg = obj.getString("msg");
                        Log.d("TOKEN_CHECK", msg);
                        if (!msg.isEmpty()) {
                            Stash.put(Constants.MESSAGE, msg);
                            String date = Constants.getFormattedDate(new Date().getTime());
                            Stash.put(Constants.UPDATED_TIME, date);
                            updateKeywords();
                        } else {
                            Stash.put(Constants.MESSAGE, "");
                            Constants.showToast(UpdateActivity.this, "Message is empty");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Constants.showToast(UpdateActivity.this, "Something went wrong!");
                    }
                },
                error -> {
                    runOnUiThread(Constants::dismissDialog);
                    Log.d("TOKEN_CHECK", error.getLocalizedMessage() + "");
                    Constants.showToast(UpdateActivity.this, error.getLocalizedMessage() + "");
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

    private void updateKeywords() {
        ArrayList<MessageModel> list = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.API_KEYWORD_MESSAGE,
                response -> {
                    runOnUiThread(Constants::dismissDialog);
                    Log.d("TOKEN_CHECK", response);
                    try {
                        JSONArray obj = new JSONArray(response);
                        for (int i = 0; i < obj.length(); i++) {
                            JSONObject jsonObject = obj.getJSONObject(i);
                            String keyword = jsonObject.getString("keyword");
                            String msg = jsonObject.getString("msg");

                            MessageModel model = new MessageModel(keyword, msg);
                            list.add(model);
                        }
                        Stash.clear(Constants.KEYWORDS_MESSAGE);
                        Stash.put(Constants.KEYWORDS_MESSAGE, list);
                        Constants.showToast(UpdateActivity.this, "Message saved");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Constants.showToast(UpdateActivity.this, "Something went wrong!");
                    }
                },
                error -> {
                    runOnUiThread(Constants::dismissDialog);
                    Log.d("TOKEN_CHECK", error.getLocalizedMessage() + "");
                    Constants.showToast(UpdateActivity.this, error.getLocalizedMessage() + "");
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