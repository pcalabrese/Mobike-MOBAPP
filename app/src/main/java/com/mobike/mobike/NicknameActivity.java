package com.mobike.mobike;

import android.graphics.PorterDuff;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class NicknameActivity extends ActionBarActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        setFinishOnTouchOutside(false);

        getSupportActionBar().hide();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ((Button) findViewById(R.id.send)).setOnClickListener(this);
        //((EditText) findViewById(R.id.nickname)).getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nickname, menu);
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
            case R.id.send:
                String nickname = ((EditText) findViewById(R.id.nickname)).getText().toString();
                String bike = ((EditText) findViewById(R.id.bike)).getText().toString();

                // upload della nuova review
                if (nickname.length() == 0) {
                    Toast.makeText(this, "Please insert a nickname", Toast.LENGTH_SHORT).show();
                } else if (bike.length() == 0) {
                    Toast.makeText(this, "Please insert a bike model", Toast.LENGTH_SHORT).show();
                } else {
                    //new SignInTask(this, nickname, bike).execute();
                }
                break;
        }
    }
}
