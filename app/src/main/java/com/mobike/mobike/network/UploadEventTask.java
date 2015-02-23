package com.mobike.mobike.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobike.mobike.utils.Event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrea-PC on 22/02/2015.
 */
public class UploadEventTask extends AsyncTask<String, Void, String> {
    private Event event;
    private Context context;

    private final static String UploadEventURL = "qualcosa";
    private final static String TAG = "UploadEventTask";

    public UploadEventTask(Context context, Event event) {
        this.event = event;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            return uploadEvent();
        } catch (IOException e) {
            Log.v(TAG, "exception  message: " + e.getMessage() + " exception class: " + e.getClass());
            return "Unable to upload the route. URL may be invalid.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

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
            out.write(event.exportInJSON().toString());
            out.close();
            Log.v(TAG, "json sent: " + event.exportInJSON().toString());
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                String routeID;
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                routeID = br.readLine();
                Log.v(TAG, routeID);
                br.close();
                return "Upload completed!";
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