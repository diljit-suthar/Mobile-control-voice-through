package com.master.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSION_CODE = 1001;
    private TextView resultText;
    private SpeechService speechService;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultText = findViewById(R.id.result_text);

        if (checkPermissions()) {
            initModel();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },
                PERMISSION_CODE);
    }

    private void initModel() {
        new Thread(() -> {
            try {
                File modelPath = new File(Environment.getExternalStorageDirectory(), "Master Voice Model/model");
                model = new Model(modelPath.getAbsolutePath());
                Recognizer recognizer = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(MainActivity.this);

                runOnUiThread(() -> resultText.setText("Listening..."));
            } catch (IOException e) {
                runOnUiThread(() -> {
                    resultText.setText("Model error: " + e.getMessage());
                    Toast.makeText(this, "Model load failed", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE && checkPermissions()) {
            initModel();
        } else {
            resultText.setText("Permission Denied");
        }
    }

    @Override public void onPartialResult(String hypothesis) {
        runOnUiThread(() -> resultText.setText("Partial: " + hypothesis));
    }

    @Override public void onResult(String hypothesis) {
        runOnUiThread(() -> resultText.setText("Result: " + hypothesis));
    }

    @Override public void onFinalResult(String hypothesis) {
        runOnUiThread(() -> resultText.setText("Final: " + hypothesis));
    }

    @Override public void onError(Exception e) {
        runOnUiThread(() -> resultText.setText("Error: " + e.getMessage()));
    }

    @Override public void onTimeout() {
        runOnUiThread(() -> resultText.setText("Timeout"));
    }

    @Override
    protected void onDestroy() {
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }
        super.onDestroy();
    }
}
