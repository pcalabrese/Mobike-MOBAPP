package com.mobike.mobike;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

//LoginActivity non ancora completa, il bottone è inutilizzato

public class LoginActivity extends ActionBarActivity implements View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String TAG = "LoginActivity";
    public static final String ACCOUNT_NAME = "com.mobike.mobike.account_name";
    private static final int MAPS_REQUEST = 1;

    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;
    private boolean mResolvingError = false;
    private String accountName;

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
        // Abbiamo risolto ogni errore di connessione.
        accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Log.v(TAG, "onConnected(), accountName = " + accountName);

        // Salvo l'account name nelle shared preferences
        SharedPreferences sharedPref = getSharedPreferences(ACCOUNT_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACCOUNT_NAME, accountName);
        editor.commit();

        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, MAPS_REQUEST);
    }

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
            finish();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG, "onConnectionSuspended()");
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }




    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
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

}
