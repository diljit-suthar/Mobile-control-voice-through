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

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private TextView resultView;
    private SpeechService speechService;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = findViewById(R.id.result_text_view);
        resultView.setText("Initializing...");

        if (!hasPermissions()) {
            requestPermissions();
        } else {
            loadModelFromStorage();
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },
                PERMISSIONS_REQUEST_CODE);
    }

    private void loadModelFromStorage() {
        new Thread(() -> {
            try {
                runOnUiThread(() -> resultView.setText("Loading model from storage..."));
                File modelPath = new File(Environment.getExternalStorageDirectory(), "MasterVoiceModel/model");
                model = new Model(modelPath.getAbsolutePath());

                Recognizer recognizer = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(this);

                runOnUiThread(() -> resultView.setText("Listening started"));
            } catch (IOException e) {
                runOnUiThread(() -> {
                    resultView.setText("Model load error: " + e.getMessage());
                    Toast.makeText(this, "Model load failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (hasPermissions()) {
                loadModelFromStorage();
            } else {
                resultView.setText("Permissions denied");
            }
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        runOnUiThread(() -> resultView.setText("Partial: " + hypothesis));
    }

    @Override
    public void onResult(String hypothesis) {
        runOnUiThread(() -> resultView.setText("Result: " + hypothesis));
    }

    @Override
    public void onFinalResult(String hypothesis) {
        runOnUiThread(() -> resultView.setText("Final: " + hypothesis));
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> resultView.setText("Error: " + e.getMessage()));
    }

    @Override
    public void onTimeout() {
        runOnUiThread(() -> resultView.setText("Listening timeout"));
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
