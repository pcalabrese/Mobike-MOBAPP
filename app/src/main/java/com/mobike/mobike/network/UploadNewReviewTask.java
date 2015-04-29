package com.mobike.mobike.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobike.mobike.LoginActivity;
import com.mobike.mobike.utils.Crypter;

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

/**
 * This class performs a HTTP POST to create a new Review associated with an existing route
 */
public class UploadNewReviewTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "UploadNewReviewTask";
    private static final String postNewReviewURL = "http://mobike.ddns.net/SRV/reviews/create";
    private Context context;
    private float rate;
    private String comment, routeID;

    /**
     * Creates a new UploadNewReviewTask
     * @param context
     * @param routeID
     * @param comment
     * @param rate
     */
    public UploadNewReviewTask(Context context, String routeID, String comment, float rate) {
        this.context = context;
        this.rate = rate;
        this.comment = comment;
        this.routeID = routeID;
    }

    /**
     * Standard method of Async Task, calls postReview() method
     * @param context
     * @return String with a message for the user
     */
    @Override
    protected String doInBackground(String... context) {
        try {
            return postReview();
        } catch (IOException e) {
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
    private String postReview() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            Log.v(TAG, "uploadNewReviewTask");
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
            SharedPreferences sharedPref = context.getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
            int userID = sharedPref.getInt(LoginActivity.ID, 0);
            String nickname = sharedPref.getString(LoginActivity.NICKNAME, "");
            JSONObject jsonObject = new JSONObject(), review = new JSONObject(), user = new JSONObject(), pk = new JSONObject();
            Crypter crypter = new Crypter();

            try{
                user.put("id", userID);
                user.put("nickname", nickname);
                review.put("rate", rate);
                review.put("message", comment);
                pk.put("usersId", userID);
                pk.put("routesId", routeID);
                review.put("reviewPK", pk);
                jsonObject.put("user", crypter.encrypt(user.toString()));
                jsonObject.put("review", crypter.encrypt(review.toString()));
            }
            catch(JSONException e){/*not implemented yet*/ }
            Log.v(TAG, "user: " + user.toString() + "\nreview: " + review.toString() + "\njson sent: " + jsonObject.toString().replace("\\/", "/").replace("\\\"", "\""));
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(jsonObject.toString().replace("\\/", "/").replace("\\\"", "\""));
            out.close();
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                /*BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String response = br.readLine();
                br.close();*/
                Log.v(TAG, "Recensione caricata correttamente");
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