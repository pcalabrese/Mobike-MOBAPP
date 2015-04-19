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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrea-PC on 19/04/2015.
 */
public class POICreationTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "POICreationTask";
    private static final String postNewReviewURL = "http://mobike.ddns.net/SRV/reviews/create";
    private Context context;
    private double latitude, longitude;
    private String title;
    private int category;

    public POICreationTask(Context context, double latitude, double longitude, String title, int category) {
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.category = category;
    }

    @Override
    protected String doInBackground(String... context) {
        try {
            return postPOI();
        } catch (IOException e) {
            return "Unable to upload the route. URL may be invalid.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    private String postPOI() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            Log.v(TAG, "POICreationTask");
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
            JSONObject jsonObject = new JSONObject(), poi = new JSONObject(), user = new JSONObject();
            Crypter crypter = new Crypter();

            try{
                user.put("id", userID);
                user.put("nickname", nickname);
                poi.put("latitude", latitude);
                poi.put("longitude", longitude);
                poi.put("title", title);
                poi.put("category", category);
                jsonObject.put("user", crypter.encrypt(user.toString()));
                jsonObject.put("poi", crypter.encrypt(poi.toString()));
            }
            catch(JSONException e){/*not implemented yet*/ }
            Log.v(TAG, "user: " + user.toString() + "\npoi: " + poi.toString() + "\njson sent: " + jsonObject.toString().replace("\\/", "/").replace("\\\"", "\""));
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(jsonObject.toString().replace("\\/", "/").replace("\\\"", "\""));
            out.close();
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                /*BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String response = br.readLine();
                br.close();*/
                Log.v(TAG, "POI caricato correttamente");
                return "POI created successfully";
            }
            else {
                // scrive un messaggio di errore con codice httpResult
                Log.v(TAG, " httpResult = " + httpResult);
                return "Error code in POI creation: " + httpResult;
            }
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }
}