package ataa.eyec;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import java.util.Locale;

import android.widget.Toast;

public class TextToSpeechActivity extends Activity implements OnInitListener {

    private TextToSpeech tts;
    private CharSequence text;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tts = new TextToSpeech(TextToSpeechActivity.this, TextToSpeechActivity.this);
        Intent message = getIntent();
        text = message.getStringExtra("message");
    }

    @Override
    public void onInit(int status) {

        if(status == TextToSpeech.SUCCESS) {

            int speechResult = tts.setLanguage(Locale.UK);
            if (speechResult == TextToSpeech.LANG_MISSING_DATA) {

                Log.e("TTS", "Language not supported");
            } else {

                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                Log.e("TTS", "Language is ok.");

                while(tts.isSpeaking()) {

                }

                finish();
            }
        } else {

            Log.e("TTS", "Failed to initialize TextToSpeech service.");
            Toast.makeText(this, "Failed to speak.", Toast.LENGTH_SHORT).show();
        }

    }

    public void onDestroy() {

        tts.shutdown();
        super.onDestroy();
    }
}