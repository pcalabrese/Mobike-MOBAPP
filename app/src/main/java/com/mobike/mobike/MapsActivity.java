package com.mobike.mobike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

// Activity principale con la mappa e i bottoni per controllare la registrazione del percorso

public class MapsActivity extends ActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LinearLayout mainLayout, buttonLayout;
    private Button start, pause, stop, resume;
    private enum State {BEGIN, RUNNING, PAUSED, STOPPED};
    private State state;
    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        start = (Button) findViewById(R.id.start_button);
        state = State.BEGIN;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    // Metodo che crea le voci nel menu a tendina

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
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    // Listener per i bottoni start, pause, resume e stop

    public void startButtonPressed(View view) {
        if (view.getId() == R.id.start_button) {
            start.setVisibility(View.GONE);
            pause = (Button) getLayoutInflater().inflate(R.layout.pause_button, buttonLayout, false);
            stop = (Button) getLayoutInflater().inflate(R.layout.stop_button, buttonLayout, false);
            buttonLayout.addView(pause);
            buttonLayout.addView(stop);
            //        startService(new Intent(this, TrackingService.class));
            state = State.RUNNING;
        }
    }

    public void pauseButtonPressed(View view) {
        if (view.getId() == R.id.pause_button) {
            pause.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            resume = (Button) getLayoutInflater().inflate(R.layout.resume_button, buttonLayout, false);
            stop = (Button) getLayoutInflater().inflate(R.layout.stop_button, buttonLayout, false);
            buttonLayout.addView(resume);
            buttonLayout.addView(stop);
            state = State.PAUSED;
        }
    }

    public void resumeButtonPressed(View view) {
        if (view.getId() == R.id.resume_button) {
            resume.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            pause = (Button) getLayoutInflater().inflate(R.layout.pause_button, buttonLayout, false);
            stop = (Button) getLayoutInflater().inflate(R.layout.stop_button, buttonLayout, false);
            buttonLayout.addView(pause);
            buttonLayout.addView(stop);
            state = State.RUNNING;
        }
    }

    public void stopButtonPressed(View view) {
        if (view.getId() == R.id.stop_button) {
            if (resume != null) resume.setVisibility(View.GONE);
            if (pause != null) pause.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            start = (Button) getLayoutInflater().inflate(R.layout.start_button, buttonLayout, false);
            buttonLayout.addView(start);
            //        stopService(new Intent(this, TrackingService.class));
            state = State.STOPPED;
            Intent intent = new Intent(this, SummaryActivity.class);
            startActivity(intent);
        }
    }
}
