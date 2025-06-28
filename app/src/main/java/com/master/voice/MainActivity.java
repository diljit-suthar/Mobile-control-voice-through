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

    private static final int PERMISSION_CODE = 100;
    private TextView resultText;
    private SpeechService speechService;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultText = findViewById(R.id.result_text_view);
        resultText.setText("Waiting for permission...");

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermissions()) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, PERMISSION_CODE);
            } else {
                initModel();
            }
        } else {
            initModel();
        }
    }

    private boolean checkPermissions() {
        int audioPerm = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int storagePerm = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return audioPerm == PackageManager.PERMISSION_GRANTED &&
               storagePerm == PackageManager.PERMISSION_GRANTED;
    }

    private void initModel() {
        resultText.setText("Loading model...");

        new Thread(() -> {
            try {
                File modelPath = new File(Environment.getExternalStorageDirectory(), "Master Voice Model/model");
                if (!modelPath.exists()) {
                    runOnUiThread(() -> resultText.setText("Model not found in storage."));
                    return;
                }

                model = new Model(modelPath.getAbsolutePath());
                Recognizer recognizer = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(recognizer, 16000.0f);
                speechService.startListening(this);

                runOnUiThread(() -> resultText.setText("Listening started..."));
            } catch (IOException e) {
                runOnUiThread(() -> {
                    resultText.setText("Model load error: " + e.getMessage());
                    Toast.makeText(this, "Failed to load model", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                initModel();
            } else {
                resultText.setText("Permission Denied");
                Toast.makeText(this, "Permissions are required", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        runOnUiThread(() -> resultText.setText("Partial: " + hypothesis));
    }

    @Override
    public void onResult(String hypothesis) {
        runOnUiThread(() -> resultText.setText("Result: " + hypothesis));
    }

    @Override
    public void onFinalResult(String hypothesis) {
        runOnUiThread(() -> resultText.setText("Final: " + hypothesis));
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(() -> resultText.setText("Error: " + e.getMessage()));
    }

    @Override
    public void onTimeout() {
        runOnUiThread(() -> resultText.setText("Timeout: No speech detected"));
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
