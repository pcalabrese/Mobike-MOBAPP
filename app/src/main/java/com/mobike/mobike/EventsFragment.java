package com.mobike.mobike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobike.mobike.utils.Event;
import com.mobike.mobike.utils.Route;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventsFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String EVENT_NAME = "com.mobike.mobike.EventsFragment.event_name";
    public static final String EVENT_DATE = "com.mobike.mobike.EventsFragment.event_date";
    public static final String EVENT_CREATOR = "com.mobike.mobike.EventsFragment.event_creator";
    public static final String EVENT_DESCRIPTION = "com.mobike.mobike.EventsFragment.event_description";
    public static final String EVENT_GPX = "com.mobike.mobike.EventsFragment.event_gpx";

    public static final String ROUTE_NAME = "com.mobike.mobike.EventsFragment.route_name";
    public static final String ROUTE_DESCRIPTION = "com.mobike.mobike.EventsFragment.route_description";
    public static final String ROUTE_CREATOR = "com.mobike.mobike.EventsFragment.route_creator";
    public static final String ROUTE_LENGTH = "com.mobike.mobike.EventsFragment.route_length";
    public static final String ROUTE_DURATION = "com.mobike.mobike.EventsFragment.route_duration";
    public static final String ROUTE_GPX = "com.mobike.mobike.EventsFragment.route_gpx";
    public static final String EVENT_INVITED = "com.mobike.mobike.EventsFragment.event_invited";
    public static final String downloadURL = "mobike.ddns/SRV/qualcosa";

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

        new DownloadEventsTask().execute(downloadURL);
    }

    @Override
    public void onStart() {
        super.onStart();

        Spinner spinner = (Spinner) getView().findViewById(R.id.event_types);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.event_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Initialization of the events list
        ArrayList<Event> list = new ArrayList<>();
        Route route = new Route("Roma - Cassino", "descrizione", "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx");
        String description = "Descrizione dell'evento, qui ci saranno scritti i dettagli inseriti dal creatore dell'evento al momento della creazione. Ci saranno più righe";
        String invited = "Andrea Donati\nMarco Esposito\nPaolo Calabrese\nBruno Vispi";
        list.add(new Event("Roma - Cassino", "Sunday 11/11/2014 8:30", "Sent by Andrea Donati", description, route, invited));
        list.add(new Event("Roma - Sora", "Saturday 23/02/2015 9:00", "Sent by Marco Esposito", description, route, invited));
        list.add(new Event("Roma - Viterbo", "Friday 16/05/2015 8:00", "Sent by Paolo Calabrese", description, route, invited));
        list.add(new Event("Roma - Perugia", "Saturday 16/05/2015 8:00", "Sent by Bruno Vispi", description, route, invited));
        list.add(new Event("Roma - Terni", "Friday 16/05/2015 8:00", "Sent by Paolo Calabrese", description, route, invited));
        list.add(new Event("Roma - Bolsena", "Friday 16/05/2015 8:00", "Sent by Paolo Calabrese", description, route, invited));
        list.add(new Event("Roma - Frosinone", "Friday 16/05/2015 8:00", "Sent by Paolo Calabrese", description, route, invited));

        ListAdapter listAdapter = new ListAdapter(getActivity(), R.layout.event_list_row, list);
        ListView listView = (ListView) getView().findViewById(R.id.list_view);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Event event = (Event) adapterView.getAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), EventActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EVENT_NAME, event.getName());
                bundle.putString(EVENT_DATE, event.getDate());
                bundle.putString(EVENT_CREATOR, event.getCreator());
                bundle.putString(EVENT_DESCRIPTION, event.getDescription());
                Route r = event.getRoute();
                bundle.putString(ROUTE_NAME, r.getName());
                bundle.putString(ROUTE_DESCRIPTION, r.getDescription());
                bundle.putString(ROUTE_CREATOR, r.getCreator());
                bundle.putString(ROUTE_LENGTH, r.getLength());
                bundle.putString(ROUTE_DURATION, r.getDuration());
                bundle.putString(ROUTE_GPX, r.getGpx());
                bundle.putString(EVENT_INVITED, event.getInvited());
                intent.putExtras(bundle);
                Log.v(TAG, r.getName() + r.getDescription() + r.getCreator() +
                        r.getLength() + r.getDuration() + r.getGpx());
                startActivity(intent);
            }
        });
    }

    public void showEventsList(JSONObject json){
        Spinner spinner = (Spinner) getView().findViewById(R.id.event_types);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.event_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Initialization of the events list
        ArrayList<Event> list = new ArrayList<>();
        // TODO popolare la lista con i dati presi dal json

        ListAdapter listAdapter = new ListAdapter(getActivity(), R.layout.event_list_row, list);
        ListView listView = (ListView) getView().findViewById(R.id.list_view);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Event event = (Event) adapterView.getAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), EventActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EVENT_NAME, event.getName());
                bundle.putString(EVENT_DATE, event.getDate());
                bundle.putString(EVENT_CREATOR, event.getCreator());
                bundle.putString(EVENT_DESCRIPTION, event.getDescription());
                Route r = event.getRoute();
                bundle.putString(ROUTE_NAME, r.getName());
                bundle.putString(ROUTE_DESCRIPTION, r.getDescription());
                bundle.putString(ROUTE_CREATOR, r.getCreator());
                bundle.putString(ROUTE_LENGTH, r.getLength());
                bundle.putString(ROUTE_DURATION, r.getDuration());
                bundle.putString(ROUTE_GPX, r.getGpx());
                bundle.putString(EVENT_INVITED, event.getInvited());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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
        switch (position) {
            case 0: // first item in the spinner
                break;
            case 1: // second item
                break;
            case 2: // third item
                break;
        }
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

    private class DownloadEventsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return HTTPGetEvents(downloadURL);
        }

        @Override
        protected void onPostExecute(String result) {
            try{
                JSONObject json = new JSONObject(result);
                showEventsList(json);
            }catch(JSONException e)
            { e.printStackTrace();}
        }

        private String HTTPGetEvents(String url){
            InputStream inputStream = null;
            String result = "";
            try {

                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

                // receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else{
                    return null;}

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
            return result;
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

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

            TextView tt = (TextView) v.findViewById(R.id.event_name);
            TextView tt1 = (TextView) v.findViewById(R.id.event_date);
            TextView tt3 = (TextView) v.findViewById(R.id.event_creator);

            if (tt != null) {
                tt.setText(p.getName());
            }
            if (tt1 != null) {

                tt1.setText(p.getDate());
            }
            if (tt3 != null) {

                tt3.setText(p.getCreator());
            }
        }

        return v;

    }
}