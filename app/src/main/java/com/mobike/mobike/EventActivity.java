package com.mobike.mobike;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.mobike.mobike.utils.Event;
import com.mobike.mobike.utils.Route;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventActivity extends ActionBarActivity implements HttpGetTask.HttpGet, View.OnClickListener {
    public static final String EVENT_URL = "http://mobike.ddns.net/SRV/events/retrieve/";

    private TextView mName, mDate, mCreator, mDescription, mInvited, mStartLocation, mCreationDate;
    private ImageView mThumbnail;
    private Route route;
    private String gpx, id, routeID;
    private int state;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Polyline routePoly; // the route
    private ArrayList<LatLng> points; // the points of the route

    private final String TAG = "EventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        //setUpMapIfNeeded();

        getSupportActionBar().hide();

        /* get event and route details from the bundle, route details will be used to visualize the route
         in a new RouteActivity (at the pressure of a button)
         */
        Bundle bundle = getIntent().getExtras();
        mName = (TextView) findViewById(R.id.event_name);
        mDate = (TextView) findViewById(R.id.event_date);
        mCreator = (TextView) findViewById(R.id.event_creator);
        mDescription = (TextView) findViewById(R.id.event_description);
        mInvited = (TextView) findViewById(R.id.event_invited);
        mStartLocation = (TextView) findViewById(R.id.start_location);
        mCreationDate = (TextView) findViewById(R.id.creation_date);
        mThumbnail = (ImageView) findViewById(R.id.event_map);

        state = bundle.getInt(EventsFragment.EVENT_STATE);
        id = bundle.getString(EventsFragment.EVENT_ID);
        routeID = bundle.getString(EventsFragment.ROUTE_ID);

        //new HttpGetTask(this).execute(ROUTE_URL + bundle.getString(EventsFragment.ROUTE_ID));
        //new DownloadGpxTask(this).execute(bundle.getString(EventsFragment.ROUTE_ID));
        new HttpGetTask(this).execute(EVENT_URL + id);

        //inflate dei giusto button per accettare o declinare l'invito
        LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.button_layout);

        if (state == Event.INVITED) {
            Button accept = (Button) getLayoutInflater().inflate(R.layout.accept_button, buttonLayout, false);
            Button decline = (Button) getLayoutInflater().inflate(R.layout.decline_button, buttonLayout, false);
            buttonLayout.addView(accept);
            buttonLayout.addView(decline);
            accept.setOnClickListener(this);
            decline.setOnClickListener(this);
        } else if (state == Event.ACCEPTED) {
            TextView accepted = (TextView) getLayoutInflater().inflate(R.layout.accepted_textview, buttonLayout, false);
            buttonLayout.addView(accepted);
        } else if (state == Event.REFUSED) {
            TextView declined = (TextView) getLayoutInflater().inflate(R.layout.declined_textview, buttonLayout, false);
            buttonLayout.addView(declined);
        }

    }


    public void setResult(String result) {
        String name="", date="", creator="", description="", accepted="", invited="", refused="", startLocation="", creationDate="", thumbnailURL="";
        JSONObject jsonEvent;

        try {
            jsonEvent = new JSONObject(result);
            name = jsonEvent.getString("name");
            name = name.substring(0,1).toUpperCase() + name.substring(1);
            date = jsonEvent.getString("startDate");
            creator = jsonEvent.getJSONObject("owner").getString("nickname");
            description = jsonEvent.getString("description");
            startLocation = jsonEvent.getString("startLocation");
            creationDate = jsonEvent.getString("creationDate");
            thumbnailURL = jsonEvent.getString("route imgUrl");
        } catch (JSONException e) {}


        Date eventDate = null, mDateCreation = null;
        SimpleDateFormat s1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            eventDate = s1.parse(date);
            mDateCreation = s1.parse(creationDate);
        } catch (ParseException e ) { }

        mName.setText(name);
        mDate.setText(new SimpleDateFormat("EEEE, d MMMM yyyy\nkk:mm").format(eventDate));
        mCreator.setText(creator);
        mDescription.setText(description);
        mInvited.setText(invited);
        mStartLocation.setText(startLocation);
        mCreationDate.setText(new SimpleDateFormat("EEEE, d MMMM yyyy").format(mDateCreation));
        Picasso.with(this).load(thumbnailURL).into(mThumbnail);
    }


    /*@Override
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
    } */

    // set Route
    /*public void setResult(String result) {
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
    } */

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
        //setUpMapIfNeeded();
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
        /*bundle.putString(SearchFragment.ROUTE_NAME, route.getName());
        bundle.putString(SearchFragment.ROUTE_DESCRIPTION, route.getDescription());
        bundle.putString(SearchFragment.ROUTE_CREATOR, route.getCreator());
        bundle.putString(SearchFragment.ROUTE_LENGTH, route.getLength());
        bundle.putString(SearchFragment.ROUTE_DURATION, route.getDuration());
        bundle.putString(SearchFragment.ROUTE_DIFFICULTY, route.getDifficulty());
        bundle.putString(SearchFragment.ROUTE_BENDS, route.getBends());
        bundle.putString(SearchFragment.ROUTE_TYPE, route.getType()); */
        bundle.putString(SearchFragment.ROUTE_ID, routeID);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        switch (view.getId()) {
            case R.id.accept_event_button:
                // post per accettare l'invito

                removeButtons();
                TextView accepted = (TextView) getLayoutInflater().inflate(R.layout.accepted_textview, buttonLayout, false);
                buttonLayout.addView(accepted);
                break;
            case R.id.decline_event_button:
                // post per declinare l'invito

                removeButtons();
                TextView declined = (TextView) getLayoutInflater().inflate(R.layout.declined_textview, buttonLayout, false);
                buttonLayout.addView(declined);
                break;
        }
    }

    private void removeButtons() {
        ((Button) findViewById(R.id.accept_event_button)).setVisibility(View.GONE);
        ((Button) findViewById(R.id.decline_event_button)).setVisibility(View.GONE);
    }
}