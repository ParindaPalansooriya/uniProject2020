package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class Main2Activity extends AppCompatActivity {

    TextView text2;
    ImageView image;
    ListView listView;
    ArrayList<ListItem> massageList = new ArrayList<>();
    MyAdapter adapter;
    String actionType;
    SpeechRecognizer speechRecognizer;
    boolean needToListen = true;
    boolean textClicked = false;
    boolean isSpeeking = false;
    boolean isListCick= false;
    CountDownTimer count;
    Intent speechRecognizerIntent;
    TextToSpeech speecher;

    static String mainDomain = "http://192.168.8.100:8080";/** http://device IP:host port number/API endpoint **/
    Thread thread;
    JSONObject returnPOSTreturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        text2 = findViewById(R.id.textView3);
        image = findViewById(R.id.imageView);
        listView = findViewById(R.id.listMassage);
        adapter = new MyAdapter(this,massageList);
        listView.setAdapter(adapter);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.startListening(speechRecognizerIntent);

        count = new CountDownTimer(300000,100){
            @Override
            public void onTick(long millisUntilFinished) {
                if (needToListen && !textClicked) {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }

            @Override
            public void onFinish() {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
                image.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_audio_busy));
            }
        };
        count.start();

        startSpeechToText();
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.startListening(speechRecognizerIntent);
                needToListen = true;
                textClicked = false;
                count.cancel();
                count.start();
            }
        });
        text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                needToListen = false;
                textClicked = true;
                speechRecognizer.cancel();
                image.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_audio_busy));
                dialogClick();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (!massageList.get(position).getLink().equals("NO")){
                    Log.e("jhgsgjd","shdhsdkshjkd");
                    view.findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            needToListen = false;
                            textClicked = true;
                            isListCick = true;
                            speechRecognizer.cancel();
                            Intent intent = new Intent(Main2Activity.this,WebLoading.class);
                            intent.putExtra("link",massageList.get(position).getLink());
                            startActivity(intent);
                        }
                    });
                }
            }
        });


    }


    private void startSpeechToText() {

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                needToListen = false;
                image.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_audio_online));
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                image.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_audio_busy));
            }

            @Override
            public void onError(int error) {
                needToListen = true;
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    needToListen = false;
                    text2.setText(matches.get(0));
                    addItems(matches.get(0),false,"text");
                    if (matches.get(0).equals("yes")) {
                        if (actionType.equals("reminder")) {
                            addItems("Okay. We have set the remainder",true,"text");
                            Speecher("Okay. We have set the remainder for this information");
                        } else if (actionType.equals("close")) {
                            addItems("Okay. Good bye",true,"text");
                            Speecher("Okay. Good bye");
                            finish();
                        }else{
                            addItems("Sorry. Try again",true,"text");
                            Speecher("Sorry. Try again");
                        }
                    }else if(matches.get(0).equals("no")){
                        actionType="no action";
                    }else if(matches.get(0).equals("stop")){
                        addItems("Okay. i am gig to close this application. do you conform it?",true,"text");
                        Speecher("Okay. i am gig to close this application. do you conform it?");
                        actionType="close";
                    }else {
                        JSONObject json = new JSONObject();
                        json.put("name", matches.get(0));
                        json.put("password", matches.get(0));
                        json.put("email", matches.get(0));
                        POSTRequest("/user/uni", json);
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
    }

    public void addItems(final String text, final boolean isUser, final String type){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                massageList.add(new ListItem("<h4>"+text+"</h4>",type,isUser));
                adapter.notifyDataSetChanged();
                listView.setSelection(massageList.size()-1);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void POSTRequest(final String apiURL, final JSONObject json){
        returnPOSTreturn = null;
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
                                returnPOSTreturn = returnPOST;
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
                                returnPOSTreturn = returnPOST;
                            }
                        } else {
                            JSONObject returnPOST = new JSONObject();
                            returnPOST.put("name", "Http ERROR"+responseCode);
                            returnPOST.put("description", responseCode);
                            returnPOSTreturn = returnPOST;
                        }
                        System.out.print("test API metho: "+returnPOSTreturn);
                    }
                } catch (Exception e) {
                    JSONObject returnPOST = new JSONObject();
                    returnPOST.put("name", "Exeption"+e.getMessage());
                    returnPOST.put("description", e);
                    returnPOSTreturn = returnPOST;
                }finally {
                    actionType = (String)returnPOSTreturn.get("name");
                    addItems((String)returnPOSTreturn.get("name") ,true,actionType);
                    if (actionType.equals("link")) {
                        Speecher("I am received a web link. Do you want to load it in web browser");
                    }else if (actionType.equals("reminder")){
                        Speecher("I am received a massage to set a remainder for you. Do you accept it");
                    }else{
                        Speecher((String)returnPOSTreturn.get("name"));
                    }
                }
            }
        });
        thread.start();
    }

    private void addReminder(int statrYear, int startMonth, int startDay, int startHour, int startMinut, int endYear, int endMonth, int endDay, int endHour, int endMinuts){
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(statrYear, startMonth, startDay, startHour, startMinut);
        long startMillis = beginTime.getTimeInMillis();

        Calendar endTime = Calendar.getInstance();
        endTime.set(endYear, endMonth, endDay, endHour, endMinuts);
        long endMillis = endTime.getTimeInMillis();

        String eventUriString = "content://com.android.calendar/events";
        ContentValues eventValues = new ContentValues();

        eventValues.put(CalendarContract.Events.CALENDAR_ID, 1);
        eventValues.put(CalendarContract.Events.TITLE, "OCS");
        eventValues.put(CalendarContract.Events.DESCRIPTION, "Clinic App");
        eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, "Nasik");
        eventValues.put(CalendarContract.Events.DTSTART, startMillis);
        eventValues.put(CalendarContract.Events.DTEND, endMillis);

        //eventValues.put(Events.RRULE, "FREQ=DAILY;COUNT=2;UNTIL="+endMillis);
        eventValues.put("eventStatus", 1);
        eventValues.put("visibility", 3);
        eventValues.put("transparency", 0);
        eventValues.put(CalendarContract.Events.HAS_ALARM, 1);

        Uri eventUri = getContentResolver().insert(Uri.parse(eventUriString), eventValues);
        long eventID = Long.parseLong(eventUri.getLastPathSegment());

        /***************** Event: Reminder(with alert) Adding reminder to event *******************/

        String reminderUriString = "content://com.android.calendar/reminders";

        ContentValues reminderValues = new ContentValues();

        reminderValues.put("event_id", eventID);
        reminderValues.put("minutes", 1);
        reminderValues.put("method", 1);

        Uri reminderUri = getContentResolver().insert(Uri.parse(reminderUriString), reminderValues);
    }

    private void addInstance(int statrYear, int startMonth, int startDay, int startHour, int startMinut, int endYear, int endMonth, int endDay, int endHour, int endMinuts) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, 42);
        }

        ContentResolver cr = getContentResolver();
        ContentValues contentValues = new ContentValues();

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(statrYear, startMonth, startDay, startHour, startMinut);

        Calendar endTime = Calendar.getInstance();
        endTime.set(endYear, endMonth, endDay, endHour, endMinuts);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Instances.EVENT_ID, "2");
        values.put(CalendarContract.Instances.BEGIN, beginTime.getTimeInMillis());
        values.put(CalendarContract.Instances.END, endTime.getTimeInMillis());


        cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
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

    private void dialogClick(){

        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.sendcustommasage,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        Button send = view.findViewById(R.id.button2);
        final EditText text = view.findViewById(R.id.editText);

        builder.setCancelable(true);
        final AlertDialog alert = builder.create();
        alert.getWindow().setGravity(Gravity.CENTER);
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.show();

        send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                text2.setText(text.getText().toString());
                addItems(text.getText().toString(),false,"text");
                JSONObject json = new JSONObject();
                json.put("name",text.getText().toString());
                json.put("password",text.getText().toString());
                json.put("email",text.getText().toString());
                POSTRequest("/user/uni", json);
                alert.cancel();
            }
        });

    }

    private void Speecher(final String text){
        final HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "SOME MESSAGE");
        speecher = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = speecher.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Toast.makeText(Main2Activity.this, "this Language is not support!", Toast.LENGTH_SHORT).show();
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
                isSpeeking = true;
            }

            @Override
            public void onDone(String utteranceId) {

                if (!isListCick) {
                        needToListen = true;
                        textClicked = false;
                    }
                    isSpeeking = false;
            }

            @Override
            public void onError(String utteranceId) {
                if (!isListCick) {
                    needToListen = true;
                    textClicked = false;
                }
                isSpeeking = false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isSpeeking) {
            needToListen = true;
            textClicked = false;
        }
        isListCick = false;
    }
}
