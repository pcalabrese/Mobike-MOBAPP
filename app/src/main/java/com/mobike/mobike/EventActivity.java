package com.mobike.mobike;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mobike.mobike.network.DownloadGpxTask;
import com.mobike.mobike.network.HttpGetTask;
import com.mobike.mobike.utils.CustomMapFragment;
import com.mobike.mobike.utils.Route;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventActivity extends ActionBarActivity implements DownloadGpxTask.GpxInterface, HttpGetTask.HttpGet {
    public static final String ROUTE_URL = "http://mobike.ddns.net/SRV/routes/retrieve/";

    private TextView name, date, creator, description, invited, startLocation, creationDate;
    private Route route;
    private String gpx;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Polyline routePoly; // the route
    private ArrayList<LatLng> points; // the points of the route

    private final String TAG = "EventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        setUpMapIfNeeded();

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
/*        Event event = (Event) bundle.getParcelable(EventsFragment.EVENT);
        name.setText(event.getName());
        date.setText(event.getDate());
        creator.setText("Created by " + event.getCreator());
        description.setText(event.getDescription());
        invited.setText(event.getInvited().toString());
        startLocation.setText("Start location: " + event.getStartLocation());
        creationDate.setText("Created on " + event.getCreationDate()); */

        Date mDate = null, mDateCreation = null;
        SimpleDateFormat s1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            mDate = s1.parse(bundle.getString(EventsFragment.EVENT_DATE));
            mDateCreation = s1.parse(bundle.getString(EventsFragment.EVENT_CREATION_DATE));
        } catch (ParseException e ) { }

        name.setText(bundle.getString(EventsFragment.EVENT_NAME));
        date.setText(new SimpleDateFormat("EEEE, d MMMM yyyy\nkk:mm").format(mDate));
        creator.setText(bundle.getString(EventsFragment.EVENT_CREATOR));
        description.setText(bundle.getString(EventsFragment.EVENT_DESCRIPTION));
        invited.setText(bundle.getString(EventsFragment.EVENT_INVITED));
        startLocation.setText(bundle.getString(EventsFragment.EVENT_START_LOCATION));
        creationDate.setText(new SimpleDateFormat("EEEE, d MMMM yyyy").format(mDateCreation));

        new HttpGetTask(this).execute(ROUTE_URL + bundle.getString(EventsFragment.ROUTE_ID));
        new DownloadGpxTask(this).execute(bundle.getString(EventsFragment.ROUTE_ID));
    }

    @Override
    public void setGpx(String gpx) {
        this.gpx = gpx;

        // get points from route_gpx ,set up the map and finally add the polyline of the route
        GPSDatabase db = new GPSDatabase(this);
        db.open();
        try {
            points = db.gpxToMapPoints(gpx);
        } catch (IOException e) {
        }
        db.close();

        setUpMap();

        routePoly = mMap.addPolyline(new PolylineOptions().width(6).color(Color.BLUE));
        routePoly.setPoints(points);

        Log.v(TAG, "points size = " + points.size());
        Log.v(TAG, "setGpx(), gpx: " + gpx);
        Log.v(TAG, "setGpx()");
    }

    // set Route
    public void setResult(String result) {
        try{
            JSONObject jsonRoute = new JSONObject(result);
            String name = jsonRoute.getString("name");
            String description = jsonRoute.getString("description");
            String creator = jsonRoute.getString("creatorEmail");
            String length = jsonRoute.getDouble("length") + "";
            String duration = jsonRoute.getInt("duration")+"";
            Bitmap map = null;
            //String gpx = jsonRoute.getString("url");
            String gpx = "";
            String difficulty = jsonRoute.getInt("difficulty") + "";
            String bends = jsonRoute.getInt("bends") + "";
            String type = "DefaultRouteType";
            String id = jsonRoute.getInt("id") + "";
            route = new Route(name, description, creator, length, duration, map, gpx, difficulty, bends, type, id);
        }catch(JSONException e){
            e.printStackTrace();
        }
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
/*            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(points.get(points.size() / 2),
                    MapsFragment.CAMERA_ZOOM_VALUE - 5);
            mMap.animateCamera(update); */

            LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
            for (LatLng point : points) {
                boundsBuilder.include(point);
            }
            LatLngBounds bounds = boundsBuilder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10);
            mMap.animateCamera(cameraUpdate);
            Log.v(TAG, "setUpMap()");
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
        bundle.putString(SearchFragment.ROUTE_DIFFICULTY, route.getDifficulty());
        bundle.putString(SearchFragment.ROUTE_BENDS, route.getBends());
        bundle.putString(SearchFragment.ROUTE_TYPE, route.getType());
        bundle.putString(SearchFragment.ROUTE_ID, route.getID());
        intent.putExtras(bundle);
        startActivity(intent);
    }
}