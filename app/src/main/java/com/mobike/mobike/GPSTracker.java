package com.mobike.mobike;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

/**
 * This class implements the Service for route recording.
 * It is not a "typical" service, in fact it is never started by serviceStart.
 * It is started when the GPSTracker object is created in MapsActivity and records the
 * position with the method onChangedLocation()
 */
public class GPSTracker extends Service implements LocationListener {

    private final Context context;

    private NewLocationsListener mListener; // the MapsActivity object

    //flag for GPS status
    boolean isGPSEnabled = false;

    private static final long MIN_DISTANCE_BW_UPDATES_METERS = 5;

    private static final long MIN_TIME_BW_UPDATES_MS = 1000 * 5; // Minimum time between updates in milliseconds

    protected LocationManager locationManager;  // The object that manages the communication with
                                                // the system's location service.

    /**
     * This constructor creates the object and starts the route recording.
     * If the GPS is not enabled, it shows an alert.
     * @param context
     * @param listener The MapsActivity
     */
    public GPSTracker(Context context, NewLocationsListener listener) {
        this.context = context;
        this.mListener = listener;
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double altitude = location.getAltitude();
            updateDatabase(latitude, longitude, altitude, true); //true <--> inserting first row
            startUsingGPS();
        }
        else showSettingsAlert();
    }

    /**
     *
     */
    public GPSTracker()
    {
        super();
        context = this;

    }

    /**
     * This method stops the location updates and thus the route recording.
     */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * This method starts the location updates and thus the route recording.
     */
    public void startUsingGPS() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES_MS, MIN_DISTANCE_BW_UPDATES_METERS, this);
    }

    /**
     * This method shows an alert inviting the user to activate the GPS in the settings menu.
     */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("GPS in settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
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
     * This method receives a location updates from the LocationManager object.
     * When this happens, it adds a new row to the database table with an id,
     * the latitude, the longitude, the altitude, the current time.
     * @param location the newly updated location
     */
    @Override
    public void onLocationChanged(Location location) {
        mListener.onNewLocation(location); // this method call adds the new location to the map

        double lat = location.getLatitude();
        double lng = location.getLongitude();
        double alt = location.getAltitude(); // this is 0.0 if the altitude is not available.
        updateDatabase(lat, lng, alt, false);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     *  This method adds a new row to the Database.
     * @param lat The latitude
     * @param lng The longitude
     * @param alt The altitude
     * @param firstRow this boolean is true if we are inserting the first location of the route.
     */
    public void updateDatabase(double lat, double lng, double alt, boolean firstRow)
    {
        GPSDatabase myDatabase = new GPSDatabase(context);
        myDatabase.open();
        if(firstRow){
            myDatabase.insertFirstRow(lat, lng, alt);
        }
        else {myDatabase.insertRow(lat, lng, alt);}
        myDatabase.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // NOT TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
