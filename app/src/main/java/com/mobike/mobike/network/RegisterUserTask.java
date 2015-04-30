package com.mobike.mobike.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobike.mobike.LoginActivity;
import com.mobike.mobike.MainActivity;
import com.mobike.mobike.NicknameActivity;
import com.mobike.mobike.R;
import com.mobike.mobike.utils.Crypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrea-PC on 08/03/2015.
 */

/**
 * This class performs a HTTP POST to create a new account for the user
 */
public class RegisterUserTask extends AsyncTask<String, Void, String> {
    private String name, surname, nickname, email, imageURL, bike;
    private Context context;
    private static final String TAG = "RegisterUserTask";
    public static final String registerUserURL = "http://mobike.ddns.net/SRV/users/create";

    /**
     * Creates a new RegisterUserTask
     * @param context
     * @param name
     * @param surname
     * @param nickname
     * @param email
     * @param imageURL
     * @param bike
     */
    public RegisterUserTask(Context context, String name, String surname, String nickname, String email, String imageURL, String bike) {
        this.context = context;
        this.name = name;
        this.surname = surname;
        this.nickname = nickname;
        this.email = email;
        this.imageURL= imageURL;
        this.bike = bike;
    }

    /**
     * Standard method of Async Task, calls postUser() method
     * @param context
     * @return String with a message for the user
     */
    @Override
    protected String doInBackground(String... context) {
        try {
            return postUser();
        } catch (IOException e) {
            return "Unable to complete registration. URL may be invalid.";
        }
    }

    /**
     * Standard method of Async Task, makes a Toast with the result String
     * @param result String with a message for the user
     */
    @Override
    protected void onPostExecute(String result) {

        if (result.equals("1")) {
            result = "This nickname already exists, please choose another one.";
            ((NicknameActivity) context).nicknameAlreadyExists();
        }
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    /**
     * Performs the HTTP POST
     * @return String with a message for the user
     * @throws IOException
     */
    private String postUser() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            Crypter crypter = new Crypter();
            URL u = new URL(registerUserURL);
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.connect();
            JSONObject jsonObject = new JSONObject(), jsonObject1 = new JSONObject();
            try{
                jsonObject.put("name", name);
                jsonObject.put("surname", surname);
                jsonObject.put("email", email);
                jsonObject.put("imgurl", imageURL);
                jsonObject.put("nickname", nickname);
                jsonObject.put("bikemodel", bike);
                jsonObject1.put("user", crypter.encrypt(jsonObject.toString()));
            }
            catch(JSONException e){/*not implemented yet*/ }
            Log.v(TAG, "json: " + jsonObject.toString());
            Log.v(TAG, "json criptato: " + jsonObject1.toString());
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(jsonObject1.toString());
            out.close();
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                String response = convertInputStreamToString(urlConnection.getInputStream());
                String userID = "none", nickname = "none";
                JSONObject json;
                try {
                    json = new JSONObject(crypter.decrypt((new JSONObject(response)).getString("user")));
                    userID = json.getInt("id") + "";
                    nickname = json.getString("nickname");
                } catch (JSONException e) {
                    Log.v(TAG, "errore nel parsing del json di risposta");
                }
                Log.v(TAG, "userID: " + userID + ", nickname:" + nickname);
                SharedPreferences sharedPref = context.getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(LoginActivity.ID, Integer.parseInt(userID));
                editor.putString(LoginActivity.NICKNAME, nickname);
                editor.apply();
                Intent intent = new Intent(context, MainActivity.class);
                ((Activity) context).startActivityForResult(intent, LoginActivity.MAPS_REQUEST);
                return context.getResources().getString(R.string.registration_welcome_message) + " " + name.substring(0,1).toUpperCase() + name.substring(1) + "!";
            } else if (httpResult == HttpURLConnection.HTTP_CONFLICT) {
                return "1";
            }
            else {
                // scrive un messaggio di errore con codice httpResult
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String r;
                r = br.readLine();
                Log.v(TAG, "error: " + r);
                br.close();
                Log.v(TAG, " httpResult = " + httpResult);
                return "Error code: " + httpResult;
            }
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}