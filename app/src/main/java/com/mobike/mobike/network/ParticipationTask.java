package com.mobike.mobike.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobike.mobike.LoginActivity;
import com.mobike.mobike.R;
import com.mobike.mobike.utils.Crypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Andrea-PC on 22/03/2015.
 */

/**
 * This class performs a HTTP POST to set the participation of the user to the event
 */
public class ParticipationTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "ParticipationTask";
    private static final String participationURL = "http://mobike.ddns.net/SRV/events/participation?op=";
    private Context context;
    private String eventID;

    /**
     * Creates a new ParticipationTask
     * @param context
     * @param eventID
     */
    public ParticipationTask(Context context, String eventID) {
        this.context = context;
        this.eventID = eventID;
    }

    /**
     * Standard method of Async Task, calls postParticipation() method
     * @param command
     * @return String with a message for the user
     */
    @Override
    protected String doInBackground(String... command) {
        try {
            return postParticipation(command[0]);
        } catch (IOException e) {
            return "Unable to send participation. URL may be invalid.";
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
     * @param command
     * @return String with a message for the user
     * @throws IOException
     */
    private String postParticipation(String command) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL u = new URL(participationURL + URLEncoder.encode(command, "utf-8"));
            Log.v(TAG, "url: " + participationURL + URLEncoder.encode(command, "utf-8"));
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
            JSONObject jsonObject = new JSONObject(), event = new JSONObject(), user = new JSONObject();
            Crypter crypter = new Crypter();

            try{
                user.put("id", userID);
                user.put("nickname", nickname);
                event.put("id", eventID);
                jsonObject.put("user", crypter.encrypt(user.toString()));
                jsonObject.put("event", crypter.encrypt(event.toString()));
            }
            catch(JSONException e){/*not implemented yet*/ }
            Log.v(TAG, "user: " + user.toString() + "\nevent: " + event.toString() + "\njson sent: " + jsonObject.toString().replace("\\/", "/").replace("\\\"", "\""));
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(jsonObject.toString().replace("\\/", "/").replace("\\\"", "\""));
            out.close();
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                /*BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String response = br.readLine();
                br.close();*/
                Log.v(TAG, "Partecipazione inviata correttamente");
                return context.getResources().getString(R.string.participation_sent_successfully);
            }
            else {
                // scrive un messaggio di errore con codice httpResult
                Log.v(TAG, " httpResult = " + httpResult);
                return "Error code in participation: " + httpResult;
            }
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }
}