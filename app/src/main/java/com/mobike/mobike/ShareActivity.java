package com.mobike.mobike;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class ShareActivity extends ActionBarActivity {

    private String shareURL;
    private TextView urlTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        urlTextView = (TextView) findViewById(R.id.url_textview);

        Intent intent = getIntent();
        shareURL = "http://mobike.ddns.net/WAPP/itineraries/" + intent.getStringExtra(SummaryActivity.ROUTE_ID);

        urlTextView.setText(getString(R.string.share_textview) + " " + shareURL);
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

    // return to MapsActivity to record a new route
    public void recordNewRoute(View view) {
        finish();
    }

    public void buttonPressed(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shared_message) + " " + shareURL);
        startActivity(Intent.createChooser(intent, "Share"));
    }
}
