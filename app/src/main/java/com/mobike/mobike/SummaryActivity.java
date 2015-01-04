package com.mobike.mobike;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.List;

/**
 * This activity displays the route that has just been recorded and gives
 * the user the choice to save or delete it.
 */

public class SummaryActivity extends ActionBarActivity {

    private static final int SHARE_REQUEST = 1;
    private static final String TAG = "SummaryActivity";
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
        route.setPoints(points);
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
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.summary_map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
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
        //saving the first and the last ones
        LatLng start = points.get(0);
        LatLng end = points.get(points.size() -1);

        // Adding the start and end markers
        mMap.addCircle(new CircleOptions().center(start).fillColor(Color.GREEN).
                strokeColor(Color.BLACK).radius(10));
        mMap.addCircle(new CircleOptions().center(end).fillColor(Color.RED).
                strokeColor(Color.BLACK).radius(10));
        // Adding the empty route to the map
        route = mMap.addPolyline(new PolylineOptions().width(6).color(Color.BLUE));
        // Zooming on the route
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(points.get(points.size()/2),
                MapsActivity.CAMERA_ZOOM_VALUE - 5);
        mMap.animateCamera(update);

        db.close();
    }

    /**
     * This method is called when the user choose to delete the recorded route.
     * @param view the view
     */
    public void deleteRoute(View view) {
        GPSDatabase db = new GPSDatabase(this);
        db.deleteTable();
        // go to the mapsActivity and delete the route on the map (points = newArrayList<LatLng>;
                                                                // route,setPoints(points);
        finish();
    }

    public void saveRoute(View view) {
        // Parte l'upload del percorso

        // Avvia l'activity per la condivisione del tracciato sui social networks
        Intent intent = new Intent(this, ShareActivity.class);
        startActivityForResult(intent, SHARE_REQUEST);
    }

    // Method called when ShareActivity finishes, returns to MapsActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult()");
        deleteRoute(null);
    }
}
