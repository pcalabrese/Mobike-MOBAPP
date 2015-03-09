package com.mobike.mobike.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobike.mobike.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrea-PC on 04/03/2015.
 */
public class UploadNewReviewTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "UploadNewReviewTask";
    private static final String postNewReviewURL = "qualcosa";
    private Context context;
    private float rate;
    private String comment, routeID;

    public UploadNewReviewTask(Context context, String routeID, String comment, float rate) {
        this.context = context;
        this.rate = rate;
        this.comment = comment;
        this.routeID = routeID;
    }

    @Override
    protected String doInBackground(String... context) {
        try {
            return postReview();
        } catch (IOException e) {
            return "Unable to upload the route. URL may be invalid.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    private String postReview() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL u = new URL(postNewReviewURL);
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "text/plain");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.connect();
            SharedPreferences sharedPref = context.getSharedPreferences(LoginActivity.ID, Context.MODE_PRIVATE);
            int userID = sharedPref.getInt(LoginActivity.ID, 0);
            JSONObject jsonObject = new JSONObject();
            try{
                jsonObject.put("userID", userID);
                jsonObject.put("routeID", routeID);
                jsonObject.put("rate", rate);
                jsonObject.put("comment", comment);
            }
            catch(JSONException e){/*not implemented yet*/ }
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(jsonObject.toString());
            out.close();
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                /*BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String response = br.readLine();
                br.close();*/
                return "Review uploaded successfully";
            }
            else {
                // scrive un messaggio di errore con codice httpResult
                Log.v(TAG, " httpResult = " + httpResult);
                return "Error code in review upload: " + httpResult;
            }
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }
}