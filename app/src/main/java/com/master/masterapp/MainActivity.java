package com.master.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.IOException;

public class MainActivity extends Activity implements RecognitionListener {

    private TextView txtResult;
    private SpeechService speechService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResult = findViewById(R.id.txt_result);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE},
                    1);
        }

        try {
            Model model = new Model(getAssets(), "model");
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(recognizer, 16000.0f);
            speechService.startListening(this);
        } catch (IOException e) {
            txtResult.setText("Error loading model: " + e.getMessage());
        }
    }

    @Override
    public void onResult(String hypothesis) {
        txtResult.setText(hypothesis);
    }

    @Override
    public void onFinalResult(String command) {
        txtResult.setText(command);
        command = command.toLowerCase();

        if (command.contains("call my father")) {
            callNumber("1234567890"); // Replace this number
        } else if (command.contains("otg on")) {
            toggleOTG();
        } else if (command.contains("open youtube")) {
            openApp("com.google.android.youtube");
        }
    }

    private void callNumber(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        startActivity(intent);
    }

    private void openApp(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);
        }
    }

    private void toggleOTG() {
        txtResult.setText("OTG toggle command received.");
        // TO DO: Add real OTG toggle logic here (Shizuku/root/intent if possible)
    }

    @Override public void onPartialResult(String hypothesis) {}
    @Override public void onError(Exception e) {
        txtResult.setText("Error: " + e.getMessage());
    }
    @Override public void onTimeout() {}
}
