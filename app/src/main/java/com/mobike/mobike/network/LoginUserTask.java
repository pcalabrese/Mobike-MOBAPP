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
 * Created by Andrea-PC on 09/03/2015.
 */
public class LoginUserTask extends AsyncTask<String, Void, String> {
    private String name, surname, email, imageURL;
    private Context context;
    public static final String registerUserURL = "qualcosa";
    private final static String TAG = "LoginUserTask";

    public LoginUserTask(Context context, String name, String surname, String email, String imageURL) {
        this.context = context;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.imageURL = imageURL;
    }

    @Override
    protected String doInBackground(String... url) {
        try {
            return postUser(url[0]);
        } catch (IOException e) {
            Log.v(TAG, "exception  message: " + e.getMessage() + " exception class: " + e.getClass());
            return "Unable to complete login. URL may be invalid.";
        }
    }

    @Override
    protected void onPostExecute(String result) {

    }

    private String postUser(String url) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
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
            JSONObject jsonObject = new JSONObject();
            try{
                jsonObject.put("name", name);
                jsonObject.put("surname", surname);
                jsonObject.put("email", email);
                jsonObject.put("imageURL", imageURL);
            }
            catch(JSONException e){/*not implemented yet*/ }
            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(jsonObject.toString());
            out.close();
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                String response = convertInputStreamToString(urlConnection.getInputStream());
                String userID = "none", nickname = "none";
                JSONObject json;
                try {
                    json = new JSONObject(response);
                    userID = json.getInt("userId") + "";
                    nickname = json.getString("nickname");
                } catch (JSONException e) {}
                Log.v(TAG, "userID: " + userID);
                SharedPreferences sharedPref = context.getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(LoginActivity.ID, Integer.parseInt(userID));
                editor.putString(LoginActivity.NICKNAME, nickname);
                editor.apply();
                Intent intent = new Intent(context, MainActivity.class);
                ((Activity) context).startActivityForResult(intent, LoginActivity.MAPS_REQUEST);
                Toast.makeText(context, "Welcome " + name + "!", Toast.LENGTH_SHORT).show();
                return "";
            }
            else {
                // l'utente non è registrato, fa partire la registrazione
                Log.v(TAG, " httpResult = " + httpResult);
                Intent intent = new Intent(context, NicknameActivity.class);
                context.startActivity(intent);
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