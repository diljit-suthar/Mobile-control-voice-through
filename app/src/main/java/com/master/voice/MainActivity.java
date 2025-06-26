package com.master.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vosk.Model;
import com.vosk.Recognizer;
import com.vosk.Vosk;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 1;
    private Recognizer recognizer;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Vosk API
        initVosk();

        // Check for microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_PERMISSION);
        } else {
            startListening();
        }
    }

    private void initVosk() {
        try {
            // Initialize Vosk and the model
            Vosk.setLogLevel(0); // Disable unnecessary logs
            model = new Model(getAssets().open("vosk-model-small-en-us-0.15"));
            recognizer = new Recognizer(model, 16000.0f);
            Toast.makeText(this, "Vosk model loaded successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing Vosk: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startListening() {
        try {
            // Start listening for voice input
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please speak now...");

            startActivityForResult(intent, 1001);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error starting listening: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = result.get(0);

            // Handle the recognized text
            handleRecognizedText(spokenText);
        }
    }

    private void handleRecognizedText(String spokenText) {
        Toast.makeText(this, "You said: " + spokenText, Toast.LENGTH_SHORT).show();

        // Example voice commands handling
        if (spokenText.contains("OTG on")) {
            // Trigger OTG on action
            Toast.makeText(this, "Turning OTG on", Toast.LENGTH_SHORT).show();
        } else if (spokenText.contains("call father")) {
            // Trigger call action
            Toast.makeText(this, "Calling Father...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Command not recognized.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources
        if (recognizer != null) {
            recognizer.close();
        }
        if (model != null) {
            model.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_LONG).show();
            }
        }
    }
}
