package com.mobike.mobike;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

//LoginActivity non ancora completa, devo risolvere ancora dei problemi

public class LoginActivity extends ActionBarActivity implements View.OnClickListener, ConnectionCallbacks, OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
    private static final String TAG = "LoginActivity";
    private View signInButton;

    private ProgressDialog mConnectionProgressDialog;
    private GoogleApiClient mGoogleApiClient;
    private ConnectionResult mConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);
        Plus.PlusOptions options = new Plus.PlusOptions.Builder().addActivityTypes("http://schemas.google.com/AddActivity", "http://schemas.google.com/ReviewActivity").build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Plus.API, options).addScope(Plus.SCOPE_PLUS_LOGIN).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        // Barra di avanzamento da visualizzare se l'errore di connessione non viene risolto.
        mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage("Signing in...");
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
        if (view.getId() == R.id.sign_in_button) {
            // start the asynchronous sign in flow
            // mSignInClicked = true;
            mGoogleApiClient.connect();
        }
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



    // Inizia la gestione dell'oggetto plusClient

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mConnectionProgressDialog.isShowing()) {
            // L'utente ha già fatto clic sul pulsante di accesso. Inizia a risolvere
            // gli errori di connessione. Attendi fino a onConnected() per eliminare la
            // finestra di dialogo di connessione.
            if (result.hasResolution()) {
                try {
                    result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    mGoogleApiClient.connect();
                }
            }
        }

        // Salva l'intent in modo che sia possibile avviare un'attività quando l'utente fa clic
        // sul pulsante di accesso.
        mConnectionResult = result;
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Abbiamo risolto ogni errore di connessione.
        mConnectionProgressDialog.dismiss();
        Log.v(TAG, "sono connesso!!");

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
            mConnectionResult = null;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }
}
