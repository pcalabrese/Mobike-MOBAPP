package com.mobiketeam.mobike;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mobiketeam.mobike.utils.Crypter;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This is the activity where are displayed details of user's account like image, name, email, nickname and bike model
 */
public class AccountDetailsActivity extends ActionBarActivity {

    private static final String TAG = "AccountDetailsActivity";
    public final static String ACCOUNT_URL = "http://mobike.ddns.net/SRV/users/getDetails?token=";

    /**
     * Activity lifecycle method, initializes the UI
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        ImageView imageView = (ImageView) findViewById(R.id.image);

        SharedPreferences preferences = getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
        String imageUrl = preferences.getString(LoginActivity.IMAGEURL, "");
        String name = preferences.getString(LoginActivity.NAME, "") + " " + preferences.getString(LoginActivity.SURNAME, "");
        String nickname = preferences.getString(LoginActivity.NICKNAME, "");
        String email = preferences.getString(LoginActivity.EMAIL, "");

        imageUrl = imageUrl.split("sz=")[0] + "sz=500";

        Picasso.with(this).load(imageUrl).into(imageView);
        ((TextView) findViewById(R.id.name)).setText(name);
        ((TextView) findViewById(R.id.nickname)).setText(nickname);
        ((TextView) findViewById(R.id.email)).setText(email);

        downoadDetails();
    }

    private void downoadDetails() {
        String user = "";
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
        int id = sharedPreferences.getInt(LoginActivity.ID, 0);
        String nickname = sharedPreferences.getString(LoginActivity.NICKNAME, "");
        Crypter crypter = new Crypter();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("nickname", nickname);
            user = URLEncoder.encode(crypter.encrypt(jsonObject.toString()), "utf-8");
            Log.v(TAG, "json per l'account: " + jsonObject.toString());
        } catch (JSONException e) {
            Log.v(TAG, "json exception in downloadDetails()");
        }
        catch (UnsupportedEncodingException uee) {}

        String url = ACCOUNT_URL + user + "&id=" + id;

        Log.v(TAG, "downloadDetail(), url: " + url);

        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        setDetails(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "Errore nel download dei dettagli dell'account, error: " + error);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    private void setDetails(String result) {
        JSONObject user;
        String name = "", nickname = "", email = "", bike = "", imageUrl = "";
        Crypter crypter = new Crypter();

        try {
            user = new JSONObject(crypter.decrypt((new JSONObject(result)).getString("user")));
            name = user.getString("name") + " " + user.getString("surname");
            email = user.getString("email");
            bike = user.getString("bikemodel");
            //imageUrl = user.getString("imgurl");
            nickname = user.getString("nickname");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //ImageView imageView = (ImageView) findViewById(R.id.image);
        //imageUrl = imageUrl.split("sz=")[0] + "sz=500";
        //Picasso.with(this).load(imageUrl).into(imageView);
        ((TextView) findViewById(R.id.name)).setText(name);
        ((TextView) findViewById(R.id.nickname)).setText(nickname);
        ((TextView) findViewById(R.id.email)).setText(email);
        ((TextView) findViewById(R.id.bike)).setText(bike);
    }
}
