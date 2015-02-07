package com.mobike.mobike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobike.mobike.utils.Event;

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

    public static final String EVENT_NAME = "com.mobike.mobike.event_name";
    public static final String EVENT_DATE = "com.mobike.mobike.event_date";
    public static final String EVENT_CREATOR = "com.mobike.mobike.event_creator";
    public static final String EVENT_DESCRIPTION = "com.mobike.mobike.event_description";

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
        String description = "Descrizione dell'evento, qui ci saranno scritti i dettagli inseriti dal creatore dell'evento al momento della creazione. Ci saranno più righe";
        list.add(new Event("Roma - Cassino", "Sunday 11/11/2014 8:30", "Sent by Andrea Donati", description));
        list.add(new Event("Roma - Sora", "Saturday 23/02/2015 9:00", "Sent by Marco Esposito", description));
        list.add(new Event("Roma - Viterbo", "Friday 16/05/2015 8:00", "Sent by Paolo Calabrese", description));
        list.add(new Event("Roma - Perugia", "Saturday 16/05/2015 8:00", "Sent by Bruno Vispi", description));
        list.add(new Event("Roma - Terni", "Friday 16/05/2015 8:00", "Sent by Paolo Calabrese", description));
        list.add(new Event("Roma - Bolsena", "Friday 16/05/2015 8:00", "Sent by Paolo Calabrese", description));
        list.add(new Event("Roma - Frosinone", "Friday 16/05/2015 8:00", "Sent by Paolo Calabrese", description));

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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

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