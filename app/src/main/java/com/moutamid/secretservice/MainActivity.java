package com.moutamid.secretservice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.moutamid.secretservice.activities.NoContactsActivity;
import com.moutamid.secretservice.activities.ReplyActivity;
import com.moutamid.secretservice.activities.SetTimerActivity;
import com.moutamid.secretservice.activities.TokenActivity;
import com.moutamid.secretservice.databinding.ActivityMainBinding;
import com.moutamid.secretservice.utilis.Constants;
import com.moutamid.secretservice.utilis.VolleySingleton;

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

        binding.token.setOnClickListener(v -> {
            startActivity(new Intent(this, TokenActivity.class));
        });

        requestQueue = VolleySingleton.getInstance(MainActivity.this).getRequestQueue();

    }

    @Override
    protected void onResume() {
        super.onResume();
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.API_STANDARD_MESSAGE, null,
                response -> {
                    /**
                     Stash.put(Constants.TOKEN, binding.token.getText().toString().trim());
                     Stash.put(Constants.IS_TOKEN_VERIFY, true);

                     binding.validated.setVisibility(View.VISIBLE);
                     binding.notValidated.setVisibility(View.GONE);
                     **/
                },
                error -> {
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

}