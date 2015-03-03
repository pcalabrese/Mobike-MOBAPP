package com.mobike.mobike;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.mobike.mobike.network.UploadEventTask;
import com.mobike.mobike.utils.Event;

import java.util.ArrayList;
import java.util.Calendar;


public class EventCreationActivity extends ActionBarActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private String routeID, startDate, startTime;
    private static final String TAG = "EventCreationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_event_creation);

        //downloadUsers();

        ((Button) findViewById(R.id.cancel)).setOnClickListener(this);
        ((Button) findViewById(R.id.pick_date)).setOnClickListener(this);
        ((Button) findViewById(R.id.pick_time)).setOnClickListener(this);
        ((Button) findViewById(R.id.create)).setOnClickListener(this);

        ArrayList<String> data=new ArrayList<String>();
        data.add("Andrea Donati");
        data.add("Marco Esposito");
        data.add("Bruno Vispi");
        data.add("Paolo Calabrese");

        setUsersHints(data);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.create:
                //create and send event
                String name, description, startLocation, invited;
                name = ((TextView) findViewById(R.id.name)).getText().toString();
                description = ((TextView) findViewById(R.id.description)).getText().toString();
                startLocation = ((TextView) findViewById(R.id.start_location)).getText().toString();
                invited = ((MultiAutoCompleteTextView) findViewById(R.id.invite)).getText().toString();
                Log.v(TAG, "invited: " + invited);
                invited = invited.replaceAll(", ", "\n");
                Log.v(TAG, "invited: " + invited);
                SharedPreferences sharedPref = getSharedPreferences(LoginActivity.ID, Context.MODE_PRIVATE);
                String nickname = sharedPref.getString(LoginActivity.NICKNAME, "");

            //    Event event = new Event(name, startDate + " " + startTime, "", nickname, description, routeID, startLocation, creationDate, invited);
            //    new UploadEventTask(this, event).execute();
                finish();
                break;
            case R.id.pick_date:
                //create PickDateDialog and display date in date text view
                DatePickerFragment dateDialog = new DatePickerFragment();
                dateDialog.setListener(this);
                dateDialog.show(getSupportFragmentManager(), "datePicker");
                break;
            case R.id.pick_time:
                TimePickerFragment timeDialog = new TimePickerFragment();
                timeDialog.setListener(this);
                timeDialog.show(getSupportFragmentManager(), "timePicker");
                break;
            case R.id.pick_route:
                //choose a route and save the ID
                break;
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        TextView dateText = (TextView) findViewById(R.id.date);
        dateText.setText(String.format("%02d", day) + "/" + String.format("%02d", month+1) + "/" + String.format("%04d", year));
        startDate = String.format("%02d", day) + "/" + String.format("%02d", month+1) + "/" + String.format("%04d", year);
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView timeText = (TextView) findViewById(R.id.time);
        timeText.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
        startTime = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
    }

/*    public void downloadUsers() {
        new DownloadUsersTask(this).execute();
    }
*/
    public void setUsersHints(ArrayList<String> users) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, users);
        MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.invite);

        MultiAutoCompleteTextView.CommaTokenizer tokenizer=new MultiAutoCompleteTextView.CommaTokenizer();

        textView.setAdapter(adapter);
        textView.setTokenizer(tokenizer);
    }





    public static class DatePickerFragment extends android.support.v4.app.DialogFragment {
        private DatePickerDialog.OnDateSetListener listener;

        public void setListener(DatePickerDialog.OnDateSetListener listener) {
            this.listener = listener;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), listener, year, month, day);
        }
    }


    public static class TimePickerFragment extends android.support.v4.app.DialogFragment {
        private TimePickerDialog.OnTimeSetListener listener;

        public void setListener(TimePickerDialog.OnTimeSetListener listener) {
            this.listener = listener;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), listener, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

    }
}
