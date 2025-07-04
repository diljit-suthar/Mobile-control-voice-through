package com.master.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
        resultText.setText("Checking permissions...");

        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.RECEIVE_SMS
                    }, PERMISSION_CODE);
        } else {
            initModel();
        }
    }

    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void initModel() {
        resultText.setText("Loading model from assets...");

        new Thread(() -> {
            try {
                model = new Model(getAssets(), "model");
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
