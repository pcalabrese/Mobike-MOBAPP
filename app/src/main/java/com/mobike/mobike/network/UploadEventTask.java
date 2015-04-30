package com.mobike.mobike.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobike.mobike.LoginActivity;
import com.mobike.mobike.R;
import com.mobike.mobike.utils.Event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Andrea-PC on 22/02/2015.
 */

/**
 * This class performs a HTTP POST to create a new Event
 */
public class UploadEventTask extends AsyncTask<String, Void, String> {
    private Event event;
    private Context context;
    private HashMap<String, Integer> usersMap;

    private final static String UploadEventURL = "http://mobike.ddns.net/SRV/events/create";
    private final static String TAG = "UploadEventTask";

    /**
     * Creates a new UploadEventTask
     * @param context
     * @param event
     * @param usersMap
     */
    public UploadEventTask(Context context, Event event, HashMap<String, Integer> usersMap) {
        this.event = event;
        this.context = context;
        this.usersMap = usersMap;
    }

    /**
     * Standard method of Async Task, calls uploadEvent() method
     * @param strings
     * @return String with a message for the user
     */
    @Override
    protected String doInBackground(String... strings) {
        try {
            return uploadEvent();
        } catch (IOException e) {
            Log.v(TAG, "exception  message: " + e.getMessage() + " exception class: " + e.getClass());
            return "Unable to upload the event. URL may be invalid.";
        }
    }

    /**
     * Standard method of Async Task, makes a Toast with the result String
     * @param result String with a message for the user
     */
    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    /**
     * Performs the HTTP POST
     * @return String with a message for the user
     * @throws IOException
     */
    private String uploadEvent() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL u = new URL(UploadEventURL);
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "text/plain");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.connect();

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            SharedPreferences sharedPref = context.getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
            int userID = sharedPref.getInt(LoginActivity.ID, 0);
            out.write(event.exportInJSON(userID, usersMap).toString().replace("\\/", "/").replace("\\\"", "\""));
            out.close();
            Log.v(TAG, "json sent: " + event.exportInJSON(userID, usersMap).toString().replace("\\/", "/").replace("\\\"", "\""));

            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                String eventID;
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                eventID = br.readLine();
                Log.v(TAG, "Event created: " + eventID);
                br.close();
                return context.getResources().getString(R.string.event_created);
            }
            else {
                // scrive un messaggio di errore con codice httpResult
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String r;
                r = br.readLine();
                Log.v(TAG, r);
                br.close();
                Log.v(TAG, " httpResult = " + httpResult);
                return "Error code: " + httpResult;
            }
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }
}