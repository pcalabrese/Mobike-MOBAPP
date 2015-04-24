package com.mobike.mobike.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobike.mobike.GPSDatabase;
import com.mobike.mobike.LoginActivity;
import com.mobike.mobike.utils.Crypter;
import com.mobike.mobike.utils.POI;

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
public class POIListCreationTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "POIListCreationTask";
    private static final String postNewReviewURL = "http://mobike.ddns.net/SRV/pois/create";
    private Context context;
    private double latitude, longitude;
    private String title;
    private int category;
    private int routeID;

    public POIListCreationTask(Context context, int routeID) {
        this.context = context;
        this.routeID = routeID;
    }

    @Override
    protected String doInBackground(String... context) {
        try {
            return postPOIs();
        } catch (IOException e) {
            return "Unable to upload the route. URL may be invalid.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    private String postPOIs() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            Log.v(TAG, "POIListCreationTask");
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

            JSONObject jsonObject = new JSONObject(), poi, user = new JSONObject(), owner = new JSONObject(), poiInDB;
            JSONArray resultArray = new JSONArray();
            Crypter crypter = new Crypter();

            GPSDatabase db = new GPSDatabase(context);
            JSONArray arrayInDB = db.getPOITableInJSON();
            db.close();

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
                    poi.put("type", POI.intToStringType(poiInDB.getInt("category")));
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
                /*BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String response = br.readLine();
                br.close();*/
                Log.v(TAG, "POI caricato correttamente");
                return "POIs uploaded successfully";
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