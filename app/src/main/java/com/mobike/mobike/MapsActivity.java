package com.mobike.mobike;

import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * This Activity implements the route recording controls.
 */

public class MapsActivity extends ActionBarActivity implements NewLocationsListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LinearLayout buttonLayout;
    private Button start, pause, stop, resume;
    private enum State {BEGIN, RUNNING, PAUSED, STOPPED} // All the possible states
    private State state;        // The current state
    private static final String TAG = "MapsActivity";
    protected static final float CAMERA_ZOOM_VALUE = 15;    // The value of the map zoom. It must
                                                        // be between 2 (min zoom) and 21 (max)
    private GPSTracker gps;     // The Service and LocationListener object, that manages
                                   // the route recording

    private Polyline route;     // The currently recording route to be drawn in the map
    private List<LatLng> points;    // the points of the route

    /**
     * This method is called when the activity is created.
     * It initializes the layout, the map the route to be drawn on the map and the state.
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        start = (Button) findViewById(R.id.start_button);
        state = State.BEGIN;
        route = mMap.addPolyline(new PolylineOptions().width(6).color(Color.BLUE));
        points = new ArrayList<LatLng>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Keep the screen always on for this activity
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Return to the default settings, the screen can go off for inactivity
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * This method creates the items of the options menu.
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.map_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This method sets the map up, displaying the user's position and zooming on it.
     *
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // Enabling MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        if(location!=null) {
            // Getting latitude of the current location
            double latitude = location.getLatitude();

            // Getting longitude of the current location
            double longitude = location.getLongitude();

            // Creating a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);
            // Creating blue marker showing the current location
            mMap.addMarker(new MarkerOptions().position(latLng).title("Start"));
            // zooming to the current location
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_VALUE); //zoom value between 2(min zoom)-21(max zoom)
            mMap.animateCamera(update);
        }
    }

    /**
     * This method is invoked by onChangedLocation() in GPSTracker service class.
     * It adds the new location to the route on the map.
     *
     * @param location the last updated location (see GPSTracker class)
     */
    public void onNewLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_VALUE); //zoom value between 2(min zoom)-21(max zoom)
        mMap.animateCamera(update);
        points.add(latLng);
        route.setPoints(points);
    }


    /**
     * This method is invoked when the "Start" button is pressed.
     * It changes the button in the lower part of the screen
     * and creates the GPSTracker object, which constructor takes as arguments
     * the context and the MapsActivity object (because it implements NewLocationsListener).
     * Once created, the GPSTracker object makes the route recording start.
     *
     * @param view
     */
    public void startButtonPressed(View view) {
        if (view.getId() == R.id.start_button) {
            start.setVisibility(View.GONE);
            pause = (Button) getLayoutInflater().inflate(R.layout.pause_button, buttonLayout, false);
            stop = (Button) getLayoutInflater().inflate(R.layout.stop_button, buttonLayout, false);
            buttonLayout.addView(pause);
            buttonLayout.addView(stop);
            //        startService(new Intent(this, TrackingService.class));
            state = State.RUNNING;

            gps = new GPSTracker(this, this);
        }
    }

    /**
     * This method is invoked when the "Pause" button is pressed.
     * The layout of the lower part of the screen changes and the route recording stops.
     *
     * @param view
     */
    public void pauseButtonPressed(View view) {
        if (view.getId() == R.id.pause_button) {
            pause.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            resume = (Button) getLayoutInflater().inflate(R.layout.resume_button, buttonLayout, false);
            stop = (Button) getLayoutInflater().inflate(R.layout.stop_button, buttonLayout, false);
            buttonLayout.addView(resume);
            buttonLayout.addView(stop);
            state = State.PAUSED;
            gps.stopUsingGPS();
        }
    }

    /**
     * This method is invoked when the "Resume" button is pressed.
     * The layout of the lower part of the screen changes and the route recording starts again.
     * @param view
     */
    public void resumeButtonPressed(View view) {
        if (view.getId() == R.id.resume_button) {
            resume.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            pause = (Button) getLayoutInflater().inflate(R.layout.pause_button, buttonLayout, false);
            stop = (Button) getLayoutInflater().inflate(R.layout.stop_button, buttonLayout, false);
            buttonLayout.addView(pause);
            buttonLayout.addView(stop);
            state = State.RUNNING;
            gps.startUsingGPS();
        }
    }

    /**
     * This method is invoked when the "Stop" button is pressed.
     * The route recording is definetely stopped and the SummaryActivity starts.
     *
     * @param view
     */
    public void stopButtonPressed(View view) {
        if (view.getId() == R.id.stop_button) {
            if (resume != null) resume.setVisibility(View.GONE);
            if (pause != null) pause.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            start = (Button) getLayoutInflater().inflate(R.layout.start_button, buttonLayout, false);
            buttonLayout.addView(start);

            if(state == State.RUNNING) {
                state = State.STOPPED;
                gps.stopUsingGPS();
            }
            else { state = State.STOPPED;}


            Intent intent = new Intent(this, SummaryActivity.class);
            startActivity(intent);
			

        }
    }

    //metodo che viene chiamato quando il percorso viene cancellato, cioè quando si ritorna dalla SummaryActivity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // cancellazione del percorso attuale e reset dell'activity
        //activity pronta a registrare un nuovo percorso

        Log.v(TAG, "onActivityResult()");
    }
}

/**
 * This interface is useful to access newly updated location without using intents from
 * GPSTracker service.
 */
interface NewLocationsListener {
    public void onNewLocation(Location location);
}
