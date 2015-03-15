package com.mobike.mobike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class RouteActivity extends ActionBarActivity implements DownloadGpxTask.GpxInterface, View.OnClickListener, HttpGetTask.HttpGet {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Polyline routePoly; // the polyline of the route
    private ArrayList<LatLng> points; // the points of the route

    private TextView name, description, creator, length, duration, difficulty, bends, type;
    private String gpx, routeID;
    private boolean pickingRoute;

    private static final String TAG = "RouteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        setUpMapIfNeeded();

        getSupportActionBar().hide();

        ((Button) findViewById(R.id.new_review_button)).setOnClickListener(this);

        pickingRoute = getIntent().getExtras().getInt(SearchFragment.REQUEST_CODE) == EventCreationActivity.ROUTE_REQUEST;

        // inflate del bottone "PICK THIS ROUTE" in un linear layout vuoto, che chiama setResult(RESULT_OK, intent)
        if (pickingRoute) {
            RelativeLayout buttonLayout = (RelativeLayout) findViewById(R.id.pick_button_layout);
            buttonLayout.addView(getLayoutInflater().inflate(R.layout.pick_this_route_button, buttonLayout, false));
            ((ImageButton) findViewById(R.id.pick_this_route_button)).setOnClickListener(this);
        }

        // get data from bundle and displays in textViews
        Bundle bundle = getIntent().getExtras();
        name = (TextView) findViewById(R.id.route_name);
        description = (TextView) findViewById(R.id.route_description);
        creator = (TextView) findViewById(R.id.route_creator);
        length = (TextView) findViewById(R.id.route_length);
        duration = (TextView) findViewById(R.id.route_duration);
        difficulty = (TextView) findViewById(R.id.route_difficulty);
        bends = (TextView) findViewById(R.id.route_bends);
        type = (TextView) findViewById(R.id.route_type);



        name.setText(bundle.getString(SearchFragment.ROUTE_NAME));
        description.setText(bundle.getString(SearchFragment.ROUTE_DESCRIPTION));
        creator.setText(bundle.getString(SearchFragment.ROUTE_CREATOR));
        length.setText(String.format("%.01f", Float.parseFloat(bundle.getString(SearchFragment.ROUTE_LENGTH))/1000) + " km");
        int durationInSeconds = Integer.parseInt(bundle.getString(SearchFragment.ROUTE_DURATION));
        duration.setText(String.valueOf(durationInSeconds/3600) + " h " + String.valueOf((durationInSeconds/60)%60) + " m " + String.valueOf(durationInSeconds%60) + " s");
        difficulty.setText(bundle.getString(SearchFragment.ROUTE_DIFFICULTY));
        bends.setText(bundle.getString(SearchFragment.ROUTE_BENDS));
        type.setText(bundle.getString(SearchFragment.ROUTE_TYPE));

        routeID = bundle.getString(SearchFragment.ROUTE_ID);
        new DownloadGpxTask(this).execute(routeID);
        //new HttpGetTask(this).execute(ALL_REVIEWS_URL + userID + routeID); //prendere lo userID dalle shared pref
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

            Log.v(TAG, "setUpMap()");
            //saving the first and the last ones
            LatLng start = points.get(0);
            LatLng end = points.get(points.size() - 1);

            // Adding the start and end markers
            mMap.addMarker(new MarkerOptions().position(start).title("Start"));
            mMap.addMarker(new MarkerOptions().position(end).title("End"));
            // Zooming on the route
            /*CameraUpdate update = CameraUpdateFactory.newLatLngZoom(points.get(points.size() / 2),
                    MapsFragment.CAMERA_ZOOM_VALUE - 5);
            mMap.animateCamera(update); */

            LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
            for (LatLng point : points) {
                boundsBuilder.include(point);
            }
            LatLngBounds bounds = boundsBuilder.build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10);
            mMap.animateCamera(cameraUpdate);
        }
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
        Log.v(TAG, "setGpx(), gpx size: " + gpx.length() + ", gpx: " + gpx);
        Log.v(TAG, "setGpx()");
    }


    public void setResult(String result) {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.USER, MODE_PRIVATE);
        String nickname = sharedPreferences.getString(LoginActivity.NICKNAME, "");
        JSONObject object;
        try {
            object = new JSONObject(result);
            /* inflate di un eventuale recensione dell'utente (un file xml con la recensione, il bottone modifica e la linea di separazione
                da aggiungere la linear layout con id "reviews_list"
            if (user ha una review) {
                JSONObject userReview = object.getJSONObject("user review");

            */
            String user, rate, message;
            LinearLayout reviewsLayout = (LinearLayout) findViewById(R.id.reviews_list);
            boolean found = false;

            JSONArray array = object.getJSONArray("reviews");
            for (int i = 0; i < array.length(); i++) {
                JSONObject review = array.getJSONObject(i);
                user = review.getString("user");
                rate = review.getString("rate");
                message = review.getString("message");

                 //aggiungi review al linear layout "reviews_list"
                 //inflate del file "review_item.xml" con i campi popolati
                if (! user.equals(nickname)) {
                    View view = getLayoutInflater().inflate(R.layout.review_item, reviewsLayout, false);
                    ((TextView) view.findViewById(R.id.user)).setText(user);
                    ((RatingBar) view.findViewById(R.id.rating_bar)).setRating(Float.parseFloat(rate));
                    ((TextView) view.findViewById(R.id.review)).setText(message);
                    reviewsLayout.addView(view);
                } else {
                    //inflate della recensione dell'utente
                    found = true;
                    LinearLayout userReviewLayout = (LinearLayout) findViewById(R.id.user_review_layout);
                    View view = getLayoutInflater().inflate(R.layout.user_review_item, userReviewLayout, false);
                    ((TextView) view.findViewById(R.id.user)).setText(user);
                    ((RatingBar) view.findViewById(R.id.rating_bar)).setRating(Float.parseFloat(rate));
                    ((TextView) view.findViewById(R.id.review)).setText(message);
                    userReviewLayout.addView(view);
                }
            }
            if (!found) {
                LinearLayout userReviewLayout = (LinearLayout) findViewById(R.id.user_review_layout);
                View view = getLayoutInflater().inflate(R.layout.new_review_button, userReviewLayout, false);
                view.setOnClickListener(this);
                userReviewLayout.addView(view);
            }
        } catch (JSONException e) {}
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_review_button:
                Intent intent = new Intent(this, ReviewCreationActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(SearchFragment.ROUTE_ID, routeID);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.pick_this_route_button:
                Intent data = new Intent();
                Bundle b = new Bundle();
                b.putInt(SearchFragment.ROUTE_ID, Integer.parseInt(routeID));
                b.putString(SearchFragment.ROUTE_NAME, name.getText().toString());
                b.putString(SearchFragment.ROUTE_LENGTH, length.getText().toString());
                b.putString(SearchFragment.ROUTE_DURATION, duration.getText().toString());
                b.putString(SearchFragment.ROUTE_CREATOR, creator.getText().toString());
                b.putString(SearchFragment.ROUTE_TYPE, type.getText().toString());
                data.putExtras(b);
                setResult(RESULT_OK, data);
                Log.v(TAG, "pick this route button pressed");
                finish();
                break;
            case R.id.edit_review:
                //modifica la review esistente
                break;
        }
    }
}
