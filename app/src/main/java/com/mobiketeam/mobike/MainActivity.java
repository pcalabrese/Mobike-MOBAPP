package com.mobiketeam.mobike;

import android.app.AlertDialog;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mobiketeam.mobike.network.HttpGetTask;
import com.mobiketeam.mobike.tabs.SlidingTabLayout;
import com.mobiketeam.mobike.utils.Crypter;
import com.mobiketeam.mobike.utils.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This is the main activity, where you can reach all the features. It contains three tabs with rute recording, route list and event list.
 */
public class MainActivity extends ActionBarActivity implements HttpGetTask.HttpGet, NavigationView.OnNavigationItemSelectedListener {

    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private AdView mAdView;
    private FragmentManager fragmentManager;
    private Fragment routesFragment, mapsFragment;
    private Fragment[] fragments = new Fragment[2];
    private String[] fragmentTAGS = new String[2];

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* VISUALIZZO ACTION BAR CON LOGO */
        /*
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        */
        //getSupportActionBar().hide();

        //checkPendingEvents(EventsFragment.downloadInvitedEventsURL);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fragmentManager = getSupportFragmentManager();
        mapsFragment = new MapsFragment();
        routesFragment = new SearchFragment();
        fragments[0] = mapsFragment;
        fragments[1] = routesFragment;
        fragmentTAGS[0] = "mapsFragment";
        fragmentTAGS[1] = "routesFragment";
        onNavigationDrawerItemSelected(0);
        //fragmentManager.beginTransaction()
          //      .replace(R.id.fragment_container, mapsFragment).commit();

        // resetting the database
        if (savedInstanceState == null) {
            GPSDatabase db = new GPSDatabase(this);
            db.deleteTableLoc();
            db.deleteTablePOI();
            Log.v(TAG, "deleting tables in db");
        }

        /*
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        mPager.setOffscreenPageLimit(3);
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mTabs.setSelectedIndicatorColors(getResources().getColor(R.color.colorAccent));
        mTabs.setViewPager(mPager);
        */

        // Initialize the banner (adView)
        mAdView = (AdView) findViewById(R.id.adView);
        //mAdView.setAdListener(new MyAdListener(this));
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
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
            case R.id.action_logout:
                setResult(LoginActivity.DISCONNECT, null);
                finish();
                break;
            case R.id.action_bug_report:
                sendBugReport();
                break;
            case R.id.action_send_email:
                sendEmail();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendBugReport() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "mobiketeam@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "BUG REPORT");
        startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.bug_report) + "..."));
    }

    public void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "mobiketeam@gmail.com", null));
        startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.send_email)));
    }

    private String generateToken() {
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
        } catch (UnsupportedEncodingException uee) {
        }

        return user;
    }

    private void checkPendingEvents(String url) {
        String user = generateToken();
        new HttpGetTask(this).execute(url + user);
        Log.v(TAG, "downloadEvents url: " + url + user);
    }

    public void setResult(String result) {
        try {
            Crypter crypter = new Crypter();
            JSONArray jsonArray = new JSONArray(crypter.decrypt(new JSONObject(result).getString("events")));
            TextView titleView = ((TextView) getLayoutInflater().inflate(R.layout.list_dialog_title, null, false));
            titleView.setText(getResources().getString(R.string.pending_inivtations_dialog_title));
            if (pendingInvitations(jsonArray)) {
                new AlertDialog.Builder(this)
                        .setCustomTitle(titleView)
                        .setMessage(getResources().getString(R.string.pending_invitations_dialog_message))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        } catch (JSONException e) {
        }
    }

    private boolean pendingInvitations(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++)
            if (array.getJSONObject(i).getInt("userState") == Event.INVITED)
                return true;
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int position = 0;
        int id = item.getItemId();
        if (id == R.id.nav_camara) {
            position = 0;
        } else if (id == R.id.nav_gallery) {
            position = 1;
        }

        onNavigationDrawerItemSelected(position);
        return true;
    }

    private void onNavigationDrawerItemSelected(int position) {
        // Add the fragments only once
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragmentManager.findFragmentByTag(fragmentTAGS[position]) == null) {
            fragmentTransaction.add(R.id.fragment_container,fragments[position],fragmentTAGS[position]);
        }

        // Hiding & Showing fragments
        for(int catx = 0; catx < fragments.length; catx++) {
            if(catx == position) {
                fragmentTransaction.show(fragments[catx]);
            }
            else {
                // Check if the fragment is added and then hide it
                if (fragmentManager.findFragmentByTag(fragmentTAGS[catx]) != null) {
                    fragmentTransaction.hide(fragments[catx]);
                }
            }
        }
        fragmentTransaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /*
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;
        String tag = null;

        if (id == R.id.nav_camara) {
            fragment = mapsFragment;
            tag = "mapsFragment";
        } else if (id == R.id.nav_gallery) {
            fragment = routesFragment;
            tag = "routesFragment";
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        if (fragment != null && fragmentManager.findFragmentByTag(tag) != null) {
            //FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment, tag).show(fragment);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }*/

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                case 0:
                    return new MapsFragment();
                case 1:
                    return new SearchFragment();
                case 2:
                    return new EventsFragment();
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
