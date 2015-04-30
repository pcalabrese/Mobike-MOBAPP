package com.mobike.mobike.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobike.mobike.GPSDatabase;
import com.mobike.mobike.R;
import com.mobike.mobike.ReviewCreationActivity;
import com.mobike.mobike.SearchFragment;
import com.mobike.mobike.ShareActivity;
import com.mobike.mobike.SummaryActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrea-PC on 21/02/2015.
 */

/**
 * This class performs a HTTP POST to create a new route
 */
public class UploadRouteTask extends AsyncTask<String, Void, String> {
    private Context context;
    private String email, name, description, difficulty, bends, type, startLocation, endLocation;
    private GPSDatabase db;

    private final static String UploadRouteURL = "http://mobike.ddns.net/SRV/routes/create";
    private final static String TAG = "UploadRouteTask";

    /**
     * Creates a new UploadRouteTask
     * @param context
     * @param email
     * @param name
     * @param description
     * @param difficulty
     * @param bends
     * @param type
     * @param startLocation
     * @param endLocation
     */
    public UploadRouteTask(Context context, String email, String name, String description, String difficulty, String bends, String type, String startLocation, String endLocation) {
        this.context = context;
        this.email = email;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.bends = bends;
        this.type = type;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    /**
     * Standard method of Async Task, calls uploadRoute() method
     * @param strings
     * @return String with a message for the user
     */
    @Override
    protected String doInBackground(String... strings) {
        try {
            return uploadRoute();
        } catch (IOException e) {
            Log.v(TAG, "exception  message: " + e.getMessage() + " exception class: " + e.getClass());
            return "Unable to upload the route. URL may be invalid.";
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
    private String uploadRoute() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL u = new URL(UploadRouteURL);
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
            GPSDatabase db = new GPSDatabase(context);
            out.write(db.exportRouteInJson(email, name, description, difficulty, bends, type, startLocation, endLocation).toString().replace("\\/", "/").replace("\\\"", "\""));
            out.close();
            Log.v(TAG, "json sent: "+ db.exportRouteInJson(email, name, description, difficulty, bends, type, startLocation, endLocation).toString().replace("\\/", "/").replace("\\\"", "\""));
            db.close();
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                String routeID;
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                routeID = br.readLine();
                Log.v(TAG, routeID);
                br.close();
                ((SummaryActivity) context).setRoute(routeID);

                new POIListCreationTask(context, Integer.parseInt(routeID)).execute();

                Intent intent = new Intent(context, ReviewCreationActivity.class);
                intent.putExtra(SearchFragment.ROUTE_ID, routeID);
                intent.putExtra(SearchFragment.REQUEST_CODE, SummaryActivity.REVIEW_REQUEST);
                ((Activity) context).startActivityForResult(intent, SummaryActivity.REVIEW_REQUEST);
                return context.getResources().getString(R.string.upload_completed);
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