package com.mobike.mobike;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mobike.mobike.utils.CustomMapFragment;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RouteActivity extends ActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Polyline route; // the polyline of the route
    private ArrayList<LatLng> points; // the points of the route

    private TextView name, description, creator, length, duration;
    private String gpx;
    private static final String downloadURL = "http//:mobike.ddns.net/SRV/qualcosa";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // get data from bundle and displays in textViews
        Bundle bundle = getIntent().getExtras();
        name = (TextView) findViewById(R.id.route_name);
        description = (TextView) findViewById(R.id.route_description);
        creator = (TextView) findViewById(R.id.route_creator);
        length = (TextView) findViewById(R.id.route_length);
        duration = (TextView) findViewById(R.id.route_duration);

        name.setText(bundle.getString(SearchFragment.ROUTE_NAME));
        description.setText(bundle.getString(SearchFragment.ROUTE_DESCRIPTION));
        creator.setText(bundle.getString(SearchFragment.ROUTE_CREATOR));
        length.setText(bundle.getString(SearchFragment.ROUTE_LENGTH));
        duration.setText(bundle.getString(SearchFragment.ROUTE_DURATION));
        gpx = bundle.getString(SearchFragment.ROUTE_GPX);

/*        GPSDatabase db = new GPSDatabase(this);
        db.open();
        points = db.gpxToMapsPoints(gpx);
        db.close();
*/
        setUpMapIfNeeded();
/*
        route = mMap.addPolyline(new PolylineOptions().width(6).color(Color.BLUE));
        route.setPoints(points);
*/
    }

    // method to finish current activity at the pressure of top left back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // set listener to add the possibility to scroll the map inside the scroll view
                ((CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).setListener(new CustomMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        ((ScrollView) findViewById(R.id.scroll_view)).requestDisallowInterceptTouchEvent(true);
                    }
                });
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (points != null && points.size() > 0) {
            //saving the first and the last ones
            LatLng start = points.get(0);
            LatLng end = points.get(points.size() - 1);

            // Adding the start and end markers
            mMap.addMarker(new MarkerOptions().position(start).title("Start"));
            mMap.addMarker(new MarkerOptions().position(end).title("End"));
            // Zooming on the route
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(points.get(points.size() / 2),
                    MapsFragment.CAMERA_ZOOM_VALUE - 5);
            mMap.animateCamera(update);
        }
    }

    /*private class DLRouteDetailsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return HTTPGetRoute(downloadURL);
        }

        @Override
        protected void onPostExecute(String result) {
            try{
                JSONObject json = new JSONObject(result);
            }catch(JSONException e)
            { e.printStackTrace();}

            showRouteDetails(json);
        }

        private String HTTPGetEvent(String url){
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
    }*/
}
