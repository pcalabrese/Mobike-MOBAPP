package com.mobiketeam.mobike.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mobiketeam.mobike.LoginActivity;
import com.mobiketeam.mobike.MainActivity;
import com.mobiketeam.mobike.NicknameActivity;
import com.mobiketeam.mobike.R;
import com.mobiketeam.mobike.utils.Crypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Andrea-PC on 09/03/2015.
 */

/**
 * This class performs a HTTP GET to check whether the user is already registered or not
 */
public class LoginUserTask extends AsyncTask<String, Void, String> {
    private String name, surname, email, imgURL;
    private Context context;
    public static final String loginUserURL = "http://mobike.ddns.net/SRV/users/auth";
    private final static String TAG = "LoginUserTask";

    /**
     * Creates a new LoginUserTask
     * @param context
     * @param name
     * @param surname
     * @param email
     * @param imgURL
     */
    public LoginUserTask(Context context, String name, String surname, String email, String imgURL) {
        this.context = context;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.imgURL = imgURL;
    }

    /**
     * Standard method of Async Task, calls getUser() method
     * @param url
     * @return String with a message for the user
     */
    @Override
    protected String doInBackground(String... url) {
        try {
            return getUser();
        } catch (IOException e) {
            Log.v(TAG, "exception  message: " + e.getMessage() + " exception class: " + e.getClass());
            e.printStackTrace();
            return "Unable to complete login. URL may be invalid.";
            //return "exception  message: " + e.getMessage() + " exception class: " + e.getClass() + ", \nstack trace: " + e.getStackTrace();
        }
    }

    /**
     * Standard method of Async Task, makes a Toast with the result String
     * @param result String with a message for the user
     */
    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        Log.v(TAG, result);
    }

    /**
     * Performs the HTTP GET
     * @return String with a message for the user
     * @throws IOException
     */
    private String getUser() throws IOException {
        HttpURLConnection urlConnection = null;
        String result = "";
        try {
            Crypter crypter = new Crypter();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("surname", surname);
                jsonObject.put("email", email);
                jsonObject.put("imgurl", imgURL);
            } catch (JSONException e) {
            }
            Log.v(TAG, "json: " + jsonObject.toString());

            String token = URLEncoder.encode(crypter.encrypt(jsonObject.toString()), "utf-8");
            URL u = new URL(loginUserURL + "?token=" + token);
            Log.v(TAG, "token = " + token);
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);

            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                String response = convertInputStreamToString(urlConnection.getInputStream());
                String nickname = "none";
                int userID = 0;
                JSONObject json;
                try {
                    json = new JSONObject(crypter.decrypt((new JSONObject(response)).getString("user")));
                    userID = json.getInt("id");
                    nickname = json.getString("nickname");
                } catch (JSONException e) {
                }
                Log.v(TAG, "userID: " + userID + "\nnickname: " + nickname);
                SharedPreferences sharedPref = context.getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(LoginActivity.ID, userID);
                editor.putString(LoginActivity.NICKNAME, nickname);
                editor.apply();
                Intent intent = new Intent(context, MainActivity.class);
                ((Activity) context).startActivityForResult(intent, LoginActivity.MAPS_REQUEST);
                return context.getResources().getString(R.string.login_welcome_back_message) + " " + name.substring(0, 1).toUpperCase() + name.substring(1) + "!";
            } else if (httpResult == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // l'utente non è registrato, fa partire la registrazione
                Log.v(TAG, " httpResult = " + httpResult);
                Intent intent = new Intent(context, NicknameActivity.class);
                // uso startActivityForResult così se NicknameActivity termina esce dall'applicazione
                // può terminare per due motivi, o termina la main activity o l'utente non inserisce il nickname
                ((Activity) context).startActivityForResult(intent, LoginActivity.REGISTRATION_REQUEST);
                return context.getResources().getString(R.string.login_not_registered_message);
            } else {
                Log.v(TAG, " httpResult = " + httpResult);
                return "Error code: " + httpResult;
            }
        } catch (IOException e) {
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // l'utente non è registrato, fa partire la registrazione
                Log.v(TAG, " httpResult = " + httpResult);
                Intent intent = new Intent(context, NicknameActivity.class);
                // uso startActivityForResult così se NicknameActivity termina esce dall'applicazione
                // può terminare per due motivi, o termina la main activity o l'utente non inserisce il nickname
                ((Activity) context).startActivityForResult(intent, LoginActivity.REGISTRATION_REQUEST);
                return context.getResources().getString(R.string.login_not_registered_message);
            }
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return "";
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