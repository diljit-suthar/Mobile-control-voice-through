package com.master.masterapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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
import org.vosk.android.SpeechStreamService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final String TAG = "MasterApp";

    private SpeechService speechService;
    private TextToSpeech tts;
    private TextView resultTextView;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextView = findViewById(R.id.result_text_view);

        // Request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            initModelAndRecognizer();
        }

        // Initialize Text to Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void initModelAndRecognizer() {
        new Thread(() -> {
            try {
                File modelPath = copyAssetsToStorage(this, "model");
                model = new Model(modelPath.getAbsolutePath());
                speechService = new SpeechService(model, 16000.0f);
                speechService.startListening(this);
                runOnUiThread(() -> speak("Voice recognition started"));
            } catch (IOException e) {
                Log.e(TAG, "Model init failed", e);
            }
        }).start();
    }

    private File copyAssetsToStorage(Context context, String folderName) throws IOException {
        File outDir = new File(context.getExternalFilesDir(null), folderName);
        if (!outDir.exists()) outDir.mkdirs();

        AssetManager assetManager = context.getAssets();
        String[] files = assetManager.list(folderName);
        for (String filename : files) {
            File outFile = new File(outDir, filename);
            if (!outFile.exists()) {
                try (InputStream in = assetManager.open(folderName + "/" + filename);
                     OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
            }
        }
        return outDir;
    }

    @Override
    public void onResult(String hypothesis) {
        runOnUiThread(() -> {
            resultTextView.setText(hypothesis);
            processCommand(hypothesis);
        });
    }

    @Override
    public void onPartialResult(String hypothesis) {
        // Optional: can show live voice text
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "SpeechService error: ", e);
        runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onTimeout() {
        runOnUiThread(() -> speak("Timeout"));
    }

    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void processCommand(String text) {
        String cmd = text.toLowerCase();

        if (cmd.contains("otg on")) {
            speak("Opening OTG settings");
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);
        } else if (cmd.contains("call my father") || cmd.contains("call father")) {
            speak("Calling father");
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:1234567890")); // Replace with real number
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 2);
            }
        } else {
            speak("Command not recognized");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initModelAndRecognizer();
            } else {
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
