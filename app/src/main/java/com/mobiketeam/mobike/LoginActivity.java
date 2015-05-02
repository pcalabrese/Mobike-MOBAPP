package com.mobiketeam.mobike;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.mobiketeam.mobike.network.LoginUserTask;

/**
 * This is the first activity, where the user logs in or create a new account
 */

public class LoginActivity extends ActionBarActivity implements View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String TAG = "LoginActivity";
    public static final String USER = "com.mobike.mobike.user";
    public static final String EMAIL = "com.mobike.mobike.email";
    public static final String NAME = "com.mobike.mobike.name";
    public static final String SURNAME = "com.mobike.mobike.surname";
    public static final String NICKNAME = "com.mobike.mobike.nickname";
    public static final String ID = "com.mobike.mobike.id";
    public static final String IMAGEURL = "com.mobike.mobike.imageurl";
    public static final int MAPS_REQUEST = 1;
    public static final int REGISTRATION_REQUEST = 2;
    public static final int DISCONNECT = 99;

    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;
    private boolean mResolvingError = false;
    private String email, name, surname, imageURL;
    private NetworkInfo.State state = NetworkInfo.State.CONNECTING;

    private static final String postURL = "http://mobike.ddns.net/SRV/users/auth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Plus.PlusOptions options = new Plus.PlusOptions.Builder().addActivityTypes("http://schemas.google.com/AddActivity", "http://schemas.google.com/ReviewActivity").build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Plus.API, options).addScope(Plus.SCOPE_PLUS_LOGIN).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

        mResolvingError = savedInstanceState != null && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
    }

    public void onClick(View view) {
     /*   if (view.getId() == R.id.sign_in_button && !mGoogleApiClient.isConnected()) {
            if (mConnectionResult == null) {
                mConnectionProgressDialog.show();
            } else {
                try {
                    mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    // Riprova a connetterti.
                    mConnectionResult = null;
                    mGoogleApiClient.connect();
                }
            }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
    }

    // Inizia la gestione dell'oggetto plusClient

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.v(TAG, "onConnectionFailed()");

        if (mResolvingError) {
            // Already attempting to resolve an error.
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    // method called in case of successful connection
    @Override
    public void onConnected(Bundle bundle) {
        if (state == NetworkInfo.State.CONNECTING) {
            // Abbiamo risolto ogni errore di connessione.
            email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            name = person.getName().getGivenName();
            surname = person.getName().getFamilyName();

            Log.v(TAG, "onConnected(), Name = " + person.getDisplayName());
            Log.v(TAG, "onConnected(), Name = " + person.getName());
            if (person.hasLanguage())
                Log.v(TAG, "onConnected(), Language = " + person.getLanguage());
            if (person.hasGender())
                Log.v(TAG, "onConnected(), Gender = " + person.getGender());
            if (person.hasImage()) {
                imageURL = person.getImage().getUrl();
                Log.v(TAG, "onConnected(), ImageURL = " + person.getImage().getUrl());
            }


            // Salvo l'account name nelle shared preferences
            SharedPreferences sharedPref = getSharedPreferences(USER, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(EMAIL, email);
            editor.putString(NAME, name);
            editor.putString(SURNAME, surname);
            editor.putString(IMAGEURL, imageURL);
            editor.apply();

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                new LoginUserTask(this, name, surname, email, imageURL).execute();
            else
                Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
        }
        else {
            state = NetworkInfo.State.CONNECTING;
            signOutFromGplus();
        }
    }

    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            // clearCookies();
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            //mGoogleApiClient.disconnect();
            /*Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.e(TAG, "User access revoked!");
                            mGoogleApiClient.connect();
                        }

                    }); */
            mGoogleApiClient.clearDefaultAccountAndReconnect();
            //mGoogleApiClient.connect();

            Log.v(TAG, "signOutFromGplus()");
        }
    }

    /**
     * This method close the app if the user navigates back to login activity, in addiction it handles Google login connection problems.
     *
     * @param requestCode request code of the terminated activity
     * @param resultCode  result code of the terminated activity
     * @param intent      intent containing result data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        } else if (requestCode == MAPS_REQUEST) {
            if (resultCode == DISCONNECT) {
                state = NetworkInfo.State.DISCONNECTING;
                Log.v(TAG, "state changed");
                return;
            }
            finish();
        } else if (requestCode == REGISTRATION_REQUEST)
            finish();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG, "onConnectionSuspended()");
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }


    // The rest of this code is all about building the error dialog

    /**
     * Create a dialog to display an error message
     *
     * @param errorCode
     */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /**
     * Called from ErrorDialogFragment when the dialog is dismissed.
     */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /**
     * This class represents the fragment to display an error message.
     */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode, this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((LoginActivity) getActivity()).onDialogDismissed();
        }
    }
}
