package com.moutamid.secretservice.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.databinding.ActivityTokenBinding;
import com.moutamid.secretservice.utilis.Constants;

public class TokenActivity extends AppCompatActivity {
    ActivityTokenBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTokenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.back.setOnClickListener(v -> onBackPressed());

        binding.link.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://secret-service.be/"))));

        if (Stash.getBoolean(Constants.IS_TOKEN_VERIFY, false)){
            binding.validated.setVisibility(View.VISIBLE);
            binding.notValidated.setVisibility(View.GONE);
        } else {
            binding.validated.setVisibility(View.GONE);
            binding.notValidated.setVisibility(View.VISIBLE);
        }

        if (!Stash.getString(Constants.TOKEN, "").isEmpty()){
            binding.token.setText(Stash.getString(Constants.TOKEN, ""));
        }

        binding.verify.setOnClickListener(v -> {
            Stash.put(Constants.TOKEN, binding.token.getText().toString().trim());
            Stash.put(Constants.IS_TOKEN_VERIFY, true);
            Toast.makeText(this, "Verified", Toast.LENGTH_SHORT).show();
            binding.validated.setVisibility(View.VISIBLE);
            binding.notValidated.setVisibility(View.GONE);
        });

    }
}