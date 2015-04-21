package com.mobike.mobike;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobike.mobike.network.HttpGetTask;
import com.mobike.mobike.tabs.SlidingTabLayout;
import com.mobike.mobike.utils.Crypter;
import com.mobike.mobike.utils.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This is the main activity, where you can reach all the features. It contains three tabs with rute recording, route list and event list.
 */
public class MainActivity extends ActionBarActivity implements HttpGetTask.HttpGet {

    private ViewPager mPager;
    private SlidingTabLayout mTabs;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* VISUALIZZO ACTION BAR CON LOGO */
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        //getSupportActionBar().hide();

        downloadEvents(EventsFragment.downloadInvitedEventsURL);

        // resetting the database
        if (savedInstanceState == null) {
            GPSDatabase db = new GPSDatabase(this);
            db.deleteTableLoc();
            db.deleteTablePOI();
            Log.v(TAG, "deleting tables in db");
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        mPager.setOffscreenPageLimit(3);
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTabs.setSelectedIndicatorColors(getResources().getColor(R.color.colorAccent));
        mTabs.setViewPager(mPager);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_account:
                Intent intent = new Intent(this, AccountDetailsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void downloadEvents(String url) {
        String user = "";
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
        int id = sharedPreferences.getInt(LoginActivity.ID, 0);
        String nickname = sharedPreferences.getString(LoginActivity.NICKNAME, "");
        Crypter crypter = new Crypter();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", id);
            jsonObject.put("nickname", nickname);
            user = URLEncoder.encode(crypter.encrypt(jsonObject.toString()), "utf-8");
            Log.v(TAG, "json per gli inviti pendenti: " + jsonObject.toString());
        } catch (JSONException e) {
            Log.v(TAG, "json exception in downloadEvents()");
        }
        catch (UnsupportedEncodingException uee) {}

        new HttpGetTask(this).execute(url + user);
        Log.v(TAG, "downloadEvents url: " + url + user);
    }

    public void setResult(String result) {
        try {
            Crypter crypter = new Crypter();
            JSONArray jsonArray = new JSONArray(crypter.decrypt(new JSONObject(result).getString("events")));
            TextView titleView = ((TextView) getLayoutInflater().inflate(R.layout.list_dialog_title, null, false));
            titleView.setText("Pending Invitations");
            if (pendingInvitations(jsonArray)) {
                new AlertDialog.Builder(this)
                        .setCustomTitle(titleView)
                        .setMessage("Hey! You have pending invitations to events, check them!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        } catch (JSONException e) {}
    }

    private boolean pendingInvitations(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++)
            if (array.getJSONObject(i).getInt("userState") == Event.INVITED)
                return true;
        return false;
    }


    class MyPagerAdapter extends FragmentPagerAdapter {

        private static final int FRAGMENT_NUMBER = 3;
        private String[] titles;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            titles = getResources().getStringArray(R.array.fragment_titles);
        }

        // questo metodo prende in input la posizione e restituisce il relativo fragment, devo creare un'istanza dei fragment
        // a seconda della posizione
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new MapsFragment();
                case 1: return new SearchFragment();
                case 2: return new EventsFragment();
            }
            return null;
        }

        // restituisce il titolo della tab in funzione della posizione
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return FRAGMENT_NUMBER;
        }
    }
}
