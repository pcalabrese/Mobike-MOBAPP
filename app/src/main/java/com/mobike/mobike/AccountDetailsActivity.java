package com.mobike.mobike;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class AccountDetailsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        ImageView imageView = (ImageView) findViewById(R.id.image);

        SharedPreferences preferences = getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
        String imageUrl = preferences.getString(LoginActivity.IMAGEURL, "");
        String name = preferences.getString(LoginActivity.NAME, "") + " " + preferences.getString(LoginActivity.SURNAME, "");
        String nickname = preferences.getString(LoginActivity.NICKNAME, "");
        String email = preferences.getString(LoginActivity.EMAIL, "");
        //String bike = preferences.getString(LoginActivity.BIKE, "");

        imageUrl = imageUrl.split("sz=")[0] + "sz=500";

        Picasso.with(this).load(imageUrl).into(imageView);
        ((TextView) findViewById(R.id.name)).setText(name);
        ((TextView) findViewById(R.id.nickname)).setText(nickname);
        ((TextView) findViewById(R.id.email)).setText(email);
        ((TextView) findViewById(R.id.bike)).setText("bike model (under development)");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account_details, menu);
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
}
