package com.moutamid.secretservice.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.moutamid.secretservice.R;
import com.moutamid.secretservice.databinding.ActivityReplyBinding;

public class ReplyActivity extends AppCompatActivity {
    ActivityReplyBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReplyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.back.setOnClickListener(v -> onBackPressed());



    }
}