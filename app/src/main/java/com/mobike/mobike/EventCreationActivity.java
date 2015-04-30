package com.mobike.mobike;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mobike.mobike.network.HttpGetTask;
import com.mobike.mobike.network.UploadEventTask;
import com.mobike.mobike.utils.Crypter;
import com.mobike.mobike.utils.Event;
import com.mobike.mobike.utils.Route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * This is the activity for the creation of a new Event, where the user insert all datas, select invited users and pick the associated route
 */
public class EventCreationActivity extends ActionBarActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, HttpGetTask.HttpGet {
    private String startDate, startTime;
    private int routeID = 0;
    private boolean picked = false;
    private HashMap<String, Integer> usersMap;
    private ArrayList<String> userList;
    private boolean[] userState;

    private static final String TAG = "EventCreationActivity";

    public static final String ALL_NICKNAMES_URL = "http://mobike.ddns.net/SRV/users/retrieveall";
    public static final int ROUTE_REQUEST = 1;

    /**
     * Activity lifecycle method, initializes the UI and downloads all users
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_event_creation);

        downloadUsers();

        ((Button) findViewById(R.id.cancel)).setOnClickListener(this);
        ((Button) findViewById(R.id.pick_date)).setOnClickListener(this);
        ((Button) findViewById(R.id.pick_time)).setOnClickListener(this);
        ((Button) findViewById(R.id.create)).setOnClickListener(this);
        ((Button) findViewById(R.id.pick_route)).setOnClickListener(this);
        ((Button) findViewById(R.id.invite_button)).setOnClickListener(this);
        ((TextView) findViewById(R.id.invited_users_textview)).setMovementMethod(new ScrollingMovementMethod());
        ((TextView) findViewById(R.id.invited_users_textview)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ((ScrollView) findViewById(R.id.scroll_view)).requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

    /**
     * Handles click events on buttons and uplaods the event if all data required have been inserted
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel:
                finish();
                break;
            case R.id.create:
                //create and send event
                String name, description, startLocation, date, time, invited;
                name = ((EditText) findViewById(R.id.name)).getText().toString();
                description = ((EditText) findViewById(R.id.description)).getText().toString();
                startLocation = ((EditText) findViewById(R.id.start_location)).getText().toString();
                //invited = ((MultiAutoCompleteTextView) findViewById(R.id.invite)).getText().toString();
                invited = ((TextView) findViewById(R.id.invited_users_textview)).getText().toString();
                date = ((TextView) findViewById(R.id.date)).getText().toString();
                time = ((TextView) findViewById(R.id.time)).getText().toString();

                if (name.length() == 0 || description.length() == 0 || startLocation.length() == 0 || invited.length() == 0
                        || date.length() == 0 || time.length() == 0 || routeID == 0)
                    Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                else
                    createEvent();
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
                if (picked) {
                    ((LinearLayout) findViewById(R.id.route_picked)).removeView(findViewById(R.id.route_list_element));
                    picked = false;
                    Log.v(TAG, "removing route");
                }
                //choose a route and save the ID
                Intent intent = new Intent(this, FragmentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(SearchFragment.REQUEST_CODE, ROUTE_REQUEST);
                intent.putExtras(bundle);
                startActivityForResult(intent, ROUTE_REQUEST);
                Log.v(TAG, "pickRoute button pressed");
                break;

            case R.id.invite_button:
                TextView titleView = ((TextView) getLayoutInflater().inflate(R.layout.list_dialog_title, null, false));
                titleView.setText("Select users");
                new AlertDialog.Builder(this)
                        .setCustomTitle(titleView)
                        .setMultiChoiceItems(userList.toArray(new String[userList.size()]), userState, new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                userState[which] = isChecked;
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // visualizza i nomi scelti
                                Log.v(TAG, "invitati scelti: " + userState.toString());

                                String s = "";
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userState[i]) s += userList.get(i) + "\n";
                                }

                                ((TextView) findViewById(R.id.invited_users_textview)).setText(s);
                            }
                        })
                        .show();
        }
    }

    /**
     * Starts the upload of the new event
     */
    public void createEvent() {
        String name, description, startLocation, invited, creationDate;
        name = ((EditText) findViewById(R.id.name)).getText().toString();
        description = ((EditText) findViewById(R.id.description)).getText().toString();
        startLocation = ((EditText) findViewById(R.id.start_location)).getText().toString();
        //invited = ((MultiAutoCompleteTextView) findViewById(R.id.invite)).getText().toString();
        invited = ((TextView) findViewById(R.id.invited_users_textview)).getText().toString();
        Log.v(TAG, "invited: " + invited);
        //invited = invited.replaceAll(", ", "\n");
        Log.v(TAG, "invited: " + invited);
        SharedPreferences sharedPref = getSharedPreferences(LoginActivity.USER, Context.MODE_PRIVATE);
        String nickname = sharedPref.getString(LoginActivity.NICKNAME, "");
        creationDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());

        Event event = new Event(name, description, nickname, startDate + " " + startTime, startLocation, creationDate, String.valueOf(routeID), invited);
        new UploadEventTask(this, event, usersMap).execute();
        finish();
    }

    /**
     * Method called when the user selects a Date
     * @param view
     * @param year
     * @param month
     * @param day
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        TextView dateText = (TextView) findViewById(R.id.date);
        dateText.setText(String.format("%02d", day) + "/" + String.format("%02d", month+1) + "/" + String.format("%04d", year));
        startDate = String.format("%04d", year) + "/" + String.format("%02d", month+1) + "/" + String.format("%02d", day);
    }

    /**
     * Method called when the user selects a Time
     * @param view
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TextView timeText = (TextView) findViewById(R.id.time);
        timeText.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
        startTime = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + ":00";
    }

    /**
     * Starts the download of all users used for invitation
     */
    public void downloadUsers() {
        String name, surname, email, imgURL;
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.USER, MODE_PRIVATE);
        name = sharedPreferences.getString(LoginActivity.NAME, "");
        surname = sharedPreferences.getString(LoginActivity.SURNAME, "");
        email = sharedPreferences.getString(LoginActivity.EMAIL, "");
        imgURL = sharedPreferences.getString(LoginActivity.IMAGEURL, "");

        try {
            Crypter crypter = new Crypter();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("surname", surname);
                jsonObject.put("email", email);
                jsonObject.put("imgurl", imgURL);
            } catch (JSONException e) {
            }
            Log.v(TAG, "token: " + jsonObject.toString());

            String token = URLEncoder.encode(crypter.encrypt(jsonObject.toString()), "utf-8");
            new HttpGetTask(this).execute(ALL_NICKNAMES_URL + "?token=" + token);
        } catch (UnsupportedEncodingException uee) {
        }
    }

    /**
     * Method called from HttpGetTask to set the result of HTTP GET
     * @param result
     */
    @Override
    public void setResult(String result) {
        userList = new ArrayList<>();
        usersMap = new HashMap<>();
        Crypter crypter = new Crypter();
        Log.v(TAG, "result: " + result);

        try{
            JSONArray array = new JSONArray(crypter.decrypt(new JSONObject(result).getString("users")));
            Log.v(TAG, "array di users: " + array.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject user = array.getJSONObject(i);
                String nickname = user.getString("nickname");
                int id = user.getInt("id");
                userList.add(nickname);
                usersMap.put(nickname, id);
            }
        }catch(JSONException e)
        { e.printStackTrace();}

        //setUsersHints(userList);
        userState = new boolean[userList.size()];
    }

    /*public void setUsersHints(ArrayList<String> users) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, users);
        MultiAutoCompleteTextView textView = (MultiAutoCompleteTextView) findViewById(R.id.invite);

        MultiAutoCompleteTextView.CommaTokenizer tokenizer=new MultiAutoCompleteTextView.CommaTokenizer();

        textView.setAdapter(adapter);
        textView.setTokenizer(tokenizer);
    }*/

    /**
     * Method called when an activity started for result ends, it displays the picked route
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ROUTE_REQUEST) {
            if (resultCode == RESULT_OK) {
                picked = true;
                routeID = intent.getExtras().getInt(SearchFragment.ROUTE_ID);
                Log.v(TAG, "pickRoute, routeID: " + routeID);

                // set the route (magari prendo l'item della lista dei percorsi e lo visualizzo
                LinearLayout routeLayout = (LinearLayout) findViewById(R.id.route_picked);
                routeLayout.addView(getLayoutInflater().inflate(R.layout.route_list_row, routeLayout, false));
                Bundle bundle = intent.getExtras();
                ((TextView) findViewById(R.id.route_name)).setText(bundle.getString(SearchFragment.ROUTE_NAME));
                ((TextView) findViewById(R.id.route_length)).setText(bundle.getString(SearchFragment.ROUTE_LENGTH));
                ((TextView) findViewById(R.id.route_duration)).setText(bundle.getString(SearchFragment.ROUTE_DURATION));
                ((TextView) findViewById(R.id.route_creator)).setText(bundle.getString(SearchFragment.ROUTE_CREATOR));
                ((ImageView) findViewById(R.id.route_type)).setImageResource(Route.getStaticTypeColor(bundle.getString(SearchFragment.ROUTE_TYPE)));
                ((ImageView) findViewById(R.id.route_image)).setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.staticmap));
                ((RatingBar) findViewById(R.id.rating_bar)).setRating(bundle.getFloat(SearchFragment.ROUTE_RATING));
                Log.v(TAG, "route name: " + bundle.getString(SearchFragment.ROUTE_NAME));
            }
        }
    }


    /**
     * Dialog where the user can select the date for the event
     */
    public static class DatePickerFragment extends android.support.v4.app.DialogFragment {
        private DatePickerDialog.OnDateSetListener listener;

        public void setListener(DatePickerDialog.OnDateSetListener listener) {
            this.listener = listener;
        }

        @NonNull
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


    /**
     * Dialog where the user can select the time for the event
     */
    public static class TimePickerFragment extends android.support.v4.app.DialogFragment {
        private TimePickerDialog.OnTimeSetListener listener;

        public void setListener(TimePickerDialog.OnTimeSetListener listener) {
            this.listener = listener;
        }

        @NonNull
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
