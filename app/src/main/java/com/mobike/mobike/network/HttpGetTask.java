package com.mobike.mobike.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Andrea-PC on 07/03/2015.
 */

/**
 * This class performs a generic HTTP GET to a given url and pass the String result to the calling Activity with the setResult(String result) method
 */
public class HttpGetTask extends AsyncTask<String, Void, String> {
    private HttpGet httpGet;

    private final static String TAG = "HttpGetTask";

    /**
     * Creates a new HttpetTask
     * @param httpGet
     */
    public HttpGetTask(HttpGet httpGet) {
        this.httpGet = httpGet;
    }

    /**
     * Standard method of Async Task, calls downloadJSON(url) method
     * @param url
     * @return String with the result of the request
     */
    @Override
    protected String doInBackground(String... url) {
        try {
            Log.v(TAG, "starting HttpGetTask");
            return downloadJSON(url[0]);
        } catch (IOException e) {
            Log.v(TAG, "exception  message: " + e.getMessage() + " exception class: " + e.getClass());
            return "Unable to perform netowrk operation (GET). URL may be invalid.";
        }
    }

    /**
     * Standard method of Async Task, calls setResult(result)
     * @param result String with the result of the request
     */
    @Override
    protected void onPostExecute(String result) {
        httpGet.setResult(result);
    }

    /**
     * Performs the HTTP GET
     * @param url
     * @return String with the result of the request
     * @throws IOException
     */
    private String downloadJSON(String url) throws IOException {
        HttpURLConnection urlConnection = null;
        String result = "";
        try {
            URL u = new URL(url);
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            int httpResult = urlConnection.getResponseCode();
            if (httpResult == HttpURLConnection.HTTP_OK) {
                InputStream in = urlConnection.getInputStream();
                if (in != null)
                    result = convertInputStreamToString(in);
                Log.v(TAG, "code: " + httpResult);
            } else {
                // scrive un messaggio di errore con codice httpResult
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                String r;
                r = br.readLine();
                Log.v(TAG, "error: " + r);
                br.close();
                Log.v(TAG, " httpResult = " + httpResult);
            }
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return result;
    }

    /**
     * Utility method to read a String from InputStream
     * @param inputStream
     * @return String read
     * @throws IOException
     */
    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    /**
     * Interface that must be implemented by Activities that use HttpGetTask
     */
    public interface HttpGet {
        public void setResult(String result);
    }
}