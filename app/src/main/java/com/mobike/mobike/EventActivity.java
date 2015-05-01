package com.mobike.mobike;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.mobike.mobike.network.HttpGetTask;
import com.mobike.mobike.network.ParticipationTask;
import com.mobike.mobike.utils.Crypter;
import com.mobike.mobike.utils.CustomMapFragment;
import com.mobike.mobike.utils.Event;
import com.mobike.mobike.utils.Route;
import com.mobike.mobike.utils.SquareImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This activity displays all details of events, with a link to the associated route and all participants
 */
public class EventActivity extends ActionBarActivity implements HttpGetTask.HttpGet, View.OnClickListener {
    public static final String EVENT_URL = "http://mobike.ddns.net/SRV/events/retrieve/";
    public static final String ACCEPT = "accept";
    public static final String DECLINE = "decline";

    private TextView mName, mDate, mCreator, mDescription, mInvited, mStartLocation, mCreationDate;
    private Button mDisplayRouteButton, mAcceptedButton, mInvitedButton, mDeclinedButton;
    private ImageView mThumbnail;
    private Route route;
    private String eventID, routeID;
    private int state;
    private ArrayList<String> usersAccepted, usersInvited, usersDeclined;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Polyline routePoly; // the route
    private ArrayList<LatLng> points; // the points of the route

    private final String TAG = "EventActivity";

    /**
     * Activity lifecycle method, initializes the UI and start the download of the event
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        //setUpMapIfNeeded();

        getSupportActionBar().hide();

        /* get event and route details from the bundle, route details will be used to visualize the route
         in a new RouteActivity (at the pressure of a button)
         */
        Bundle bundle = getIntent().getExtras();
        mName = (TextView) findViewById(R.id.event_name);
        mDate = (TextView) findViewById(R.id.event_date);
        mCreator = (TextView) findViewById(R.id.event_creator);
        mDescription = (TextView) findViewById(R.id.event_description);
        mInvited = (TextView) findViewById(R.id.event_invited);
        mStartLocation = (TextView) findViewById(R.id.start_location);
        mCreationDate = (TextView) findViewById(R.id.creation_date);
        mThumbnail = (SquareImageView) findViewById(R.id.event_map);
        mDisplayRouteButton = ((Button) findViewById(R.id.display_route_button));
        mAcceptedButton = ((Button) findViewById(R.id.accepted_users_button));
        mInvitedButton = ((Button) findViewById(R.id.pending_users_button));
        mDeclinedButton = ((Button) findViewById(R.id.declined_users_button));

        state = bundle.getInt(EventsFragment.EVENT_STATE);
        eventID = bundle.getString(EventsFragment.EVENT_ID);

        new HttpGetTask(this).execute(EVENT_URL + eventID);

        //inflate dei giusto button per accettare o declinare l'invito

        if (state == Event.INVITED) {
            inflateButtons();
        } else if (state == Event.ACCEPTED) {
            inflateTextviews();
        } else if (state == Event.REFUSED) {
            inflateTextviews();
        }

        mDisplayRouteButton.setOnClickListener(this);
        mAcceptedButton.setOnClickListener(this);
        mInvitedButton.setOnClickListener(this);
        mDeclinedButton.setOnClickListener(this);

        mDisplayRouteButton.setText(getResources().getString(R.string.view_route_details));
    }

    /**
     * Displays the downloaded event, filling all text fields
     * @param result String result of the HTTP GET
     */
    @Override
    public void setResult(String result) {
        String name = "", date = "", creator = "", description = "", startLocation = "", creationDate = "", thumbnailURL = "";
        int acceptedSize = 0, invitedSize = 0, declinedSize = 0;
        JSONObject jsonEvent;
        Crypter crypter = new Crypter();

        if (result.length() == 0) {
            Toast.makeText(this, "An error occurred loading this event", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            jsonEvent = new JSONObject(crypter.decrypt(new JSONObject(result).getString("event")));
            Log.v(TAG, "json evento: " + jsonEvent.toString());
            name = jsonEvent.getString("name");
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            date = jsonEvent.getString("startdate");
            creator = jsonEvent.getJSONObject("owner").getString("nickname");
            description = jsonEvent.getString("description");
            startLocation = jsonEvent.getString("startlocation");
            creationDate = jsonEvent.getString("creationdate");
            thumbnailURL = jsonEvent.getJSONObject("route").isNull("imgUrl") ? "https://maps.googleapis.com/maps/api/staticmap?size=500x500&path=weight:3%7Ccolor:0xff0000ff%7Cenc:aty~Fo|uiAkMnT_G`OYvIrDzKvG`OrH`NjE`OpDjS~BhRXd_@_Cpd@qHnc@ii@zw@cf@jnAqLdPw_@n|BrHfo@sHpd@kAtcAvGrqA{f@~eB" : jsonEvent.getJSONObject("route").getString("imgUrl") + "&size=500x500";
            routeID = jsonEvent.getJSONObject("route").getInt("id") + "";
            usersAccepted = getList(jsonEvent.getString("usersAccepted"));
            usersInvited = getList(jsonEvent.getString("usersInvited"));
            usersDeclined = getList(jsonEvent.getString("usersRefused"));
            acceptedSize = jsonEvent.getInt("acceptedSize");
            invitedSize = jsonEvent.getInt("invitedSize");
            declinedSize = jsonEvent.getInt("refusedSize");
        } catch (JSONException e) {
        }


        Date eventDate = null, mDateCreation = null;
        SimpleDateFormat s1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            eventDate = s1.parse(date);
            mDateCreation = s1.parse(creationDate);
        } catch (ParseException e) {
        }

        mName.setText(name);
        mDate.setText(new SimpleDateFormat("EEEE, d MMMM yyyy\nkk:mm").format(eventDate));
        mCreator.setText(creator);
        mDescription.setText(description);
        mAcceptedButton.setText(String.valueOf(acceptedSize) + "\n" + getResources().getString(R.string.users_accepted));
        mInvitedButton.setText(String.valueOf(invitedSize) + "\n" + getResources().getString(R.string.users_invited));
        mDeclinedButton.setText(String.valueOf(declinedSize) + "\n" + getResources().getString(R.string.users_declined));
        mStartLocation.setText(startLocation);
        if (mDateCreation != null)
            mCreationDate.setText(new SimpleDateFormat("EEEE, d MMMM yyyy").format(mDateCreation));
        Picasso.with(this).load(thumbnailURL).into(mThumbnail);
    }

    /**
     * Creates an ArrayList of Strings from a String representing a JSONArray
     * @param s
     * @return
     * @throws JSONException
     */
    private ArrayList<String> getList(String s) throws JSONException{
        ArrayList<String> result = new ArrayList<>();
        JSONArray array = new JSONArray(s);
        if (array.length() == 0) return result;

        for (int i = 0; i < array.length(); i++)
            result.add(array.getJSONObject(i).getString("nickname"));

        return result;
    }

    /**
     * Terminates current activity at the pressure of top left back button
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts RouteActivity to display route's details
     */
    public void displayRoute() {
        Intent intent = new Intent(this, RouteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(SearchFragment.ROUTE_ID, routeID);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * Handles click events on buttons
     * @param view
     */
    @Override
    public void onClick(View view) {
        LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        switch (view.getId()) {
            case R.id.accept_event_button:
                // post per accettare l'invito
                new ParticipationTask(this, eventID).execute(ACCEPT);

                removeButtons();
                TextView accepted = (TextView) getLayoutInflater().inflate(R.layout.accepted_textview, buttonLayout, false);
                buttonLayout.addView(accepted);
                break;

            case R.id.decline_event_button:
                // post per declinare l'invito
                new ParticipationTask(this, eventID).execute(DECLINE);

                removeButtons();
                TextView declined = (TextView) getLayoutInflater().inflate(R.layout.declined_textview, buttonLayout, false);
                buttonLayout.addView(declined);
                break;

            case R.id.accept_declined_event_button:
                new ParticipationTask(this, eventID).execute(ACCEPT);
                removeTextviews();
                state = Event.ACCEPTED;
                inflateTextviews();
                recreateActivity(Event.ACCEPTED);
                break;

            case R.id.decline_accepted_event_button:
                new ParticipationTask(this, eventID).execute(DECLINE);
                removeTextviews();
                state = Event.REFUSED;
                inflateTextviews();
                recreateActivity(Event.REFUSED);
                break;

            case R.id.accepted_users_button:
                createListDialog("Accepted", usersAccepted);
                break;

            case R.id.pending_users_button:
                createListDialog("Invited", usersInvited);
                break;

            case R.id.declined_users_button:
                createListDialog("Declined", usersDeclined);
                break;

            case R.id.display_route_button:
                displayRoute();
        }
    }

    /**
     * Create a ListDialog with the list of users
     * @param title
     * @param elements
     */
    private void createListDialog(String title, ArrayList<String> elements) {
        TextView titleView = ((TextView) getLayoutInflater().inflate(R.layout.list_dialog_title, null, false));
        titleView.setText(title);
        ShowListDialog dialog = new ShowListDialog();
        dialog.setArguments(title, elements, titleView);
        dialog.show(getSupportFragmentManager(), "usersList");
    }

    /**
     * Removes participation buttons at the top of the UI
     */
    private void removeButtons() {
        ((Button) findViewById(R.id.accept_event_button)).setVisibility(View.GONE);
        ((Button) findViewById(R.id.decline_event_button)).setVisibility(View.GONE);
    }

    /**
     * Removes participation textView and button at the top of UI
     */
    private void removeTextviews() {
        if (state == Event.ACCEPTED) {
            ((TextView) findViewById(R.id.accepted_event_textview)).setVisibility(View.GONE);
            ((ImageButton) findViewById(R.id.decline_accepted_event_button)).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.declined_event_textview)).setVisibility(View.GONE);
            ((ImageButton) findViewById(R.id.accept_declined_event_button)).setVisibility(View.GONE);
        }
    }

    /**
     * Inflates buttons at the top of UI to accept or decline the invitation
     */
    private void inflateButtons() {
        LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        Button accept = (Button) getLayoutInflater().inflate(R.layout.accept_button, buttonLayout, false);
        Button decline = (Button) getLayoutInflater().inflate(R.layout.decline_button, buttonLayout, false);
        buttonLayout.addView(accept);
        buttonLayout.addView(decline);
        accept.setOnClickListener(this);
        decline.setOnClickListener(this);
    }

    /**
     * Inflates the correct combination of TextView and Button depending on event state
     */
    private void inflateTextviews() {
        LinearLayout buttonLayout = (LinearLayout) findViewById(R.id.button_layout);
        if (state == Event.ACCEPTED) {
            TextView accepted = (TextView) getLayoutInflater().inflate(R.layout.accepted_textview, buttonLayout, false);
            ImageButton decline = (ImageButton) getLayoutInflater().inflate(R.layout.decline_accepted_button, buttonLayout, false);
            buttonLayout.addView(accepted);
            buttonLayout.addView(decline);
            decline.setOnClickListener(this);
        } else {
            TextView declined = (TextView) getLayoutInflater().inflate(R.layout.declined_textview, buttonLayout, false);
            ImageButton accept = (ImageButton) getLayoutInflater().inflate(R.layout.accept_declined_button, buttonLayout, false);
            buttonLayout.addView(declined);
            buttonLayout.addView(accept);
            accept.setOnClickListener(this);
        }
    }

    /**
     * Recreate the current activity with the current state
     * @param state
     */
    public void recreateActivity(int state) {
        Bundle bundle = new Bundle();
        bundle.putString(EventsFragment.EVENT_ID, eventID);
        bundle.putInt(EventsFragment.EVENT_STATE, state);
        bundle.putString(EventsFragment.ROUTE_ID, routeID);
        Intent intent = getIntent();
        intent.replaceExtras(bundle);
        finish();
        startActivity(intent);
    }


    /**
     * This is a dialog that shows a list of String entries
     */
    public static class ShowListDialog extends android.support.v4.app.DialogFragment {
        private ArrayList<String> elements;
        private String title;
        private View titleView;

        /**
         * Method to pass arguments to the Dialog
         * @param title
         * @param e
         * @param t
         */
        public void setArguments(String title, ArrayList<String> e, View t) {
            this.title = title;
            elements = e;
            titleView = t;
        }

        /**
         * Method called on the creation of the Dialog, initialize the UI
         * @param savedInstanceState
         * @return
         */
        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCustomTitle(titleView)
                    .setItems(elements.toArray(new String[elements.size()]), null);
            return builder.create();
        }
    }
}