package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebRequest extends AsyncTask<String, Void, JSONObject> {

    static String mainDomain = "https://reqbin.com";/** http://device IP:host port number/API endpoint **/

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public JSONObject POSTRequest(String apiURL, JSONObject json) throws IOException, ParseException, JSONException {

        final String POST_PARAMS = json.toString();
        System.out.println(json);
        URL obj = new URL(mainDomain + apiURL);
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
                    returnPOST.put("name", "Http OK");
                    returnPOST.put("message", responseCode);
                    return returnPOST;
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
                    return returnPOST;
                }
            } else {
                JSONObject returnPOST = new JSONObject();
                returnPOST.put("name", "Http ERROR");
                returnPOST.put("message", responseCode);
                return returnPOST;
            }

        }
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        return null;
    }

    public void doInBackground(String s, JSONObject json) {
    }
}
