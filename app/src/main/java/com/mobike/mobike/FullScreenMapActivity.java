package com.mobike.mobike;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * This is a fullscreen map activity to display better the route's path
 */
public class FullScreenMapActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String TAG = "FullScreenMapActivity";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Polyline routePoly; // the polyline of the route

    private List<LatLng> points; // the points of the route

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_map);

        Log.v(TAG, "onCreate()");

        // get points from route_gpx ,set up the map and finally add the polyline of the route
        GPSDatabase db = new GPSDatabase(this);
        db.open();
        //String gpx = getIntent().getExtras().getString(RouteActivity.GPX);
        String gpx = RouteActivity.currentGpx;
        points = db.gpxToMapPoints(gpx);
        db.close();

        setUpMapIfNeeded();

        ((ImageButton) findViewById(R.id.fullscreen_exit_button)).setOnClickListener(this);
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
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                //setUpMap();

                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        setUpMap();
                        routePoly = mMap.addPolyline(new PolylineOptions().width(6).color(Color.BLUE));
                        routePoly.setPoints(points);
                    }
                });
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fullscreen_exit_button:
                finish();
                break;
        }
    }
}
