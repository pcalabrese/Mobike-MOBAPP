package com.mobike.mobike;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mobike.mobike.network.DownloadRouteTask;
import com.mobike.mobike.utils.CustomMapFragment;
import com.mobike.mobike.utils.Route;

import java.io.IOException;
import java.util.ArrayList;

public class EventActivity extends ActionBarActivity {

    private TextView name, date, creator, description, invited, startLocation, creationDate;
    private Route route;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Polyline routePoly; // the route
    private ArrayList<LatLng> points; // the points of the route

    private final String TAG = "EventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        /* get event and route details from the bundle, route details will be used to visualize the route
         in a new RouteActivity (at the pressure of a button)
         */
        Bundle bundle = getIntent().getExtras();
        name = (TextView) findViewById(R.id.event_name);
        date = (TextView) findViewById(R.id.event_date);
        creator = (TextView) findViewById(R.id.event_creator);
        description = (TextView) findViewById(R.id.event_description);
        invited = (TextView) findViewById(R.id.event_invited);
        startLocation = (TextView) findViewById(R.id.start_location);
        creationDate = (TextView) findViewById(R.id.creation_date);

        // displays event's details in textViews
        name.setText(bundle.getString(EventsFragment.EVENT_NAME));
        date.setText(bundle.getString(EventsFragment.EVENT_DATE));
        creator.setText("Created by " + bundle.getString(EventsFragment.EVENT_CREATOR));
        description.setText(bundle.getString(EventsFragment.EVENT_DESCRIPTION));
        invited.setText(bundle.getString(EventsFragment.EVENT_INVITED));
        startLocation.setText("Start location: " + bundle.getString(EventsFragment.EVENT_START_LOCATION));
        creationDate.setText("Created on " + bundle.getString(EventsFragment.EVENT_CREATION_DATE));

        new DownloadRouteTask(this).execute(bundle.getString(EventsFragment.ROUTE_ID));
    }

    public void setRoute(Route route) {
        this.route = route;

        // get points from route_gpx ,set up the map and finally add the polyline of the route
        GPSDatabase db = new GPSDatabase(this);
        db.open();
        try {
            points = db.gpxToMapPoints(route.getGpx());
        } catch (IOException e) {
        }
        db.close();

        setUpMapIfNeeded();

        routePoly = mMap.addPolyline(new PolylineOptions().width(6).color(Color.BLUE));
        routePoly.setPoints(points);

        Log.v(TAG, "setRoute()");
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

    public void displayRoute(View view) {
        Intent intent = new Intent(this, RouteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(SearchFragment.ROUTE_NAME, route.getName());
        bundle.putString(SearchFragment.ROUTE_DESCRIPTION, route.getDescription());
        bundle.putString(SearchFragment.ROUTE_CREATOR, route.getCreator());
        bundle.putString(SearchFragment.ROUTE_LENGTH, route.getLength());
        bundle.putString(SearchFragment.ROUTE_DURATION, route.getDuration());
        bundle.putString(SearchFragment.ROUTE_GPX, route.getGpx());
        bundle.putString(SearchFragment.ROUTE_DIFFICULTY, route.getDifficulty());
        bundle.putString(SearchFragment.ROUTE_BENDS, route.getBends());
        bundle.putString(SearchFragment.ROUTE_TYPE, route.getType());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}