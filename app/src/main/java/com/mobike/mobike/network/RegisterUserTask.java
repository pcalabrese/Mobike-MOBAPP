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
public class RegisterUserTask extends AsyncTask<String, Void, String> {
    private String name, surname, nickname, email, imageURL, bike;
    private Context context;
    private static final String TAG = "RegisterUserTask";
    public static final String registerUserURL = "http://mobike.ddns.net/SRV/users/create";

    public RegisterUserTask(Context context, String name, String surname, String nickname, String email, String imageURL, String bike) {
        this.context = context;
        this.name = name;
        this.surname = surname;
        this.nickname = nickname;
        this.email = email;
        this.imageURL= imageURL;
        this.bike = bike;
    }

    @Override
    protected String doInBackground(String... context) {
        try {
            return postUser();
        } catch (IOException e) {
            return "Unable to complete registration. URL may be invalid.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
    }

    private String postUser() throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            Crypter crypter = new Crypter();
            URL u = new URL(registerUserURL);
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "text/plain");
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
                jsonObject.put("imageURL", imageURL);
                jsonObject.put("nickname", nickname);
                jsonObject.put("bike", bike);
                jsonObject1.put("user", crypter.encrypt(jsonObject.toString()));
            }
            catch(JSONException e){/*not implemented yet*/ }
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
                    userID = json.getInt("userId") + "";
                    nickname = json.getString("nickname");
                } catch (JSONException e) {}
                SharedPreferences sharedPref = context.getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(LoginActivity.ID, Integer.parseInt(userID));
                editor.putString(LoginActivity.NICKNAME, nickname);
                editor.apply();
                Intent intent = new Intent(context, MainActivity.class);
                ((Activity) context).startActivityForResult(intent, LoginActivity.MAPS_REQUEST);
                return "Welcome " + name.substring(0,1).toUpperCase() + name.substring(1) + "!";
            }
            else {
                // scrive un messaggio di errore con codice httpResult
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