package com.master.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextView resultView;
    private SpeechService speechService;
    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = findViewById(R.id.result_text_view);
        resultView.setText("Starting...");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            extractAndLoadModel();
        }
    }

    private void extractAndLoadModel() {
        new Thread(() -> {
            try {
                runOnUiThread(() -> resultView.setText("Extracting model..."));
                File modelDir = new File(getFilesDir(), "model");
                if (!modelDir.exists()) {
                    copyAssets("model", modelDir);
                }

                runOnUiThread(() -> resultView.setText("Loading model..."));
                model = new Model(modelDir.getAbsolutePath());

                Recognizer rec = new Recognizer(model, 16000.0f);
                speechService = new SpeechService(rec, 16000.0f);
                speechService.startListening(this);

                runOnUiThread(() -> resultView.setText("Model loaded, listening..."));

            } catch (IOException e) {
                runOnUiThread(() -> {
                    resultView.setText("Model error: " + e.getMessage());
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void copyAssets(String assetPath, File outDir) throws IOException {
        String[] assets = getAssets().list(assetPath);
        if (assets == null || assets.length == 0) {
            try (InputStream in = getAssets().open(assetPath)) {
                File outFile = new File(outDir.getParentFile(), assetPath);
                try (FileOutputStream out = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
            }
        } else {
            if (!outDir.exists()) outDir.mkdirs();
            for (String file : assets) {
                copyAssets(assetPath + "/" + file, new File(outDir, file));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            extractAndLoadModel();
        } else {
            resultView.setText("Permission denied");
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
