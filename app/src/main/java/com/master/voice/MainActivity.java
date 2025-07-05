package com.master.voice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
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
import java.util.ArrayList;
import java.util.List;

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
        resultText.setText("Checking permissions...");
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsList = new ArrayList<>();
        permissionsList.add(Manifest.permission.RECORD_AUDIO);
        permissionsList.add(Manifest.permission.RECEIVE_SMS);
        permissionsList.add(Manifest.permission.READ_SMS);
        permissionsList.add(Manifest.permission.SEND_SMS);
        permissionsList.add(Manifest.permission.CALL_PHONE);
        permissionsList.add(Manifest.permission.READ_PHONE_STATE);

        // Android 13+ media permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsList.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissionsList.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        boolean allGranted = true;
        for (String permission : permissionsList) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this,
                    permissionsList.toArray(new String[0]), PERMISSION_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } else {
            initModel();
        }
    }

    private void initModel() {
        resultText.setText("Loading model...");

        new Thread(() -> {
            try {
                File externalModelDir = new File(Environment.getExternalStorageDirectory(), "model");
                File internalModelDir = new File(getFilesDir(), "model");

                if (externalModelDir.exists() && externalModelDir.isDirectory()) {
                    model = new Model(externalModelDir.getAbsolutePath());
                } else {
                    if (!internalModelDir.exists()) {
                        Assets.copyAssetDirToInternalStorage(this, "model", "model");
                    }
                    model = new Model(internalModelDir.getAbsolutePath());
                }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                checkAndRequestPermissions(); // Re-check for MANAGE_EXTERNAL_STORAGE if needed
            } else {
                resultText.setText("Permission Denied");
                Toast.makeText(this, "Permissions are required", Toast.LENGTH_LONG).show();
            }
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
