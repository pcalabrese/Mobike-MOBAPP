package com.mobike.mobike.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.mobike.mobike.EventsFragment;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Andrea-PC on 21/02/2015.
 */
public class DownloadEventsTask extends AsyncTask<String, Void, String> {
    private Fragment fragment;
    private Activity activity;
    private ProgressDialog progressDialog;

    public DownloadEventsTask(Activity activity, Fragment fragment) {
        this.fragment = fragment;
        this.activity = activity;
    }
    @Override
    protected String doInBackground(String... urls) {
        return HTTPGetEvents(urls[0]);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(activity, "Downloading events...", "", true, false);
    }

    @Override
    protected void onPostExecute(String result) {
        try{
            JSONArray json = new JSONArray(result);
            ((EventsFragment) fragment).showEventsList(json);
        }catch(JSONException e)
        { e.printStackTrace();}

        progressDialog.dismiss();
    }

    private String HTTPGetEvents(String url){
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