package com.mobike.mobike.network;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Andrea-PC on 24/02/2015.
 */
public class DownloadGpxTask extends AsyncTask<String, Void, String> {
    public static final String downloadGpxURL = "http://mobike.ddns.net/SRV/routes/retrieve";
    private GpxInterface gpxInterface;

    public DownloadGpxTask(GpxInterface gpxInterface) {
        this.gpxInterface = gpxInterface;
    }

    @Override
    protected String doInBackground(String... urls) {
        return HTTPGetGpx(downloadGpxURL + "/" + urls[0] + "/gpx");
    }

    @Override
    protected void onPostExecute(String result) {
        gpxInterface.setGpx(result);
    }

    private String HTTPGetGpx(String url){
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


    public interface GpxInterface {
        public void setGpx(String gpx);
    }
}
