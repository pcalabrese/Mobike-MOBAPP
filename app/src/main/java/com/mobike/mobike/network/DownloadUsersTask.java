package com.mobike.mobike.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.mobike.mobike.EventCreationActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Andrea-PC on 22/02/2015.
 */
public class DownloadUsersTask extends AsyncTask<String, Void, String> {
    private Activity activity;
    private static final String downloadUsersURL = "qualcosa";

    public DownloadUsersTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... urls) {
        return HTTPGetUsers(downloadUsersURL);
    }

    @Override
    protected void onPostExecute(String result) {
        ArrayList<String> userList = new ArrayList<>();
        try{
            JSONArray array = new JSONArray(result);
            for (int i = 0; i < array.length(); i++) {
                JSONObject user = array.getJSONObject(i);
                String name = user.getString("name");
                userList.add(name);
                ((EventCreationActivity) activity).setUsersHints(userList);
            }
        }catch(JSONException e)
        { e.printStackTrace();}
    }

    private String HTTPGetUsers(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else{
                return null;}

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}