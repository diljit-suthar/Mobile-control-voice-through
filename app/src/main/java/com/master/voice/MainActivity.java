package com.master.voice;

import android.Manifest; import android.content.pm.PackageManager; import android.os.Bundle; import android.util.Log; import android.widget.Toast; import androidx.annotation.NonNull; import androidx.appcompat.app.AppCompatActivity; import androidx.core.app.ActivityCompat; import androidx.core.content.ContextCompat; import org.vosk.Model; import org.vosk.Recognizer; import org.vosk.android.RecognitionListener; import org.vosk.android.SpeechService; import java.io.IOException;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
private static final String TAG = "MainActivity";
private Model model;
private SpeechService speechService;
private boolean isModelReady = false;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toast.makeText(this, "App Started", Toast.LENGTH_SHORT).show();

    int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        return;
    }

    initializeModel();
}

private void initializeModel() {
    Toast.makeText(this, "Initializing Model...", Toast.LENGTH_SHORT).show();
    new Thread(() -> {
        try {
            model = new Model("model");
            isModelReady = true;
            runOnUiThread(() -> {
                Toast.makeText(this, "Model loaded successfully", Toast.LENGTH_SHORT).show();
                startListening();
            });
        } catch (IOException e) {
            Log.e(TAG, "Model loading failed", e);
            runOnUiThread(() -> Toast.makeText(this, "Model failed, basic UI started", Toast.LENGTH_LONG).show());
        }
    }).start();
}

private void startListening() {
    try {
        Recognizer recognizer = new Recognizer(model, 16000.0f);
        speechService = new SpeechService(recognizer, 16000.0f);
        speechService.startListening(this);
        Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
    } catch (IOException e) {
        Log.e(TAG, "SpeechService failed", e);
        Toast.makeText(this, "Speech service failed", Toast.LENGTH_LONG).show();
    }
}

@Override
public void onPartialResult(String hypothesis) {
    Log.d(TAG, "Partial: " + hypothesis);
}

@Override
public void onResult(String hypothesis) {
    Log.d(TAG, "Result: " + hypothesis);
    Toast.makeText(this, "Command: " + hypothesis, Toast.LENGTH_SHORT).show();
}

@Override
public void onFinalResult(String hypothesis) {
    Log.d(TAG, "Final Result: " + hypothesis);
}

@Override
public void onError(Exception e) {
    Log.e(TAG, "Error: ", e);
    Toast.makeText(this, "Recognition Error", Toast.LENGTH_SHORT).show();
}

@Override
public void onTimeout() {
    Toast.makeText(this, "Listening Timeout", Toast.LENGTH_SHORT).show();
}

@Override
protected void onDestroy() {
    super.onDestroy();
    if (speechService != null) {
        speechService.stop();
        speechService.shutdown();
    }
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeModel();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

}

