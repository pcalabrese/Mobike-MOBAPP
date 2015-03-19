package com.mobike.mobike;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobike.mobike.network.HttpGetTask;
import com.mobike.mobike.utils.CircleButton;
import com.mobike.mobike.utils.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, HttpGetTask.HttpGet {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String EVENT_ID = "com.mobike.mobike.EevntsFragment.event_id";
    public static final String EVENT_STATE = "com.mobike.mobike.EevntsFragment.event_state";
    public static final String EVENT_NAME = "com.mobike.mobike.EventsFragment.event_name";
    public static final String EVENT_DATE = "com.mobike.mobike.EventsFragment.event_date";
    public static final String EVENT_CREATOR = "com.mobike.mobike.EventsFragment.event_creator";
    public static final String EVENT_DESCRIPTION = "com.mobike.mobike.EventsFragment.event_description";
    public static final String EVENT_INVITED = "com.mobike.mobike.EventsFragment.event_invited";
    public static final String ROUTE_ID = "com.mobike.mobike.EventsFragment.route_id";
    public static final String EVENT_START_LOCATION = "com.mobike.mobike.EventsFragment.event_start_location";
    public static final String EVENT_CREATION_DATE = "com.mobike.mobike.EventsFragment.event_creation_date";

    public static final String downloadAllEventsURL = "http://mobike.ddns.net/SRV/events/retrieveall";
    public static final String downloadUserEventsURL = "http://mobike.ddns.net/SRV/events/retrieve";
    public static final String downloadInvitedEventsURL = "";

    private Boolean initialSpinner = true, firstTime;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final String TAG = "EventsFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventsFragment newInstance(String param1, String param2) {
        EventsFragment fragment = new EventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public EventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        downloadEvents(downloadAllEventsURL);

        firstTime = true;

        Log.v(TAG, "onCreate()");
    }

    @Override
    public void onStart() {
        super.onStart();

    /*    Spinner spinner = (Spinner) getView().findViewById(R.id.event_types);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.event_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this); */

        ListView listView = (ListView) getView().findViewById(R.id.list_view);

        if (firstTime) {
            View header = View.inflate(getActivity(), R.layout.spinner, null);
            final Spinner spinner = (Spinner) header.findViewById(R.id.types);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.event_types, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(this);
            listView.addHeaderView(header);
            firstTime = false;

            mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
            mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    switch (spinner.getSelectedItemPosition()) {
                        case 0:
                            downloadEvents(downloadAllEventsURL);
                            break;
                        case 1:
                            //downloadEvents(downloadUserEventsURL);
                            break;
                        case 2:
                            //downloadEvents(downloadAcceptedEventsURL);
                            break;
                    }
                }
            });
        }

        initialSpinner = true;

        ((ImageButton) getView().findViewById(R.id.create_event)).setOnClickListener(this);

        Log.v(TAG, "onStart()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
/*        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // method called when an item in the spinner is selected
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        Log.v(TAG, "onItemSelected()");
        if (initialSpinner) {
            initialSpinner = false;
            return;
        }
        switch (position) {
            case 0:
                downloadEvents(downloadAllEventsURL);
                break;
            case 1: //downloadEvents(downloadUserEventsURL);
                break;
            case 2: //downloadEvents(downloadAcceptedEventsURL);
                break;
        }
    }

    private void downloadEvents(String url) {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        new HttpGetTask(this).execute(url);
        Log.v(TAG, "downloadEvents: " + url);
    }

    // method called when no items in the spinner are selected
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public void setResult(String result) {
        // Initialization of the events list
        ArrayList<Event> list = new ArrayList<>();

        JSONObject jsonEvent;
        JSONArray json;
        String name, id, date, creator, routeID, startLocation, creationDate;
        int acceptedSize, invitedSize, refusedSize, state;

        try {
            json = new JSONArray(result);
            for (int i = 0; i < json.length(); i++) {
                jsonEvent = json.getJSONObject(i);
                name = jsonEvent.getString("name");
                name = name.substring(0,1).toUpperCase() + name.substring(1);
                id = jsonEvent.getInt("eventID") + "";
                creator = jsonEvent.getJSONObject("owner").getString("nickname");
                date = jsonEvent.getString("startDate");
                startLocation = jsonEvent.getString("startLocation");
                routeID = jsonEvent.getInt("routeId") + "";
                acceptedSize = jsonEvent.getInt("acceptedSize");
                invitedSize = jsonEvent.getInt("invitedSize");
                refusedSize = jsonEvent.getInt("refusedSize");
                state = jsonEvent.getInt("userState");

                list.add(new Event(name, id, date, creator, routeID, startLocation, acceptedSize, invitedSize, refusedSize, state));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ListView listView = (ListView) getView().findViewById(R.id.list_view);

        ListAdapter listAdapter = new ListAdapter(getActivity(), R.layout.event_list_row, list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Event event = (Event) adapterView.getAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), EventActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EVENT_ID, event.getId());
                bundle.putInt(EVENT_STATE, event.getState());
                bundle.putString(ROUTE_ID, event.getRouteID());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_event:
                Intent intent = new Intent(getActivity(), EventCreationActivity.class);
                startActivity(intent);
                break;
        }
    }
}




class ListAdapter extends ArrayAdapter<Event> {

    public ListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListAdapter(Context context, int resource, List<Event> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.event_list_row, null);

        }

        Event p = getItem(position);

        if (p != null) {

            TextView name = (TextView) v.findViewById(R.id.event_name);
            TextView date = (TextView) v.findViewById(R.id.event_date);
            TextView time = (TextView) v.findViewById(R.id.event_time);
            TextView creator = (TextView) v.findViewById(R.id.event_creator);
            TextView location = (TextView) v.findViewById(R.id.event_location);
            ImageView state = (ImageView) v.findViewById(R.id.event_state);
            TextView accepted = (TextView) v.findViewById(R.id.event_accepted);
            TextView invited = (TextView) v.findViewById(R.id.event_invited);
            TextView refused = (TextView) v.findViewById(R.id.event_refused);


            if (name != null) {
                name.setText(p.getName());
            }
            if (date != null) {
                Date mDate = null, mDateCreation = null;
                SimpleDateFormat s1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    mDate = s1.parse(p.getStartDate());
                } catch (ParseException e ) { }
                String dateString = new SimpleDateFormat("EEEE, d/MM/yyyy").format(mDate);
                date.setText(dateString.substring(0,1).toUpperCase() + dateString.substring(1));
            }
            if (time != null) {
                String[] work = p.getStartDate().split(" ")[1].split(":");
                String timeString = work[0] + ":" + work[1];
                time.setText(timeString);
            }
            if (creator != null) {
                creator.setText("Created by " + p.getCreator());
            }
            if (location != null)
                location.setText(p.getStartLocation());
            if (state != null)
                state.setImageResource(p.getColorState());
            if (accepted != null)
                accepted.setText(p.getAcceptedSize());
            if (invited != null)
                invited.setText(p.getInvitedSize());
            if (refused != null)
                refused.setText(p.getRefusedSize());
        }

        return v;

    }
}