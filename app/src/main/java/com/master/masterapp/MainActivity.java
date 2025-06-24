package com.master.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;
import org.vosk.android.RecognitionListener;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends Activity implements RecognitionListener {

    private SpeechService speechService;
    private TextView txt_result;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_result = findViewById(R.id.txt_result);
        Button btn_voice = findViewById(R.id.btn_voice);

        // Request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        try {
            Model model = new Model(getAssets(), "model");
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            speechService = new SpeechService(recognizer, 16000.0f);
            speechService.startListening(this);
        } catch (IOException e) {
            txt_result.setText("Error loading model: " + e.getMessage());
        }

        btn_voice.setOnClickListener(v -> startSpeechRecognition());
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        startActivityForResult(intent, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, proceed
        } else {
            txt_result.setText("Permission denied");
        }
    }

    @Override
    public void onResult(String hypothesis) {
        runOnUiThread(() -> txt_result.setText(hypothesis));
    }

    @Override
    public void onFinalResult(String hypothesis) {
        runOnUiThread(() -> txt_result.setText(hypothesis));
        if (hypothesis.toLowerCase().contains("otg on")) {
            // Implement OTG toggle
        } else if (hypothesis.toLowerCase().contains("call my father")) {
            // Trigger call intent
            callContact("Father");
        }
    }

    private void callContact(String contactName) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + contactName));
        startActivity(callIntent);
    }

    @Override
    public void onPartialResult(String hypothesis) {}

    @Override
    public void onError(Exception e) {
        txt_result.setText("Error: " + e.getMessage());
    }

    @Override
    public void onTimeout() {}
}
