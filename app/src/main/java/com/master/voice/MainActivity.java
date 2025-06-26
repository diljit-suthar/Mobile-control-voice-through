package com.master.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;
import org.vosk.android.RecognitionListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private Model model;
    private SpeechService speechService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request microphone permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSIONS_REQUEST_RECORD_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initModel();
            } else {
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void initModel() {
        new Thread(() -> {
            try {
                model = new Model("model"); // 'model' folder must be in assets
                Recognizer recognizer = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to initialize model", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onPartialResult(String hypothesis) {
        // Optional: handle partial result
    }

    @Override
    public void onResult(String hypothesis) {
        runOnUiThread(() -> Toast.makeText(this, "Result: " + hypothesis, Toast.LENGTH_LONG).show());
        // TODO: Parse `hypothesis` and perform command actions (like OTG on, call, etc.)
    }

    @Override
    public void onFinalResult(String hypothesis) {
        // Optional
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onTimeout() {
        runOnUiThread(() ->
                Toast.makeText(this, "Speech timeout", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }
    }
}
