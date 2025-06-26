package com.master.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.SpeechService;
import org.vosk.android.RecognitionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView resultView;
    private SpeechService speechService;
    private Model model;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        resultView = findViewById(R.id.result_text_view);
        resultView.setText("Starting app...");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            loadModelAndStart();
        }
    }

    private void loadModelAndStart() {
        new Thread(() -> {
            try {
                runOnUiThread(() -> resultView.setText("Loading model..."));
                // Option A: unzip to internal memory
                File modelDir = new File(getFilesDir(), "model");
                if (!modelDir.exists()) {
                    Utils.unpackZip(getAssets().open("model.zip"), modelDir);
                }
                model = new Model(modelDir.getAbsolutePath());
                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);
                runOnUiThread(() -> resultView.setText("Model loaded, listening..."));
            } catch (IOException e) {
                runOnUiThread(() -> {
                    resultView.setText("Model load error: " + e.getMessage());
                    Toast.makeText(this, "Failed loading model: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int rc, String[] perms, int[] grants) {
        super.onRequestPermissionsResult(rc, perms, grants);
        if (rc == PERMISSIONS_REQUEST_RECORD_AUDIO &&
            grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) {
            loadModelAndStart();
        } else {
            resultView.setText("Permission denied");
        }
    }

    @Override public void onPartialResult(String hyp) { runOnUiThread(() -> resultView.setText("Partial: " + hyp)); }
    @Override public void onResult(String hyp)        { runOnUiThread(() -> resultView.setText("Result: " + hyp)); }
    @Override public void onFinalResult(String hyp)   { runOnUiThread(() -> resultView.setText("Final: " + hyp)); }
    @Override public void onError(Exception e)        { runOnUiThread(() -> resultView.setText("Error: " + e.getMessage())); }
    @Override public void onTimeout()                 { runOnUiThread(() -> resultView.setText("Listening timed out")); }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (speechService != null) {
            speechService.stop();
            speechService.shutdown();
        }
    }
}
