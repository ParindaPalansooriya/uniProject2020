package com.example.myapplication.temp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.ListItem;
import com.example.myapplication.MyAdapter;
import com.example.myapplication.R;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class FullscreenActivity extends AppCompatActivity {

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    //private View mContentView;
    /*private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };*/
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };



    /** my parameters **/

    static String mainDomain = "http://192.168.8.100:8080";/** http://device IP:host port number/API endpoint **/
    Thread thread;
    JSONObject[] returnPOSTreturn={new JSONObject()};

    TextView text2;
    ImageView image;
    ListView listView;
    final boolean[] isFinish = {true};
    final boolean[] isStop = {false};
    CountDownTimer count;
    SpeechRecognizer speechRecognizer;
    TextToSpeech speecher;
    boolean isListing = false;

    ArrayList<ListItem> massageList = new ArrayList<>();
    MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content);
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hide();
            }
        });*/
        returnPOSTreturn[0] = null;
        /** Do my coding in here ****/
        text2 = findViewById(R.id.textView3);
        image = findViewById(R.id.imageView);
        listView = findViewById(R.id.listMassage);
        adapter = new MyAdapter(this,massageList);
        listView.setAdapter(adapter);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        checkPermission();
        count = new CountDownTimer(30000,100){
            @Override
            public void onTick(long millisUntilFinished) {
                if (isFinish[0]) {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }

                if (returnPOSTreturn != null) {
                    if (returnPOSTreturn[0] != null) {
                        System.out.println("test responce : " + returnPOSTreturn[0]);
                        if ((String) returnPOSTreturn[0].get("name") != null) {
                            addItems((String) returnPOSTreturn[0].get("name"), true);
                            Speecher((String) returnPOSTreturn[0].get("name"));
                        } else {
                            Speecher("Please Try again!");
                            addItems("Please Try again!", true);
                        }
                        returnPOSTreturn[0] = null;
                    }
                }
            }

            @Override
            public void onFinish() {
                speechRecognizer.stopListening();
            }
        };
        startSpeechToText(speechRecognizer);
        count.start();

        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count.start();
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        //mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /** MY Methords **/

    private void startSpeechToText(SpeechRecognizer speechRecognizer) {

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                isListing=true;
                image.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_audio_online));
                isFinish[0] = false;
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                isListing = false;
                image.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_audio_busy));
                isFinish[0] = true;
            }

            @Override
            public void onError(int error) {

            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    text2.setText(matches.get(0));
                    addItems(matches.get(0),false);
                    JSONObject json = new JSONObject();
                    //json.put("text",matches.get(0));
                    json.put("name",matches.get(0));
                    json.put("password",matches.get(0));
                    json.put("email",matches.get(0));
                    try {
                        POSTRequest("/user", json);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //callWebUrl(matches.get(0));
                    Speecher(matches.get(0));
                    if (matches.get(0).equals("stop")){
                        speecher.stop();
                        speecher.shutdown();
                        finish();
                    }
                    count.cancel();
                    isStop[0] = true;
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public JSONObject POSTRequest(final String apiURL, final JSONObject json) throws IOException, ParseException, JSONException {
        returnPOSTreturn[0] = null;
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    final String POST_PARAMS = json.toString();
                    System.out.println(json);
                    URL obj = new URL(mainDomain+apiURL);
                    HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
                    postConnection.setRequestMethod("POST");
                    postConnection.setRequestProperty("Content-Type", "application/json");
                    postConnection.setDoOutput(true);
                    postConnection.getOutputStream();
                    try (OutputStream os = postConnection.getOutputStream()) {
                        os.write(POST_PARAMS.getBytes());
                        os.flush();
                        int responseCode = postConnection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            StringBuffer response;
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getInputStream()))) {
                                String inputLine;
                                response = new StringBuffer();
                                while ((inputLine = in.readLine()) != null) {
                                    response.append(inputLine);
                                }
                            }

                            if (response.toString().isEmpty() || response.toString() == null) {
                                JSONObject returnPOST = new JSONObject();
                                returnPOST.put("name", "Http OK but Empty");
                                returnPOST.put("description", responseCode);
                                //return returnPOST;
                                returnPOSTreturn[0] = returnPOST;
                            } else {
                                JSONParser parser = new JSONParser();
                                JSONObject returnPOST = new JSONObject();

                                String temp = response.toString();
                                if (temp.charAt(0) != '[') {
                                    returnPOST = (JSONObject) parser.parse(response.toString());
                                } else {
                                    JSONArray tempOb = (JSONArray) parser.parse(response.toString());
                                    returnPOST = (JSONObject) tempOb.get(0);
                                }
                                //return returnPOST;
                                returnPOSTreturn[0] = returnPOST;
                            }
                        } else {
                            JSONObject returnPOST = new JSONObject();
                            returnPOST.put("name", "Http ERROR");
                            returnPOST.put("description", responseCode);
                            //return returnPOST;
                            returnPOSTreturn[0] = returnPOST;
                        }
                        System.out.print("test API metho: "+returnPOSTreturn[0]);
                    }
                } catch (Exception e) {
                    Log.e("exeption3",e.getMessage());
                    e.printStackTrace();
                    returnPOSTreturn[0] = null;
                }finally {
                    isFinish[0] = true;
                    isStop[0] = false;
                    count.start();
                }
            }
        });
        thread.start();
        return returnPOSTreturn[0];
    }

    public void addItems(String text, boolean isUser){
        //massageList.add(new ListItem(text,isUser));
        adapter.notifyDataSetChanged();
        listView.setSelection(massageList.size()-1);
    }

    public void Speecher(final String text){
        final HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
        speecher = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = speecher.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Toast.makeText(FullscreenActivity.this, "this Language is not support!", Toast.LENGTH_SHORT).show();
                    }else{
                        speecher.setPitch(0.2f);
                        speecher.setSpeechRate(0.8f);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                            speecher.speak(text,TextToSpeech.QUEUE_FLUSH,null,"SOME MESSAGE2");
                        }else{
                            speecher.speak(text,TextToSpeech.QUEUE_FLUSH,myHashAlarm);
                        }
                    }
                }
            }
        });
        speecher.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
            }

            @Override
            public void onDone(String utteranceId) {
                Log.e("Finished",utteranceId);
                if (!isStop[0]){
                    count.start();
                }
            }

            @Override
            public void onError(String utteranceId) {

            }
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getApplicationContext().getPackageName()));
                startActivity(intent);
                finish();
                Toast.makeText(this, "Enable Microphone Permission..!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (speecher != null){
            speecher.stop();
            speecher.shutdown();
        }
        super.onDestroy();
    }
}
