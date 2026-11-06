
package com.jarvis.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView status;
    private SpeechRecognizer recognizer;
    private TextToSpeech tts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 60, 32, 32);

        TextView title = new TextView(this);
        title.setText("J.A.R.V.I.S.");
        title.setTextSize(30);
        layout.addView(title);

        status = new TextView(this);
        status.setText("Your assistant is ready.");
        status.setTextSize(18);
        layout.addView(status);

        Button listen = new Button(this);
        listen.setText("TAP TO SPEAK");
        layout.addView(listen);

        setContentView(layout);

        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    status.setText("I'm listening, sir.");
                    speak("I'm listening, sir.");
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches =
                        results.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION);

                    if (matches != null && !matches.isEmpty()) {
                        handleCommand(matches.get(0));
                    }
                }

                @Override
                public void onError(int error) {
                    status.setText("Couldn't hear that. Tap to try again.");
                }

                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float value) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onPartialResults(Bundle results) {}
                @Override public void onEvent(int type, Bundle params) {}
            });
        }

        listen.setOnClickListener(v -> startListening());
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            return;
        }

        if (recognizer == null) {
            status.setText("Speech recognition is unavailable.");
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        recognizer.startListening(intent);
    }

    private void handleCommand(String command) {
        String text = command.toLowerCase(Locale.ROOT);
        status.setText("You said: " + command);

        String[] apps = {"whatsapp", "youtube", "chrome", "spotify"};

        for (String app : apps) {
            if (text.contains(app)) {
                try {
                    String packageName;

                    switch (app) {
                        case "whatsapp":
                            packageName = "com.whatsapp";
                            break;
                        case "youtube":
                            packageName = "com.google.android.youtube";
                            break;
                        case "chrome":
                            packageName = "com.android.chrome";
                            break;
                        default:
                            packageName = "com.spotify.music";
                    }

                    Intent launch = getPackageManager()
                        .getLaunchIntentForPackage(packageName);

                    if (launch != null) {
                        speak("Opening " + app);
                        startActivity(launch);
                    } else {
                        speak(app + " is not available.");
                    }
                } catch (Exception e) {
                    speak("I couldn't open " + app);
                }
                return;
            }
        }

        speak("I heard you say " + command);
    }

    private void speak(String message) {
        if (tts != null) {
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis");
        }
    }

    @Override
    protected void onDestroy() {
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }
}
