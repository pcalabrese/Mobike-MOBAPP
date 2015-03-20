package com.mobike.mobike;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.android.gms.plus.PlusShare;


public class ShareActivity extends ActionBarActivity implements View.OnClickListener {

    private String shareURL, routeName, location;
    private TextView urlTextView;
    private UiLifecycleHelper uiHelper;

    private static final String TAG = "ShareActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        urlTextView = (TextView) findViewById(R.id.url_textview);

        Bundle bundle = getIntent().getExtras();
        shareURL = "http://mobike.ddns.net/WAPP/itineraries/" + bundle.getString(SummaryActivity.ROUTE_ID);
        routeName = bundle.getString(SummaryActivity.ROUTE_NAME);
        location = bundle.getString(SummaryActivity.ROUTE_LOCATION);

        urlTextView.setText(shareURL);

        ((Button) findViewById(R.id.share_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.facebook_share)).setOnClickListener(this);
        ((Button) findViewById(R.id.google_plus_share)).setOnClickListener(this);
        ((Button) findViewById(R.id.new_route_button)).setOnClickListener(this);

        //facebook share dialog callbacks
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_route_button:
                recordNewRoute(view);
                break;
            case R.id.share_button:
                share(view);
                break;
            case R.id.facebook_share:
                facebookShareDialog();
                break;
            case R.id.google_plus_share:
                googleShareDialog();
                break;
        }
    }

    // return to MapsActivity to record a new route
    public void recordNewRoute(View view) {
        finish();
    }

    public void share(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shared_message) + " " + shareURL);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    public void facebookShareDialog() {
        GPSDatabase db = new GPSDatabase(this);
        String thumbnailURL = db.getEncodedPolylineURL();
        thumbnailURL = thumbnailURL.substring(0, thumbnailURL.length() - 7) + "400x400";
        Log.v(TAG, "thumbnail facebook: " + thumbnailURL);
        FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                .setLink(shareURL)
                .setPicture(thumbnailURL)
                .setName(routeName)
                .setCaption("MoBike")
                .setDescription(getResources().getString(R.string.shared_message))
                .setPlace(location)
                .build();
        uiHelper.trackPendingDialogCall(shareDialog.present());
        db.close();
    }

    private void googleShareDialog() {
        GPSDatabase db = new GPSDatabase(this);
        String thumbnailURL = db.getEncodedPolylineURL();
        thumbnailURL = thumbnailURL.substring(0, thumbnailURL.length() - 7) + "400x400";
        Intent shareIntent = new PlusShare.Builder(this)
                .setType("text/plain")
                .setText(getResources().getString(R.string.shared_message))
                .setContentUrl(Uri.parse(shareURL))
                .getIntent();

        db.close();
        startActivityForResult(shareIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e(TAG, String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i(TAG, "Success!");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
}
