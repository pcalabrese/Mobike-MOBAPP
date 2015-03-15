package com.mobike.mobike;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobike.mobike.network.HttpGetTask;
import com.mobike.mobike.utils.RangeSeekBar;


public class RouteSearchActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String TAG = "RouteSearchActivity";
    public static final String ROUTE_SEARCH_URL = "com.mobike.mobike.route_search_url";

    private ImageView mountain, hill, plain, coast;
    private Boolean[] selected = new Boolean[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_search);

        getSupportActionBar().hide();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mountain = (ImageView) findViewById(R.id.mountain_icon);
        hill = (ImageView) findViewById(R.id.hill_icon);
        plain = (ImageView) findViewById(R.id.plain_icon);
        coast = (ImageView) findViewById(R.id.coast_icon);

        int grigio_trasparente = getResources().getColor(R.color.grigio_trasparente);

        mountain.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
        hill.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
        plain.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
        coast.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);

        mountain.setOnClickListener(this);
        hill.setOnClickListener(this);
        plain.setOnClickListener(this);
        coast.setOnClickListener(this);
        ((Button) findViewById(R.id.send)).setOnClickListener(this);

        for (int i = 0; i < 4; i++) selected[i] = false;

        //range seekbar
        RangeSeekBar<Float> seekBar = new RangeSeekBar<>(100f, 500f, this);
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Float>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Float minValue, Float maxValue) {
                // handle changed range values
                ((TextView) findViewById(R.id.min_length)).setText(String.format("%.01f", minValue) + " km");
                ((TextView) findViewById(R.id.max_length)).setText(String.format("%.01f", maxValue) + " km");
                Log.i(TAG, "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);
            }
        });

        // add RangeSeekBar to pre-defined layout
        ViewGroup layout = (ViewGroup) findViewById(R.id.range_seekbar);
        layout.addView(seekBar);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_search, menu);
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

    public void onClick(View view) {
        int grigio_trasparente = getResources().getColor(R.color.grigio_trasparente);
        Log.v(TAG, "onClick()");
        switch (view.getId()) {
            case R.id.mountain_icon:
                if (!selected[0])
                    mountain.setColorFilter(null);
                else
                    mountain.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
                selected[0] = !selected[0];
                break;

            case R.id.hill_icon:
                if (!selected[1])
                    hill.setColorFilter(null);
                else
                    hill.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
                selected[1] = !selected[1];
                break;

            case R.id.plain_icon:
                if (!selected[2])
                    plain.setColorFilter(null);
                else
                    plain.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
                selected[2] = !selected[2];
                break;

            case R.id.coast_icon:
                if (!selected[3])
                    coast.setColorFilter(null);
                else
                    coast.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
                selected[3] = !selected[3];
                break;

            case R.id.send:
                String start = ((EditText) findViewById(R.id.start)).getText().toString();
                String destination = ((EditText) findViewById(R.id.destination)).getText().toString();

                if (start.length() == 0) {
                    Toast.makeText(this, "Please insert the start location", Toast.LENGTH_SHORT).show();
                } else if (destination.length() == 0) {
                    Toast.makeText(this, "Please insert the destination", Toast.LENGTH_SHORT).show();
                } else {
                    //build url and returns it to SearchFragment
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString(ROUTE_SEARCH_URL, "http://mobike.ddns.net/SRV/routes/retrieveall");
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    Toast.makeText(this, "Route search is currently under development", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
