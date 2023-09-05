package com.moutamid.secretservice.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import com.fxn.stash.Stash;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.databinding.ActivityReplyBinding;
import com.moutamid.secretservice.utilis.Constants;

public class ReplyActivity extends AppCompatActivity {
    ActivityReplyBinding binding;
    String[] communication_channel = new String[]{};
    public static final String EXTRA_MESSAGE_TITLE = "EXTRA_MESSAGE_TITLE";
    public static final String EXTRA_MESSAGE_PACK = "EXTRA_MESSAGE_PACK";
    public static final String EXTRA_MESSAGE_SHOW = "EXTRA_MESSAGE_SHOW";

    public static Intent getStartIntent(Context context, String msgTitle, String pack, boolean isFromService) {
        Intent intent = new Intent(context, ReplyActivity.class);
        intent.putExtra(EXTRA_MESSAGE_TITLE, msgTitle);
        intent.putExtra(EXTRA_MESSAGE_PACK, pack);
        intent.putExtra(EXTRA_MESSAGE_SHOW, isFromService);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReplyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.back.setOnClickListener(v -> onBackPressed());

        communication_channel = Stash.getString(Constants.Communication_Channel, "").split(", ");

        if (!Constants.isNotificationServiceEnabled(ReplyActivity.this)){
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        updateUI();

        binding.telegram.setOnClickListener(v -> {
            boolean check = false;
            communication_channel = Stash.getString(Constants.Communication_Channel, "").split(", ");
            for (String channel : communication_channel) {
                if (channel.equals(Constants.TELEGRAM)) {
                   check = true;
                }
            }

            if (check){
                binding.telegram.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
                binding.telegramIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                binding.telegramText.setTextColor(getResources().getColor(R.color.text_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                String target = Constants.TELEGRAM + ", ";
                channels = channels.replace(target, "");
                Stash.put(Constants.Communication_Channel, channels);
            } else {
                binding.telegram.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.telegramIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.telegramText.setTextColor(getResources().getColor(R.color.bg_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                channels += Constants.TELEGRAM + ", ";
                Stash.put(Constants.Communication_Channel, channels);
            }

        });

        binding.skype.setOnClickListener(v -> {
            boolean check = false;
            communication_channel = Stash.getString(Constants.Communication_Channel, "").split(", ");
            for (String channel : communication_channel) {
                if (channel.equals(Constants.SKYPE)) {
                    check = true;
                }
            }

            if (check){
                binding.skype.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
                binding.skypeIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                binding.skypeText.setTextColor(getResources().getColor(R.color.text_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                String target = Constants.SKYPE + ", ";
                channels = channels.replace(target, "");
                Stash.put(Constants.Communication_Channel, channels);
            } else {
                binding.skype.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.skypeIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.skypeText.setTextColor(getResources().getColor(R.color.bg_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                channels += Constants.SKYPE + ", ";
                Stash.put(Constants.Communication_Channel, channels);
            }

        });

        binding.whatsapp.setOnClickListener(v -> {
            boolean check = false;
            communication_channel = Stash.getString(Constants.Communication_Channel, "").split(", ");
            for (String channel : communication_channel) {
                if (channel.equals(Constants.WHATSAPP)) {
                    check = true;
                }
            }

            if (check){
                binding.whatsapp.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
                binding.whatsIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                binding.whatsText.setTextColor(getResources().getColor(R.color.text_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                String target = Constants.WHATSAPP + ", ";
                channels = channels.replace(target, "");
                Stash.put(Constants.Communication_Channel, channels);
            } else {
                binding.whatsapp.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.whatsIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.whatsText.setTextColor(getResources().getColor(R.color.bg_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                channels += Constants.WHATSAPP + ", ";
                Stash.put(Constants.Communication_Channel, channels);
            }

        });

        binding.missedCall.setOnClickListener(v -> {
            boolean check = false;
            communication_channel = Stash.getString(Constants.Communication_Channel, "").split(", ");
            for (String channel : communication_channel) {
                if (channel.equals(Constants.MISSED_CALLS)) {
                    check = true;
                }
            }

            if (check){
                binding.missedCall.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
                binding.missedIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                binding.missedText.setTextColor(getResources().getColor(R.color.text_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                String target = Constants.MISSED_CALLS + ", ";
                channels = channels.replace(target, "");
                Stash.put(Constants.Communication_Channel, channels);
            } else {
                binding.missedCall.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.missedIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.missedText.setTextColor(getResources().getColor(R.color.bg_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                channels += Constants.MISSED_CALLS + ", ";
                Stash.put(Constants.Communication_Channel, channels);
            }

        });

        binding.refusedCall.setOnClickListener(v -> {
            boolean check = false;
            communication_channel = Stash.getString(Constants.Communication_Channel, "").split(", ");
            for (String channel : communication_channel) {
                if (channel.equals(Constants.REFUSED_CALLS)) {
                    check = true;
                }
            }

            if (check){
                binding.refusedCall.setCardBackgroundColor(getResources().getColor(R.color.bg_color_trans));
                binding.refusedIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                binding.refusedText.setTextColor(getResources().getColor(R.color.text_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                String target = Constants.REFUSED_CALLS + ", ";
                channels = channels.replace(target, "");
                Stash.put(Constants.Communication_Channel, channels);
            } else {
                binding.refusedCall.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.refusedIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.refusedText.setTextColor(getResources().getColor(R.color.bg_color));
                String channels = Stash.getString(Constants.Communication_Channel, "");
                channels += Constants.REFUSED_CALLS + ", ";
                Stash.put(Constants.Communication_Channel, channels);
            }

        });

    }

    private void updateUI() {
        for (String channel : communication_channel) {
            if (channel.equals(Constants.TELEGRAM)) {
                binding.telegram.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.telegramIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.telegramText.setTextColor(getResources().getColor(R.color.bg_color));
            }
            if (channel.equals(Constants.SKYPE)) {
                binding.skype.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.skypeIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.skypeText.setTextColor(getResources().getColor(R.color.bg_color));
            }
            if (channel.equals(Constants.WHATSAPP)) {
                binding.whatsapp.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.whatsIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.whatsText.setTextColor(getResources().getColor(R.color.bg_color));
            }
            if (channel.equals(Constants.MISSED_CALLS)) {
                binding.missedCall.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.missedIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.missedText.setTextColor(getResources().getColor(R.color.bg_color));
            }
            if (channel.equals(Constants.REFUSED_CALLS)) {
                binding.refusedCall.setCardBackgroundColor(getResources().getColor(R.color.text_color));
                binding.refusedIcon.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.bg_color)));
                binding.refusedText.setTextColor(getResources().getColor(R.color.bg_color));
            }
        }
    }
}