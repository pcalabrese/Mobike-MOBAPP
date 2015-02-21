package com.mobike.mobike.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.mobike.mobike.EventActivity;
import com.mobike.mobike.SearchFragment;
import com.mobike.mobike.utils.Route;

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

/**
 * Created by Andrea-PC on 21/02/2015.
 */
public class DownloadRouteTask extends AsyncTask<String, Void, String> {
    private final String downloadRouteURL = "http://mobike.ddns.net/SRV/routes/retrieve/";
    private Activity activity;
    private ProgressDialog progressDialog;

    public DownloadRouteTask (Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... urls) {
        return HTTPGetRoutes(downloadRouteURL + urls[0]);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = ProgressDialog.show(activity, "Downloading route...", "", true, false);
    }

    @Override
    protected void onPostExecute(String result) {
        try{
            JSONObject jsonRoute = new JSONObject(result);
            String name = jsonRoute.getString("name");
            String description = jsonRoute.getString("description");
            String creator = jsonRoute.getString("creatorEmail");
            String length = jsonRoute.getDouble("length") + "";
            String duration = jsonRoute.getInt("duration")+"";
            Bitmap map = null;
            //String gpx = jsonRoute.getString("url");
            String gpx = "gpx";
            String difficulty = jsonRoute.getInt("difficulty") + "";
            String bends = jsonRoute.getInt("bends") + "";
            String type = "DefaultRouteType";
            ((EventActivity) activity).setRoute(new Route(name, description, creator, length, duration, map, gpx, difficulty, bends, type));
        }catch(JSONException e){
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    private String HTTPGetRoutes(String url) {
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
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else {
                return null;
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
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