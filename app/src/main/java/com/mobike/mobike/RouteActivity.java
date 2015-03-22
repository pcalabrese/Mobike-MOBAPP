package com.mobike.mobike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.mobike.mobike.network.DeleteReviewTask;
import com.mobike.mobike.network.DownloadGpxTask;
import com.mobike.mobike.network.HttpGetTask;
import com.mobike.mobike.utils.Crypter;
import com.mobike.mobike.utils.CustomMapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class RouteActivity extends ActionBarActivity implements View.OnClickListener, HttpGetTask.HttpGet {
    public static final String ROUTE_URL = "http://mobike.ddns.net/SRV/routes/retrieve/";

    public static final String USER_RATE = "com.mobike.mobike.RouteActivity.user_rate";
    public static final String USER_MESSAGE = "com.mobike.mobike.RouteActivity.user_message";
    public static final int EDIT_REVIEW_REQUEST = 1;
    public static final String GPX = "com.mobike.mobike.gpx";

    public static String currentGpx;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Polyline routePoly; // the polyline of the route
    private ArrayList<LatLng> points; // the points of the route

    private TextView mName, mDescription, mCreator, mLength, mDuration, mDifficulty, mBends, mType, mRating, mRatingNumber, mStartLocation, mEndLocation;
    private RatingBar mRatingBar;
    private String routeID, userRate, userMessage, gpx;
    private boolean pickingRoute;

    private static final String TAG = "RouteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        setUpMapIfNeeded();

        getSupportActionBar().hide();

        pickingRoute = getIntent().getExtras().getInt(SearchFragment.REQUEST_CODE) == EventCreationActivity.ROUTE_REQUEST;

        // inflate del bottone "PICK THIS ROUTE" in un linear layout vuoto, che chiama setResult(RESULT_OK, intent)
        if (pickingRoute) {
            RelativeLayout buttonLayout = (RelativeLayout) findViewById(R.id.pick_button_layout);
            buttonLayout.addView(getLayoutInflater().inflate(R.layout.pick_this_route_button, buttonLayout, false));
            ((ImageButton) findViewById(R.id.pick_this_route_button)).setOnClickListener(this);
        }

        // get data from bundle and displays in textViews
        Bundle bundle = getIntent().getExtras();
        mName = (TextView) findViewById(R.id.route_name);
        mDescription = (TextView) findViewById(R.id.route_description);
        mCreator = (TextView) findViewById(R.id.route_creator);
        mLength = (TextView) findViewById(R.id.route_length);
        mDuration = (TextView) findViewById(R.id.route_duration);
        mDifficulty = (TextView) findViewById(R.id.route_difficulty);
        mBends = (TextView) findViewById(R.id.route_bends);
        mType = (TextView) findViewById(R.id.route_type);
        mStartLocation = (TextView) findViewById(R.id.route_start_location);
        mEndLocation = (TextView) findViewById(R.id.route_end_location);
        mRatingBar = (RatingBar) findViewById(R.id.rating_bar);
        mRating = (TextView) findViewById(R.id.rating);
        mRatingNumber = (TextView) findViewById(R.id.rating_number);

        routeID = bundle.getString(SearchFragment.ROUTE_ID);

        ((ImageButton) findViewById(R.id.fullscreen_button)).setOnClickListener(this);

        //new DownloadGpxTask(this).execute(routeID);
        //new HttpGetTask(this).execute(ALL_REVIEWS_URL + userID + routeID); //prendere lo userID dalle shared pref
        new HttpGetTask(this).execute(ROUTE_URL + routeID);
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
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            mMap.animateCamera(cameraUpdate);
        }
    }

    public void setGpx(String gpx) {
        this.gpx = gpx;

        // get points from route_gpx ,set up the map and finally add the polyline of the route
        GPSDatabase db = new GPSDatabase(this);
        db.open();
        points = db.gpxToMapPoints(gpx);
        db.close();

        setUpMap();

        routePoly = mMap.addPolyline(new PolylineOptions().width(6).color(Color.BLUE));
        routePoly.setPoints(points);

        Log.v(TAG, "points size = " + points.size());
        Log.v(TAG, "setGpx(), gpx size: " + gpx.length() + ", gpx: " + gpx);
        Log.v(TAG, "setGpx()");
    }


    public void setResult(String result) {
        String name="", description="", creator="", length="", duration="", difficulty="", bends="", type="", gpx="", startLocation="", endLocation="";
        int ratingNumber = 0;
        double rating = 0;
        JSONArray reviews = null;
        JSONObject jsonRoute;
        Crypter crypter = new Crypter();

        Log.v(TAG, "result: " + result);

        try {
            jsonRoute = new JSONObject(crypter.decrypt(new JSONObject(result).getString("route")));
            Log.v(TAG, "jsonRoute: " + jsonRoute.toString());
            name = jsonRoute.getString("name");
            name = name.substring(0,1).toUpperCase() + name.substring(1);
            description = jsonRoute.getString("description");
            creator = jsonRoute.getJSONObject("owner").getString("nickname");
            length = jsonRoute.getDouble("length") + "";
            duration = jsonRoute.getInt("duration") + "";
            difficulty = jsonRoute.getInt("difficulty") + "";
            bends = jsonRoute.getInt("bends") + "";
            type = jsonRoute.getString("type");
            gpx = jsonRoute.getString("gpxString");
            reviews = jsonRoute.getJSONArray("reviewList");
            rating = jsonRoute.isNull("rating")? 0d : jsonRoute.getDouble("rating");
            ratingNumber = jsonRoute.isNull("ratingnumber")? 0 : jsonRoute.getInt("ratingnumber");
            startLocation = jsonRoute.getString("startlocation");
            endLocation = jsonRoute.getString("endlocation");
        } catch (JSONException e) {}


        mName.setText(name);
        mDescription.setText(description);
        mCreator.setText(creator);
        mLength.setText(String.format("%.01f", Float.parseFloat(length)/1000) + " km");
        int durationInSeconds = Integer.parseInt(duration);
        mDuration.setText(String.valueOf(durationInSeconds/3600) + " h " + String.valueOf((durationInSeconds/60)%60) + " m " + String.valueOf(durationInSeconds%60) + " s");
        mDifficulty.setText(difficulty);
        mBends.setText(bends);
        mType.setText(type);
        mStartLocation.setText(startLocation);
        mEndLocation.setText(endLocation);
        setReviews(reviews);
        setGpx(gpx);
        mRating.setText(String.format("%.01f", rating));
        mRatingNumber.setText(ratingNumber + " reviews");
        mRatingBar.setRating(Float.parseFloat(String.valueOf(rating)));

        Log.v(TAG, "setResult() completed");
    }



    public void setReviews(JSONArray reviews) {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.USER, MODE_PRIVATE);
        String nickname = sharedPreferences.getString(LoginActivity.NICKNAME, "");
        try {
            /* inflate di un eventuale recensione dell'utente (un file xml con la recensione, il bottone modifica e la linea di separazione
                da aggiungere la linear layout con id "reviews_list"
            if (user ha una review) {
                JSONObject userReview = object.getJSONObject("user review");

            */
            String user, rate, message;
            LinearLayout reviewsLayout = (LinearLayout) findViewById(R.id.reviews_list);
            boolean found = false;

            for (int i = 0; i < reviews.length(); i++) {
                JSONObject review = reviews.getJSONObject(i);
                Log.v(TAG, "set Reviews, review: " + review.toString());
                user = review.getJSONObject("owner").getString("nickname");
                rate = review.getInt("rate") + "";
                message = review.getString("message");

                Log.v(TAG, "set Reviews, campi: " + user + ", " + rate + ", " + message);

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
                    userRate = rate;
                    userMessage = message;
                    LinearLayout userReviewLayout = (LinearLayout) findViewById(R.id.user_review_layout);
                    View view = getLayoutInflater().inflate(R.layout.user_review_item, userReviewLayout, false);
                    ((TextView) view.findViewById(R.id.user)).setText(user);
                    ((RatingBar) view.findViewById(R.id.rating_bar)).setRating(Float.parseFloat(rate));
                    ((TextView) view.findViewById(R.id.review)).setText(message);
                    if (!pickingRoute) {
                        ((Button) view.findViewById(R.id.edit_review)).setOnClickListener(this);
                        ((Button) view.findViewById(R.id.delete_review)).setOnClickListener(this);
                    } else {
                        ((Button) view.findViewById(R.id.edit_review)).setVisibility(View.GONE);
                        ((Button) view.findViewById(R.id.delete_review)).setVisibility(View.GONE);
                    }
                    userReviewLayout.addView(view);
                }
            }
            if (!found) {
                LinearLayout userReviewLayout = (LinearLayout) findViewById(R.id.user_review_layout);
                View view = getLayoutInflater().inflate(R.layout.new_review_button, userReviewLayout, false);
                view.findViewById(R.id.new_review_button).setOnClickListener(this);
                userReviewLayout.addView(view);
            }
        } catch (JSONException e) {
            Log.v(TAG, "jsonException");
        }
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
                b.putString(SearchFragment.ROUTE_NAME, mName.getText().toString());
                b.putString(SearchFragment.ROUTE_LENGTH, mLength.getText().toString());
                b.putString(SearchFragment.ROUTE_DURATION, mDuration.getText().toString());
                b.putString(SearchFragment.ROUTE_CREATOR, mCreator.getText().toString());
                b.putString(SearchFragment.ROUTE_TYPE, mType.getText().toString());
                data.putExtras(b);
                setResult(RESULT_OK, data);
                Log.v(TAG, "pick this route button pressed");
                finish();
                break;

            case R.id.edit_review:
                //modifica la review esistente

                Intent ii = new Intent(this, ReviewCreationActivity.class);
                Bundle bb = new Bundle();
                bb.putString(SearchFragment.ROUTE_ID, routeID);
                bb.putString(USER_RATE, userRate);
                bb.putString(USER_MESSAGE, userMessage);
                bb.putInt(SearchFragment.REQUEST_CODE, EDIT_REVIEW_REQUEST);
                ii.putExtras(bb);
                startActivityForResult(ii, EDIT_REVIEW_REQUEST);
                break;

            case R.id.delete_review:
                new DeleteReviewTask(this, routeID, userMessage, Float.parseFloat(userRate)).execute();
                break;

            case R.id.fullscreen_button:
                Log.v(TAG, "fullscreen_button pressed");
                Intent intent1 = new Intent(this, FullScreenMapActivity.class);
                /*Bundle bun = new Bundle();
                bun.putString(GPX, gpx);
                intent1.putExtras(bun); */
                RouteActivity.currentGpx = gpx;
                startActivity(intent1);
                break;
        }
    }

    public void recreateActivity() {
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == EDIT_REVIEW_REQUEST) {
            //findViewById(R.id.user_review_item).setVisibility(View.GONE);
            //new HttpGetTask(this).execute(ROUTE_URL + routeID);

            finish();
            startActivityForResult(getIntent(), getIntent().getExtras().getInt(SearchFragment.REQUEST_CODE));
        }
    }
}
