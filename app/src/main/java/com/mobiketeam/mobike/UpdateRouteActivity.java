package com.mobiketeam.mobike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mobiketeam.mobike.utils.Route;

public class UpdateRouteActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mName, mDescription, mDifficulty, mBends;
    private Spinner mTypeSpinner;
    private Button mDone;

    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_route);

        mName = (EditText) findViewById(R.id.route_name_text);
        mDescription = (EditText) findViewById(R.id.route_description_text);
        mDifficulty = (EditText) findViewById(R.id.route_difficulty_text);
        mBends = (EditText) findViewById(R.id.route_bends_text);
        mTypeSpinner = (Spinner) findViewById(R.id.route_type);
        mDone = (Button) findViewById(R.id.done_button);

        Intent intent = getIntent();
        String difficulty = intent.getStringExtra(SearchFragment.ROUTE_DIFFICULTY);
        String bends = intent.getStringExtra(SearchFragment.ROUTE_BENDS);

        mName.setText(intent.getStringExtra(SearchFragment.ROUTE_NAME));
        mDescription.setText(intent.getStringExtra(SearchFragment.ROUTE_DESCRIPTION));
        mDifficulty.setText(difficulty.substring(0, difficulty.indexOf('/')));
        mBends.setText(bends.substring(0, bends.indexOf('/')));
        mDone.setOnClickListener(this);

        setSpinner();
    }

    private void setSpinner() {
        type = getIntent().getStringExtra(SearchFragment.ROUTE_TYPE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.route_type_selection, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(adapter);
        mTypeSpinner.setPrompt(type);
        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        type = Route.MOUNTAIN;
                        break;
                    case 1:
                        type = Route.HILL;
                        break;
                    case 2:
                        type = Route.COAST;
                        break;
                    case 3:
                        type = Route.PLAIN;
                        break;
                    case 4:
                        type = Route.MIXED;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.done_button:
                sendUpdatedRoute();
                break;
        }
    }

    private void sendUpdatedRoute() {
        if (mName.getText().toString().length() == 0) {
            Toast.makeText(this, "Insert a route name", Toast.LENGTH_SHORT).show();
        } else if (mDifficulty.getText().toString().length() == 0) {
            Toast.makeText(this, "Insert difficulty", Toast.LENGTH_SHORT).show();
        } else if(mBends.getText().toString().length() == 0) {
            Toast.makeText(this, "Insert bends", Toast.LENGTH_SHORT).show();
        }else if ((!(mDifficulty.getText().toString().length() == 0) &&
                (Integer.parseInt(mDifficulty.getText().toString()) < 1 || Integer.parseInt(mDifficulty.getText().toString()) > 10)) ||
                (!(mBends.getText().toString().length() == 0) &&
                (Integer.parseInt(mBends.getText().toString()) < 1 || Integer.parseInt(mBends.getText().toString()) > 10))) {
            Toast.makeText(this, "Difficulty and Bends must be between 1 and 10", Toast.LENGTH_SHORT).show();
        } else {

        }
    }
}
