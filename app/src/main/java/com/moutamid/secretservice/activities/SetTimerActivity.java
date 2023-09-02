package com.moutamid.secretservice.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.moutamid.secretservice.R;
import com.moutamid.secretservice.databinding.ActivitySetTimerBinding;
import com.moutamid.secretservice.utilis.Constants;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SetTimerActivity extends AppCompatActivity {
    ActivitySetTimerBinding binding;
    final Calendar calendar = Calendar.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetTimerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.back.setOnClickListener(v -> onBackPressed());

        if (Stash.getInt(Constants.TIME, 3) == 0){
            binding.timeActivate.setChecked(true);
        } else if (Stash.getInt(Constants.TIME, 3) == 1){
            binding.dateActivate.setChecked(true);
        } else if (Stash.getInt(Constants.TIME, 3) == 2){
            binding.weekActivate.setChecked(true);
        }

        binding.fromTime.setText(Constants.getFormattedTime(Stash.getLong(Constants.FROM_TIME, 0)));
        binding.toTime.setText(Constants.getFormattedTime(Stash.getLong(Constants.TO_TIME, 0)));
        binding.fromWeek.setText(Constants.getFormattedTime(Stash.getLong(Constants.FROM_WEEK, 0)));
        binding.toWeek.setText(Constants.getFormattedTime(Stash.getLong(Constants.TO_WEEK, 0)));
        binding.fromTimeStart.setText(Constants.getFormattedTime(Stash.getLong(Constants.START_DAY_TIME, 0)));
        binding.fromTimeEnd.setText(Constants.getFormattedTime(Stash.getLong(Constants.END_DAY_TIME, 0)));

        binding.fromDayStart.setText(Constants.getFormattedDay(Stash.getLong(Constants.START_DAY, 0)));
        binding.fromDayEnd.setText(Constants.getFormattedDay(Stash.getLong(Constants.END_DAY, 0)));

        DatePickerDialog.OnDateSetListener dateStart = (datePicker, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            binding.fromDayStart.setText(Constants.getFormattedDay(calendar.getTime().getTime()));
            Stash.put(Constants.START_DAY, calendar.getTime().getTime());
        };
        DatePickerDialog.OnDateSetListener dateEnd = (datePicker, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            binding.fromDayEnd.setText(Constants.getFormattedDay(calendar.getTime().getTime()));
            Stash.put(Constants.END_DAY, calendar.getTime().getTime());
        };

        binding.timeActivate.setOnClickListener(v -> {
            Stash.put(Constants.TIME, 0);
            binding.timeActivate.setChecked(true);
            binding.dateActivate.setChecked(false);
            binding.weekActivate.setChecked(false);
        });

        binding.dateActivate.setOnClickListener(v -> {
            Stash.put(Constants.TIME, 1);
            binding.timeActivate.setChecked(false);
            binding.dateActivate.setChecked(true);
            binding.weekActivate.setChecked(false);
        });

        binding.weekActivate.setOnClickListener(v -> {
            Stash.put(Constants.TIME, 2);
            binding.timeActivate.setChecked(false);
            binding.dateActivate.setChecked(false);
            binding.weekActivate.setChecked(true);
        });

        binding.fromTime.setOnClickListener(v -> openTimePicker((TextView) v , Constants.FROM_TIME));
        binding.toTime.setOnClickListener(v -> openTimePicker((TextView) v , Constants.TO_TIME));
        binding.fromWeek.setOnClickListener(v -> openTimePicker((TextView) v , Constants.FROM_WEEK));
        binding.toWeek.setOnClickListener(v -> openTimePicker((TextView) v , Constants.TO_WEEK));
        binding.fromTimeStart.setOnClickListener(v -> openTimePicker((TextView) v , Constants.START_DAY_TIME));
        binding.fromTimeEnd.setOnClickListener(v -> openTimePicker((TextView) v , Constants.END_DAY_TIME));

        binding.fromDayStart.setOnClickListener(v -> {
            new DatePickerDialog(this, dateStart, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        binding.fromDayEnd.setOnClickListener(v -> {
            new DatePickerDialog(this, dateEnd, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

    }

    private void openTimePicker(TextView textView, String constant) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Time")
                .setPositiveButtonText("Add")
                .setNegativeButtonText("Cancel")
                .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            long reminder = calendar.getTimeInMillis();
            String form = "HH:mm";
            String formattedTime = new SimpleDateFormat(form, Locale.getDefault()).format(reminder);
            textView.setText(formattedTime);
            Stash.put(constant, reminder);
        });

        timePicker.show(getSupportFragmentManager(), "timePicker");
    }

}