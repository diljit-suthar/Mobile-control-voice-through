package com.master.masterapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.SpeechService;
import org.vosk.android.RecognitionListener;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private TextView resultTextView;
    private SpeechService speechService;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultTextView = findViewById(R.id.txt_result); // match your XML

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initModel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initModel();
        } else {
            finish();
        }
    }

    private void initModel() {
        new Thread(() -> {
            try {
                model = new Model(getAssets(), "model");
                Recognizer recognizer = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(this);
            } catch (IOException e) {
                runOnUiThread(() -> resultTextView.setText("Error loading model: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onPartialResult(String hypothesis) {
        runOnUiThread(() -> resultTextView.setText(hypothesis));
    }

    @Override
    public void onResult(String hypothesis) {
        runOnUiThread(() -> resultTextView.setText(hypothesis));
    }

    @Override
    public void onFinalResult(String hypothesis) {
        runOnUiThread(() -> {
            resultTextView.setText(hypothesis);
            processCommand(hypothesis);
        });
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> resultTextView.setText("Error: " + e.getMessage()));
    }

    @Override
    public void onTimeout() {
        runOnUiThread(() -> resultTextView.setText("Timeout reached."));
    }

    private void processCommand(String command) {
        command = command.toLowerCase();
        if (command.contains("otg on")) {
            // Your OTG on logic
        } else if (command.contains("call my father")) {
            // Call logic
        }
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
