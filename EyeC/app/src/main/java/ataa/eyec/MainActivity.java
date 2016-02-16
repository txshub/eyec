package ataa.eyec;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static Map<String, String> PLACES_BY_BEACONS;
    private BeaconManager beaconManager;
    private Region region;
    private TextToSpeechActivity ttsManager;
    private static Map<String, String> placesByBeacons;
    private Button tapToSpeak;
    private TextView textFromSpeak;
    private ArrayList<String> zones;

    private static final int VOICE_RECOGNITION_REQUEST = 65535;
    private static final int SELECT_OPTION_REQUEST = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isInternetAvailable()) {
            //TODO: check if database has changed, update beacon list if so
        }

        //FOR DEMO
        placesByBeacons = new HashMap<>();
        zones = new ArrayList<String>();
        zones.add("Zone 1");
        placesByBeacons.put("64389:26560", zones.get(zones.size() - 1));
        zones.add("Zone 2");
        placesByBeacons.put("41166:56048", zones.get(zones.size() - 1));
       // zones.add("Zone 3");
        //placesByBeacons.put("37323:54525", zones.get(zones.size() - 1));
        //zones.add("Zone 4");
        //placesByBeacons.put("14547:61162", zones.get(zones.size() - 1));

        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);

        //textFromSpeak = (TextView) findViewById(R.id.recognitionText);
        tapToSpeak = (Button) findViewById(R.id.tapToSpeak);
        tapToSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakToMe(VOICE_RECOGNITION_REQUEST);
            }
        });

        //Tell a welcome message
        Intent welcome = new Intent(MainActivity.this, TextToSpeechActivity.class);
        welcome.putExtra("message", getString(R.string.welcomeText));
        startActivity(welcome);

        //Initialise beacon manager
        ttsManager = new TextToSpeechActivity();
        beaconManager = new BeaconManager(this);
        //beaconManager.setForegroundScanPeriod(5000, 15000);
        //beaconManager.setBackgroundScanPeriod(5000, 15000);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> bList) {
                if (!bList.isEmpty()) {

                    //Getting the known beacon with the best signal
                    int i = 0;
                    while (i < bList.size() && isKnown(bList.get(i)) == null)
                        i++;
                    if (i >= bList.size()) {
                        saySearching();
                        return;
                    }
                    Beacon nearestBeacon = bList.get(i);
                    String bestBName = isKnown(nearestBeacon);
                    Log.d("LISTA", "Plm: " + bList.toString());

                    //FOR TESTING PURPOSES, DELETE THIS <-------------------------------------------------------------------
                    //TextView text = (TextView) findViewById(R.id.text1);
                    //text.setText(bestBName);

                    if (bestBName != null) {
                        Intent voice = new Intent(MainActivity.this, TextToSpeechActivity.class);
                        voice.putExtra("message", getString(R.string.enteredPrefixText) + bestBName);
                        startActivity(voice);
                    } else
                        saySearching();
                }
            }
        });
        region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });

    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Checks whether the device has a working internet connection
     *
     * @return true/false
     */
    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.co.uk");

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the found beacon is known (in the database). If it is, the name is returned, otherwise null.
     *
     * @param beacon The beacon
     * @return The name/description of beacon (or null)
     */
    private String isKnown(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return null;
    }

    /**
     * Uses TTS to say a searching message
     */
    private void saySearching() {
        Intent voice = new Intent(MainActivity.this, TextToSpeechActivity.class);
        voice.putExtra("message", getString(R.string.searchingText));
        startActivity(voice);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 19)
            textFromSpeak.setText((String) data.getExtras().get("result"));
        else if (requestCode == VOICE_RECOGNITION_REQUEST && resultCode == RESULT_OK) {
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String firstMatch = matches.get(0).toString();
            //textFromSpeak.setText(firstMatch);

            //teste pe voce
            if (firstMatch.contains("list") || firstMatch.contains("lists")) {
                //Do shit

                for(int i = zones.size() - 1; i >= 0; i--) {
                    Intent numberVoice = new Intent(MainActivity.this, TextToSpeechActivity.class);
                    numberVoice.putExtra("message", (i+1) + ". " + zones.get(i));
                    startActivity(numberVoice);
                }

                // TODO Choose an option and get direction to the respective zone
            }
        } else if (requestCode == SELECT_OPTION_REQUEST && resultCode == RESULT_OK) {
            Intent numberVoice = new Intent(MainActivity.this, TextToSpeechActivity.class);
            ArrayList matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String firstMatch = matches.get(0).toString();
            numberVoice.putExtra("message", getString(R.string.youChoseText) + firstMatch);
            startActivity(numberVoice);

        }


        //TODO - other voice commands
    }

    public void speakToMe(int request) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Please speak slowly and enunciate clearly.");
        startActivityForResult(intent, request);
    }
}