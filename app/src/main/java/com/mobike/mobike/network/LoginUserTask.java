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
 * Created by Andrea-PC on 09/03/2015.
 */
public class LoginUserTask extends AsyncTask<String, Void, String> {
    private String name, email;
    private Context context;
    public static final String loginUserURL = "http://mobike.ddns.net/SRV/users/auth";
    private final static String TAG = "LoginUserTask";

    public LoginUserTask(Context context, String name, String email) {
        this.context = context;
        this.name = name;
        this.email = email;
    }

    @Override
    protected String doInBackground(String... url) {
        try {
            return getUser();
        } catch (IOException e) {
            Log.v(TAG, "exception  message: " + e.getMessage() + " exception class: " + e.getClass());
            return "Unable to complete login. URL may be invalid.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.v(TAG, result);
    }

    private String getUser() throws IOException {
        HttpURLConnection urlConnection = null;
        String result = "";
        try {
            Crypter crypter = new Crypter();
            URL u = new URL(loginUserURL + "?email=" + crypter.encrypt(email));
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);

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
                Log.v(TAG, "userID: " + userID);
                SharedPreferences sharedPref = context.getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(LoginActivity.ID, Integer.parseInt(userID));
                editor.putString(LoginActivity.NICKNAME, nickname);
                editor.apply();
                Intent intent = new Intent(context, MainActivity.class);
                ((Activity) context).startActivityForResult(intent, LoginActivity.MAPS_REQUEST);
                Toast.makeText(context, "Welcome  back" + name.substring(0,1).toUpperCase() + name.substring(1) + "!", Toast.LENGTH_SHORT).show();
                return "";
            }
            else {
                // l'utente non è registrato, fa partire la registrazione
                Log.v(TAG, " httpResult = " + httpResult);
                Intent intent = new Intent(context, NicknameActivity.class);
                // uso startActivityForResult così se NicknameActivity termina esce dall'applicazione
                // può terminare per due motivi, o termina la main activity o l'utente non inserisce il nickname
                ((Activity) context).startActivityForResult(intent, LoginActivity.REGISTRATION_REQUEST);
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