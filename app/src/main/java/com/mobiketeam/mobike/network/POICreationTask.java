package com.mobiketeam.mobike.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobiketeam.mobike.LoginActivity;
import com.mobiketeam.mobike.R;
import com.mobiketeam.mobike.utils.Crypter;
import com.mobiketeam.mobike.utils.POI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrea-PC on 19/04/2015.
 */

/**
 * This class performs a HTTP POST to create a new POI
 */
public class POICreationTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "POICreationTask";
    private static final String postNewPOIURL = "http://mobike.ddns.net/SRV/pois/create";
    private Context context;
    private double latitude, longitude;
    private String title;
    private int category;

    /**
     * Creates a new POICreationTask
     * @param context
     * @param latitude
     * @param longitude
     * @param title
     * @param category
     */
    public POICreationTask(Context context, double latitude, double longitude, String title, int category) {
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.category = category;
    }

    /**
     * Standard method of Async Task, calls postPOI() method
     * @param context
     * @return String with a message for the user
     */
    @Override
    protected String doInBackground(String... context) {
        try {
            return postPOI();
        } catch (IOException e) {
            return "Unable to upload the POI. URL may be invalid.";
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
    private String postPOI() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            Log.v(TAG, "POICreationTask");
            URL u = new URL(postNewPOIURL);
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
            JSONObject jsonObject = new JSONObject(), poi = new JSONObject(), user = new JSONObject(), owner = new JSONObject();
            Crypter crypter = new Crypter();

            try{
                user.put("id", userID);
                user.put("nickname", nickname);
                owner.put("id", userID);
                owner.put("nickname", nickname);
                poi.put("lat", latitude);
                poi.put("lon", longitude);
                poi.put("title", title);
                poi.put("type", POI.intToStringType(category));
                poi.put("owner", owner);
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
                return context.getResources().getString(R.string.poi_created_successfully);
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