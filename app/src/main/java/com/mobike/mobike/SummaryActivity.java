package com.mobike.mobike;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mobike.mobike.network.UploadRouteTask;
import com.mobike.mobike.utils.CustomMapFragment;
import com.mobike.mobike.utils.POI;
import com.mobike.mobike.utils.Route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

/**
 * This activity displays the route that has just been recorded and gives
 * the user the choice to save or delete it.
 */

public class SummaryActivity extends ActionBarActivity {

    public static final int SHARE_REQUEST = 1;
    public static final int REVIEW_REQUEST = 3;
    private static final String TAG = "SummaryActivity";
    private static final String UploadURL = "http://mobike.ddns.net/SRV/routes/create";
    private static final String DEFAULT_ACCOUNT_NAME = "no account";
    public static final String ROUTE_ID = "com.mobike.mobike.ROUTE_ID";
    public static final String ROUTE_NAME = "com.mobike.mobike.route_name";
    public static final String ROUTE_LOCATION = "com.mobike.mobike.route_location";

    private  EditText routeNameText, routeDescriptionText, routeDifficulty, routeBends, routeStartLocation, routeEndLocation;
    private Spinner typeSpinner;
    private TextView length, duration;
    private long durationInSeconds;
    private String routeName, routeDescription, email, routeID, difficulty, bends, type, startLocation, endLocation;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Polyline route; // the recorded route
    private List<LatLng> points; // the points of the route

    /**
     * This method is called when the activity is created; it checks if the map is set up
     * and then adds all the recorded location to the route, for it to be displayed on the map.
     * @param savedInstanceState the saved data of the activity instance
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        setUpMapIfNeeded();

        loadRoutePOIsFromDB();

        route = mMap.addPolyline(new PolylineOptions().width(6).color(Color.BLUE));
        GPSDatabase db = new GPSDatabase(this);
        db.open();
        points = db.getAllLocations();
        db.close();
        route.setPoints(points);

        // request of start and end location
        new GeocoderTask(this, 0).execute(points.get(0));
        new GeocoderTask(this, 1).execute(points.get(points.size() - 1));

        routeNameText = (EditText) findViewById(R.id.route_name_text);
        routeDescriptionText = (EditText) findViewById(R.id.route_description_text);
        routeDifficulty = (EditText) findViewById(R.id.route_difficulty);
        routeBends = (EditText) findViewById(R.id.route_bends);
        typeSpinner = (Spinner) findViewById(R.id.route_type);
        routeStartLocation = (EditText) findViewById(R.id.start_location);
        routeEndLocation = (EditText) findViewById(R.id.end_location);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.route_type_selection, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        typeSpinner.setAdapter(adapter);
        typeSpinner.setPrompt("Route type...");
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    switch (position) {
                        case 0:
                            type = Route.MOUNTAIN;
                            break;
                        case 1:
                            type = Route.HILL;
                            break;
                        case 2:
                            type = Route.COAST;
                            break;
                        case 3:
                            type = Route.PLAIN;
                            break;
                        case 4:
                            type = Route.MIXED;
                            break;
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });
        type = Route.MOUNTAIN;

        //set length and duration text views
        GPSDatabase db2 = new GPSDatabase(this);
        length = (TextView) findViewById(R.id.length_text_view);
        length.setText(String.format("%.02f", db2.getTotalLength()/1000) + " km");
        duration = (TextView) findViewById(R.id.duration_text_view);
        durationInSeconds = db2.getTotalDuration();
        duration.setText(String.valueOf(durationInSeconds/3600) + " h " + String.valueOf((durationInSeconds/60)%60) + " m " + String.valueOf(durationInSeconds%60) + " s");
        Log.v(TAG, "length: " + db2.getTotalLength() + " -- duration: " + db2.getTotalDuration() + " -- url:" + db2.getEncodedPolylineURL());
        db2.close();
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
            mMap = ((CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.summary_map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // set listener to add the possibility to scroll the map inside the scroll view
                ((CustomMapFragment) getSupportFragmentManager().findFragmentById(R.id.summary_map)).setListener(new CustomMapFragment.OnTouchListener() {
                    @Override
                    public void onTouch() {
                        ((ScrollView) findViewById(R.id.scroll_view)).requestDisallowInterceptTouchEvent(true);
                    }
                });
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        setUpMap();
                    }
                });
                //setUpMap();
            }
        }
    }

    /**
     * This method gets all the recorded location from the database,
     * initializes the route and centers the map at the middle of the route.
     *
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        GPSDatabase db = new GPSDatabase(this);
        db.open();
        // Taking all the points of the route
        points = db.getAllLocations();
        if (points.size() > 0) {
            //saving the first and the last ones
            LatLng start = points.get(0);
            LatLng end = points.get(points.size() - 1);

            // Adding the start and end markers
            mMap.addMarker(new MarkerOptions().position(start).title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.addMarker(new MarkerOptions().position(end).title("End"));

            // Zooming on the route
            if (points.size() > 1) {
                LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
                for (LatLng point : points) {
                    boundsBuilder.include(point);
                }
                Log.v(TAG, "numero punti: " + points.size());
                LatLngBounds bounds = boundsBuilder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                mMap.animateCamera(cameraUpdate);
            } else {
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(points.get(points.size() / 2),
                            MapsFragment.CAMERA_ZOOM_VALUE - 5);
                mMap.animateCamera(update);
            }
        }

        db.close();
    }

    private void loadRoutePOIsFromDB() {
        //load and display all POIs in the map
        GPSDatabase db = new GPSDatabase(this);
        JSONArray array = db.getPOITableInJSON();
        JSONObject poi;
        double latitude, longitude;
        String title, category;
        String[] types = getResources().getStringArray(R.array.poi_categories);
        try {
            for (int i = 0; i < array.length(); i++) {
                poi = array.getJSONObject(i);
                title = poi.getString("title");
                category = types[poi.getInt("category")];
                latitude = poi.getDouble("latitude");
                longitude = poi.getDouble("longitude");

                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                        .title(title).snippet(category).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            }
        } catch (JSONException e) {}

        Log.v(TAG, "loadRuotePOIsFromDB()");
    }

    /**
     * This method is called when the user choose to delete the recorded route.
     * @param view the view
     */
    public void deleteRoute(View view) {
        // go to the mapsActivity and delete the route on the map (points = newArrayList<LatLng>;
                                                                // route,setPoints(points);
        finish();
    }

    public void saveRoute(View view) {
        if (routeNameText.getText().toString().length() == 0) {
            Toast.makeText(this, "Insert a route name", Toast.LENGTH_SHORT).show();
            return;
        } else if (routeDifficulty.getText().toString().length() == 0) {
            Toast.makeText(this, "Insert difficulty", Toast.LENGTH_SHORT).show();
            return;
        } else if(routeBends.getText().toString().length() == 0) {
            Toast.makeText(this, "Insert bends", Toast.LENGTH_SHORT).show();
            return;
        }else if ((!(routeDifficulty.getText().toString().length() == 0) && (Integer.parseInt(routeDifficulty.getText().toString()) < 1 || Integer.parseInt(routeDifficulty.getText().toString()) > 10))
                || (!(routeBends.getText().toString().length() == 0) && (Integer.parseInt(routeBends.getText().toString()) < 1 || Integer.parseInt(routeBends.getText().toString()) > 10))) {
            Toast.makeText(this, "Difficulty and Bends must be between 1 and 10", Toast.LENGTH_SHORT).show();
            return;
        } else {
            routeName = routeNameText.getText().toString();
            routeDescription = routeDescriptionText.getText().toString();
            difficulty = routeDifficulty.getText().toString();
            bends = routeBends.getText().toString();
            //type = typeSpinner.getText().toString();
            startLocation = routeStartLocation.getText().toString();
            endLocation = routeEndLocation.getText().toString();

            SharedPreferences sharedPref = getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
            email = sharedPref.getString(LoginActivity.EMAIL, DEFAULT_ACCOUNT_NAME);
            Log.v(TAG, "email = " + email);
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new UploadRouteTask(this, email, routeName, routeDescription, difficulty, bends, type, startLocation, endLocation).execute();
                Toast.makeText(this, getResources().getString(R.string.uploading_toast), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setStartLocation(String text) {
        routeStartLocation.setText(text);
    }

    public void setEndLocation(String text) {
        routeEndLocation.setText(text);
    }

    // Method called when ShareActivity finishes, returns to MapsActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult()");
        if (requestCode == SHARE_REQUEST)
            finish();
        else if (requestCode == REVIEW_REQUEST) {
            Intent intent = new Intent(this, ShareActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(ROUTE_ID, routeID);
            bundle.putString(ROUTE_NAME, routeName);
            bundle.putString(ROUTE_LOCATION, endLocation);
            intent.putExtras(bundle);
            startActivityForResult(intent, SummaryActivity.SHARE_REQUEST);
        }
    }

    public void setRoute(String routeID) {
        this.routeID = routeID;
    }
}


/**
 * This async task get the location from <Latitude, Longitude> couple
 */
class GeocoderTask extends AsyncTask<LatLng, Void, String> {
    private Context context;
    private int request;
    private static final String TAG = "GeocoderTask";

    public GeocoderTask(Context context, int request) {
        this.context = context;
        this.request = request;
    }

    @Override
    protected String doInBackground(LatLng... locations) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String result = "";
        try {
            List<Address> list = geocoder.getFromLocation(locations[0].latitude, locations[0].longitude, 1);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                // sending back first address line and locality
                result = address.getLocality();
                Log.v(TAG, "location found: " + result);
            }
        } catch (IOException e) {
            Log.e(TAG, "Impossible to connect to Geocoder", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (request == 0)
            ((SummaryActivity) context).setStartLocation(result);
        else
            ((SummaryActivity) context).setEndLocation(result);
    }
}
