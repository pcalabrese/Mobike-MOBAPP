package com.mobiketeam.mobike.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobiketeam.mobike.GPSDatabase;
import com.mobiketeam.mobike.LoginActivity;
import com.mobiketeam.mobike.R;
import com.mobiketeam.mobike.utils.Crypter;
import com.mobiketeam.mobike.utils.POI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrea-PC on 24/04/2015.
 */

/**
 * This class performs a HTTP POST to create a list of POIs associated with an existing route
 */
public class POIListCreationTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "POIListCreationTask";
    private static final String postPOIListURL = "http://mobike.ddns.net/SRV/pois/createlist";
    private Context context;
    private double latitude, longitude;
    private String title;
    private int category;
    private int routeID;

    /**
     * Creates a new POIListCreationTask
     * @param context Context
     * @param routeID Route's id
     */
    public POIListCreationTask(Context context, int routeID) {
        this.context = context;
        this.routeID = routeID;
    }

    /**
     * Standard method of Async Task, calls postPOIs() method
     * @param context
     * @return String with a message for the user
     */
    @Override
    protected String doInBackground(String... context) {
        try {
            return postPOIs();
        } catch (IOException e) {
            return "Unable to upload the POIs associated with the route. URL may be invalid.";
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
    private String postPOIs() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            Log.v(TAG, "POIListCreationTask");
            URL u = new URL(postPOIListURL);
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

            JSONObject jsonObject = new JSONObject(), poi, user = new JSONObject(), owner = new JSONObject(), poiInDB;
            JSONArray resultArray = new JSONArray();
            Crypter crypter = new Crypter();

            GPSDatabase db = new GPSDatabase(context);
            JSONArray arrayInDB = db.getPOITableInJSON();
            db.close();

            if (arrayInDB == null || arrayInDB.length() == 0)
                return context.getResources().getString(R.string.no_pois_associated_with_route);

            try{
                user.put("id", userID);
                user.put("nickname", nickname);
                owner.put("id", userID);
                owner.put("nickname", nickname);
                for (int i = 0; i < arrayInDB.length(); i++) {
                    poi = new JSONObject();
                    poiInDB = arrayInDB.getJSONObject(i);
                    poi.put("lat", poiInDB.getDouble("latitude"));
                    poi.put("lon", poiInDB.getDouble("longitude"));
                    poi.put("title", poiInDB.getString("title"));
                    poi.put("type", POI.intToStringTypeEnglish(poiInDB.getInt("category")));
                    poi.put("owner", owner);
                    JSONArray route = new JSONArray();
                    route.put(new JSONObject().put("id", routeID));
                    poi.put("routesAssociated", route);
                    resultArray.put(poi);
                }
                jsonObject.put("user", crypter.encrypt(user.toString()));
                jsonObject.put("pois", crypter.encrypt(resultArray.toString()));
            }
            catch(JSONException e){/*not implemented yet*/ }

            Log.v(TAG, "user: " + user.toString() + "\npois: " + resultArray.toString() + "\njson sent: " + jsonObject.toString().replace("\\/", "/").replace("\\\"", "\""));
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(jsonObject.toString().replace("\\/", "/").replace("\\\"", "\""));
            out.close();

            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                Log.v(TAG, "POI list caricata correttamente");
                return context.getResources().getString(R.string.pois_uploaded_successfully);
            }
            else {
                // scrive un messaggio di errore con codice httpResult
                Log.v(TAG, " httpResult = " + httpResult);
                return "Error code in POIs uplaod: " + httpResult;
            }
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }
}