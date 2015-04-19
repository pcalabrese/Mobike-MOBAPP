package com.mobike.mobike;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * This class implements the location tracking using the new Google APIs (GoogleApiClient,
 * FusedLocationApi).
 * DO NOT REMOVE COMMENTED PARTS.
 */
public class GPSService extends Service implements GoogleApiClient.ConnectionCallbacks,
       GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{


    //a reference to the MapsActivity
    NewLocationListener nLocationListener;
    Context context;

    private static final String TAG = "GPSService";
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    //private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;

    // This boolean makes the activity control the registration.
    private boolean isRegistering;
    // This constants regulate the location update rates.
    private static final long MIN_DISTANCE_BW_UPDATES_METERS = 20;
    private static final long TIME_BW_UPDATES_MS = 10 * 1000;
    private static final long MIN_TIME_BW_UPDATES_MS = 5 * 1000; // Minimum time between updates in milliseconds

    // This variables are used to calculate the distance of the track.
    private Location previousLocation;
    private float totalDistance;

    public GPSService() {
    }

    /**
     *This constructor initializes the field variables and builds the GoogleApiClient object.
     * @param context the context of MapsActivity
     * @param listener the reference to MapsActivity
     */
    public GPSService(Context context, NewLocationListener listener){
        this.context = context;
        nLocationListener = listener;
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        mLocationRequest = createLocationRequest();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * This method is called when the connect() call of the GoogleApiClient succed.
     * It founds a first location and starts the location updates.
     * @param connectionHint no idea
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mCurrentLocation != null){ nLocationListener.onNewLocation(mCurrentLocation,0,0); }
        startLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.v(TAG, "onConnectionFailed()");

        if (mResolvingError) {
            Log.v(TAG,"Resolving Error");
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                //result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (/*IntentSender.SendIntent*/Exception e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG, "onConnectionSuspended()");
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    /**
     * this method makes the app listen to location updates

     // This code is about building the error dialog

     /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        //dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode, this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((LoginActivity)getActivity()).onDialogDismissed();
        }
    }


    /**
     * this method creates a LocationRequest object used to get Location updates
     * by the LocationListener
     * @return the LocationRequest with the default values
     */
    protected LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(TIME_BW_UPDATES_MS);
        mLocationRequest.setFastestInterval(MIN_TIME_BW_UPDATES_MS);
        mLocationRequest.setSmallestDisplacement(MIN_DISTANCE_BW_UPDATES_METERS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    /**
     * this method makes the app listen to location updates
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * This method makes the app stop listening to location updates.
     * It will be useful when new types of activity will be added to the app.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * This method is called when either "Start" or "Resume" is selected by the user.
     * It makes the registration of the track start (or restart).
     */
    public void register(){
        isRegistering = true;
    }

    public void stopRegistering(){
        isRegistering = false;
    }

    public boolean isServiceRegistering(){return isRegistering;}

    public void setDistanceToZero() {totalDistance = 0;}

    /**
     * This method is called whenever a new location is updated.
     * It gives start to the insertion of the new location in the database and to the
     * update of the map in MapsActivity.
     * @param location the last updated location.
     */
    @Override
    public void onLocationChanged(Location location){
        mCurrentLocation = location;
        nLocationListener.updateCamera(location);
        if(isServiceRegistering()) {
            GPSDatabase db = new GPSDatabase(context);
            updateDatabase(location);
            nLocationListener.setRegistered();
            nLocationListener.onNewLocation(mCurrentLocation, db.getTotalLength(), db.getTotalDuration());
        }
    }

    /**
     * This method inserts the new location in the database.
     * @param location the last updated location.
     */
    private void updateDatabase(Location location){
        GPSDatabase myDatabase = new GPSDatabase(context);
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        double alt = location.getAltitude();
        myDatabase.open();

        if(previousLocation != null){
            totalDistance = totalDistance + previousLocation.distanceTo(location);
        }
        myDatabase.insertRowLoc(lat, lng, alt, totalDistance);
        previousLocation = location;
        myDatabase.close();
    }

    /**
     * This method gives the last known location.
     * @return the last known location.
     */
    public Location getLocation(){
        return LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }
    @Override
    public IBinder onBind(Intent intent) {
        // notTODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
