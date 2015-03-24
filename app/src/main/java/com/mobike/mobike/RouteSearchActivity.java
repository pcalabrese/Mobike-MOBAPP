package com.mobike.mobike;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import com.mobike.mobike.utils.RangeSeekBar;


public class RouteSearchActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String TAG = "RouteSearchActivity";
    public static final String ROUTE_SEARCH_URL = "com.mobike.mobike.route_search_url";

    private ImageView mountain, hill, plain, coast;
    private Boolean[] selected = new Boolean[4];
    private RangeSeekBar<Float> seekBar;
    private boolean oneSelected = false;

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
        seekBar = new RangeSeekBar<>(100f, 500f, this);
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
                if (!selected[0] && !oneSelected) {
                    mountain.setColorFilter(null);
                    oneSelected = true;
                    selected[0] = !selected[0];
                }
                else if (selected[0]) {
                    mountain.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
                    oneSelected = false;
                    selected[0] = !selected[0];
                }

                break;

            case R.id.hill_icon:
                if (!selected[1] && !oneSelected) {
                    hill.setColorFilter(null);
                    oneSelected = true;
                    selected[1] = !selected[1];
                }
                else if (selected[1]) {
                    hill.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
                    oneSelected = false;
                    selected[1] = !selected[1];
                }

                break;

            case R.id.plain_icon:
                if (!selected[2] && !oneSelected) {
                    plain.setColorFilter(null);
                    oneSelected = true;
                    selected[2] = !selected[2];
                }
                else if (selected[2]) {
                    plain.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
                    oneSelected = false;
                    selected[2] = !selected[2];
                }
                break;

            case R.id.coast_icon:
                if (!selected[3] && !oneSelected) {
                    coast.setColorFilter(null);
                    oneSelected = true;
                    selected[3] = !selected[3];
                }
                else if (selected[3]) {
                    coast.setColorFilter(grigio_trasparente, PorterDuff.Mode.SRC_ATOP);
                    oneSelected = false;
                    selected[3] = !selected[3];
                }
                break;

            case R.id.send:
                String start = ((EditText) findViewById(R.id.start)).getText().toString();
                String destination = ((EditText) findViewById(R.id.destination)).getText().toString();
                String url = "http://mobike.ddns.net/SRV/routes/retrievefiltered?";

                if (start.length() != 0) {
                    Toast.makeText(this, "Please insert the start location", Toast.LENGTH_SHORT).show();
                    url += "startLocation=" + start + "&";
                }
                if (destination.length() != 0) {
                    Toast.makeText(this, "Please insert the destination", Toast.LENGTH_SHORT).show();
                    url += "endLocation=" + destination + "&";
                }
                //build url and returns it to SearchFragment

                url += "minLength=" + String.format("%.0f", seekBar.getSelectedMinValue()*1000);
                url += "&maxLength=" + String.format("%.0f", seekBar.getSelectedMaxValue()*1000);
                if (oneSelected) url += "&type=";

                if (selected[0]) url += "Montuoso";
                if (selected[1]) url += "Collinare";
                if (selected[2]) url += "Pianura";
                if (selected[3]) url += "Costiero";

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(ROUTE_SEARCH_URL, url);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                Toast.makeText(this, "Route search is currently under development", Toast.LENGTH_SHORT).show();
                finish();

                break;
        }
    }
}
