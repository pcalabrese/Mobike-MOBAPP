package com.mobike.mobike;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * This Fragment implements the route recording controls.
 */

public class MapsFragment extends android.support.v4.app.Fragment implements
        NewLocationListener, View.OnClickListener {

    private Location mCurrentLocation;
    private ActionBarActivity activity;

    private static final int SUMMARY_REQUEST = 1;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LinearLayout buttonLayout;
    private Button start, pause, stop, resume;


    private enum State {BEGIN, RUNNING, PAUSED, STOPPED} // All the possible states
    private State state;        // The current state
    private static final String TAG = "MapsFragment";
    protected static final float CAMERA_ZOOM_VALUE = 15;    // The value of the map zoom. It must
    // be between 2 (min zoom) and 21 (max)

    private Polyline route;     // The currently recording route to be drawn in the map
    private List<LatLng> points;    // the points of the route

    private GPSService gpsService;  //the reference to the Service
    private boolean registered; //true if at least one location was inserted in the database
    private boolean back; // true if i'm returned from SummaryActivity


    /**
     * This method is called when the activity is created.
     * It initializes the layout, the map the route to be drawn on the map and the state.
     *
     * @param savedInstanceState I don't know
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //bisogna ripristinare lo stato salvato, quindi bottoni, percorso sulla mappa e variabili

        /*mResolvingError = savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);*/
        // checks the GPS status and, it is disabled, shows the user an alert
        checkGPSStatus();
        registered = false;
        gpsService = new GPSService(getActivity(), this);
        Log.v(TAG, "onCreate()");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!back)
            setUp();
        Log.v(TAG, "onStart()");
    }


    private void setUp(){
        buttonLayout = (LinearLayout) getView().findViewById(R.id.button_layout);
        start = (Button) getView().findViewById(R.id.start_button);
        state = State.BEGIN;
        start.setOnClickListener(this);
        //    setUpMapIfNeeded();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        Log.v(TAG, "onCreateView()");
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
/*        FragmentManager fm = getActivity().getSupportFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(fragment);
        ft.commit();
        Log.v(TAG, "onDestroyView()");*/
    }

    /**
     * This method checks the GPS status and if it is not enabled, shows the user a dialog.
     */
    private void checkGPSStatus(){
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isGPSEnabled){ showSettingsAlert(); }
    }

    /**
     * This method updates the map, making it center on the last plocation known.
     * @param location  the last location known.
     */
    public void updateCamera(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        // zooming to the current location
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, CAMERA_ZOOM_VALUE); //zoom value between 2(min zoom)-21(max zoom)
        mMap.animateCamera(update);
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = (ActionBarActivity) getActivity();

        // Keep the screen always on for this activity
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setUpMapIfNeeded();
        Log.v(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();

        // Return to the default settings, the screen can go off for inactivity
        getActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.v(TAG, "onPause()");
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()");
    }

    /**
     * This method creates the items of the options menu.
     * @param menu the options menu
     * @return true
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This method sets the map up, initializing the route.
     *
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // Enabling MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);
        //initialises the route on the map
        route = mMap.addPolyline(new PolylineOptions().width(6).color(Color.BLUE));
        points = new ArrayList<>();
    }

    /**
     * This method updates the map, adding the last known location to the route drawn on it.
     * @param location the last known location.
     */
    private void updateUIRoute(Location location, float length, long duration){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        points.add(latLng);
        route.setPoints(points);
        ((TextView) getActivity().findViewById(R.id.current_length)).setText(String.format("%.01f", length/1000) + " km");
        ((TextView) getActivity().findViewById(R.id.current_duration)).setText(String.valueOf(duration/3600) + " h " + String.valueOf((duration/60)%60) + " m " + String.valueOf(duration%60) + " s");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_button:
                startButtonPressed(view);
                break;
            case R.id.pause_button:
                pauseButtonPressed(view);
                break;
            case R.id.resume_button:
                resumeButtonPressed(view);
                break;
            case R.id.stop_button:
                stopButtonPressed(view);
                break;
        }
    }

    /**
     * This method is invoked when the "Start" button is pressed.
     * It changes the button in the lower part of the screen
     * and makes the recording of the route start.
     *
     * @param view the view
     */
    public void startButtonPressed(View view) {
        if (view.getId() == R.id.start_button) {
            /*if (!mGoogleApiClient.isConnected()){mGoogleApiClient.connect(); }*/
            ((Button) view).setVisibility(View.GONE);
            pause = (Button) getActivity().getLayoutInflater().inflate(R.layout.pause_button, buttonLayout, false);
            stop = (Button) getActivity().getLayoutInflater().inflate(R.layout.stop_button, buttonLayout, false);
            buttonLayout.addView(pause);
            buttonLayout.addView(stop);
            pause.setOnClickListener(this);
            stop.setOnClickListener(this);
            state = State.RUNNING;
            gpsService.register();
            mCurrentLocation = gpsService.getLocation();
        }
    }

    /**
     * This method is invoked when the "Pause" button is pressed.
     * The layout of the lower part of the screen changes and the route recording stops.
     *
     * @param view the view
     */
    public void pauseButtonPressed(View view) {
        if (view.getId() == R.id.pause_button) {
            pause.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            resume = (Button) getActivity().getLayoutInflater().inflate(R.layout.resume_button, buttonLayout, false);
            stop = (Button) getActivity().getLayoutInflater().inflate(R.layout.stop_button, buttonLayout, false);
            buttonLayout.addView(resume);
            buttonLayout.addView(stop);
            resume.setOnClickListener(this);
            stop.setOnClickListener(this);
            state = State.PAUSED;
            gpsService.stopRegistering();
        }
    }

    /**
     * This method is invoked when the "Resume" button is pressed.
     * The layout of the lower part of the screen changes and the route recording starts again.
     * @param view the view
     */
    public void resumeButtonPressed(View view) {
        if (view.getId() == R.id.resume_button) {
            resume.setVisibility(View.GONE);
            stop.setVisibility(View.GONE);
            pause = (Button) getActivity().getLayoutInflater().inflate(R.layout.pause_button, buttonLayout, false);
            stop = (Button) getActivity().getLayoutInflater().inflate(R.layout.stop_button, buttonLayout, false);
            buttonLayout.addView(pause);
            buttonLayout.addView(stop);
            pause.setOnClickListener(this);
            stop.setOnClickListener(this);
            state = State.RUNNING;
            gpsService.register();
            mCurrentLocation = gpsService.getLocation();
        }
    }

    /**
     * This method is invoked when the "Stop" button is pressed.
     * The route recording is definitevely stopped and the SummaryActivity starts.
     *
     * @param view the view
     */
    public void stopButtonPressed(View view) {
        if (view.getId() == R.id.stop_button) {
            final View.OnClickListener onClickListener = this;
            TextView titleView = ((TextView) getActivity().getLayoutInflater().inflate(R.layout.list_dialog_title, null, false));
            titleView.setText(getResources().getString(R.string.stop));
            if(registered) {
                //alert dialog to stop the registration
                new AlertDialog.Builder(getActivity())
                        .setCustomTitle(titleView)
                        .setMessage(getResources().getString(R.string.stop_registration))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // stop the registration
                                stopRegistration(onClickListener);
                                Intent intent = new Intent(getActivity(), SummaryActivity.class);
                                startActivityForResult(intent, SUMMARY_REQUEST);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else {
                // dialog to stop the registration if there are no recorded positions
                new AlertDialog.Builder(getActivity())
                        .setCustomTitle(titleView)
                        .setMessage(getResources().getString(R.string.stop_registration_no_positions))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // stop the registration
                                stopRegistration(onClickListener);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }

    private void stopRegistration(View.OnClickListener onClickListener) {
        if (resume != null) resume.setVisibility(View.GONE);
        if (pause != null) pause.setVisibility(View.GONE);
        stop.setVisibility(View.GONE);
        start = (Button) getActivity().getLayoutInflater().inflate(R.layout.start_button, buttonLayout, false);
        buttonLayout.addView(start);
        start.setOnClickListener(onClickListener);

        if (state == State.RUNNING) {
            state = State.STOPPED;
            gpsService.stopRegistering();
        } else {
            state = State.STOPPED;
        }
        gpsService.stopRegistering();
    }

    /**
     * This method shows an alert inviting the user to activate the GPS in the settings menu.
     */
    public void showSettingsAlert(){
        TextView titleView = ((TextView) getActivity().getLayoutInflater().inflate(R.layout.list_dialog_title, null, false));
        titleView.setText("GPS in settings");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setCustomTitle(titleView);

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    /**
     * This method is called by the service whenever a new location is updated.
     * It makes the camera update and, if registering, the last location known
     * to be added to the route to be added on the map.
     * @param location The last location updated.
     */
    public void onNewLocation(Location location, float length, long duration){
        if (location != null) {
            Log.v(TAG, "Location accuracy: " + String.valueOf(location.getAccuracy()));
        //    Toast.makeText(getActivity(), "Location accuracy: " + String.valueOf(location.getAccuracy()), Toast.LENGTH_SHORT).show();
        }
        updateCamera(location);
        updateUIRoute(location, length, duration);
        mCurrentLocation = location;
    }
    public void setRegistered(){registered = true;}

    /**
     *this method is called whenever the app comes back to MapsActivity from SummaryActivity
     * @param requestCode boh
     * @param resultCode dunno
     * @param data nada
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        points = new ArrayList<>();
        route.setPoints(points);

        GPSDatabase db = new GPSDatabase(getActivity());
        db.deleteTable();
        mCurrentLocation = null;
        registered = false;
        gpsService.setDistanceToZero();
        db.close();

        back = true;
        Log.v(TAG, "onActivityResult()");
    }


}



/**
 * This interface makes it possible to communicate between GPSService and MapsActivity, giving
 * a reference to a MapsActivity object, which implements NewLocationListener [see GPSService]
 */
interface NewLocationListener {
    public void onNewLocation(Location location, float length, long duration);
    public void updateCamera(Location location);
    public void setRegistered();
}
