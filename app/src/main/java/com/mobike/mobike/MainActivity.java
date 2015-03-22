package com.mobike.mobike;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobike.mobike.network.HttpGetTask;
import com.mobike.mobike.tabs.SlidingTabLayout;

import org.json.JSONArray;
import org.json.JSONException;


public class MainActivity extends ActionBarActivity implements HttpGetTask.HttpGet {

    private ViewPager mPager;
    private SlidingTabLayout mTabs;

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

        //new HttpGetTask(this).execute(EventsFragment.downloadInvitedEventsURL);

        // resetting the database
        GPSDatabase db = new GPSDatabase(this);
        db.deleteTable();

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


    public void setResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            if (jsonArray.length() > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Invitations")
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
