package com.mobike.mobike;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
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
import com.mobike.mobike.utils.Route;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener, HttpGetTask.HttpGet, View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "SearchFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ProgressDialog progressDialog;
    private Boolean initialSpinner = true, firstTime;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean pickingRoute;

    public static final int SEARCH_REQUEST = 3;

    public static final String REQUEST_CODE = "com.mobike.mobike.SearchFragment.request_code";
    public static final String ROUTE_ID = "com.mobike.mobike.SearchFragment.route_id";
    public static final String ROUTE_NAME = "com.mobike.mobike.SearchFragment.route_name";
    public static final String ROUTE_DESCRIPTION = "com.mobike.mobike.SearchFragment.route_description";
    public static final String ROUTE_CREATOR = "com.mobike.mobike.SearchFragment.route_creator";
    public static final String ROUTE_LENGTH = "com.mobike.mobike.SearchFragment.route_length";
    public static final String ROUTE_DURATION = "com.mobike.mobike.SearchFragment.route_duration";
    public static final String ROUTE_GPX = "com.mobike.mobike.SearchFragment.route_gpx";
    public static final String ROUTE_DIFFICULTY = "com.mobike.mobike.SearchFragment.route_difficulty";
    public static final String ROUTE_BENDS = "com.mobike.mobike.SearchFragment.route_bends";
    public static final String ROUTE_TYPE = "com.mobike.mobike.SearchFragment.route_type";

    public static final String downloadAllRoutesURL = "http://mobike.ddns.net/SRV/routes/retrieveall";
    public static final String downloadUserRoutesURL = "http://mobike.ddns.net/SRV/routes/retrieve";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        if (getActivity().getIntent().getExtras() != null)
            pickingRoute = getActivity().getIntent().getExtras().getInt(REQUEST_CODE) == EventCreationActivity.ROUTE_REQUEST;

        Log.v(TAG, "pickingRoute: " + pickingRoute);

        firstTime = true;

        downloadRoutes(downloadAllRoutesURL);

        Log.v(TAG, "onCreate()");
    }

    @Override
    public void onStart() {
        super.onStart();

 /*       Spinner spinner = (Spinner) getView().findViewById(R.id.route_types);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.route_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this); */

        ((ImageButton) getView().findViewById(R.id.route_search)).setOnClickListener(this);

        ListView listView = (ListView) getView().findViewById(R.id.list_view);

        if (firstTime) {
            View header = View.inflate(getActivity(), R.layout.spinner, null);
            final Spinner spinner = (Spinner) header.findViewById(R.id.types);
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.route_types, android.R.layout.simple_spinner_item);
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
                            downloadRoutes(downloadAllRoutesURL);
                            break;
                        case 1: //downloadRoutes(downloadUserRoutesURL);
                            break;
                    }
                }
            });
        }

        initialSpinner = true;

        Log.v(TAG, "onStart()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
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

    // method called when an item in the Spinner is selected
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        Log.v(TAG, "onItemSelected()");
        if (initialSpinner) {
            initialSpinner = false;
            return;
        }
        switch (position) {
            case 0:
                downloadRoutes(downloadAllRoutesURL);
                break;
            case 1:
                //downloadRoutes(downloadUserRoutesURL);
                break;
        }
    }

    private void downloadRoutes(String url) {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        new HttpGetTask(this).execute(url);
        Log.v(TAG, "downloadRoutes: " + url);
    }

    //method called when no items in Spinner are selected
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void setResult(String result) {
        // grid view
        ListView listView = (ListView) getView().findViewById(R.id.list_view);
        ArrayList<Route> arrayList = new ArrayList<>();
        // TODO popolo l'arrayList con i dati presi dal json

        JSONObject jsonRoute;
        JSONArray json;
        String name, id, description, creator, duration, length, difficulty, bends, type, thumbnailURL, startLocation, endLocation; // ora type non c'è nel json
        int rating, ratingNumber;
        Bitmap map;

        try {
            json = new JSONArray(result);
            for (int i = 0; i< json.length(); i++) {
                jsonRoute = json.getJSONObject(i);
                name = jsonRoute.getString("name");
                name = name.substring(0,1).toUpperCase() + name.substring(1);
                id = jsonRoute.getInt("routeId") + "";
                description = jsonRoute.getString("description");
                creator = jsonRoute.getJSONObject("owner").getString("nickname");
                length = jsonRoute.getDouble("length") + "";
                duration = jsonRoute.getInt("duration") + "";
                difficulty = jsonRoute.getInt("difficulty") + "";
                bends = jsonRoute.getInt("bends") + "";
                type = jsonRoute.getString("type");
                rating = jsonRoute.getInt("rating");
                ratingNumber = jsonRoute.getInt("ratingNumber");
                thumbnailURL = jsonRoute.getString("imgUrl");
                startLocation = jsonRoute.getString("startLocation");
                endLocation = jsonRoute.getString("endLocation");

                arrayList.add(new Route(name, id, description, creator, length, duration, difficulty, bends, type, thumbnailURL, startLocation, endLocation, rating, ratingNumber));
            }
        }catch(JSONException e){e.printStackTrace();}

        // creo il gridAdapter
        RouteAdapter routeAdapter = new RouteAdapter(getActivity(), R.layout.route_list_row, arrayList);
        // imposto l'adapter
        listView.setAdapter(routeAdapter);
        // imposto il listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // faccio partire l'activity per la visualizzazione del percorso, passando i campi di Route in un bundle
                Route route = (Route) adapterView.getAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), RouteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(ROUTE_ID, route.getID());
                bundle.putString(ROUTE_NAME, route.getName());
                bundle.putString(ROUTE_DESCRIPTION, route.getDescription());
                bundle.putString(ROUTE_CREATOR, route.getCreator());
                bundle.putString(ROUTE_LENGTH, route.getLength());
                bundle.putString(ROUTE_DURATION, route.getDuration());
                bundle.putString(ROUTE_DIFFICULTY, route.getDifficulty());
                bundle.putString(ROUTE_BENDS, route.getBends());
                bundle.putString(ROUTE_TYPE, route.getType());
                if (pickingRoute) {
                    bundle.putInt(REQUEST_CODE, EventCreationActivity.ROUTE_REQUEST);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, EventCreationActivity.ROUTE_REQUEST);
                } else {
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.route_search:
                Intent intent = new Intent(getActivity(), RouteSearchActivity.class);
                startActivityForResult(intent, SEARCH_REQUEST);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == EventCreationActivity.ROUTE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                getActivity().setResult(Activity.RESULT_OK, intent);
                getActivity().finish();
            }
        } else if (requestCode == SearchFragment.SEARCH_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                downloadRoutes(intent.getExtras().getString(RouteSearchActivity.ROUTE_SEARCH_URL));
            }

        }
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

// custom class for square images
class SquareImageView extends ImageView {
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //Snap to width
    }
}



class RouteAdapter extends ArrayAdapter<Route> {
    private Context context;

    public RouteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public RouteAdapter(Context context, int resource, List<Route> items) {
        super(context, resource, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.route_list_row, null);

        }

        Route p = getItem(position);

        if (p != null) {

            TextView name = (TextView) v.findViewById(R.id.route_name);
            TextView length = (TextView) v.findViewById(R.id.route_length);
            TextView duration = (TextView) v.findViewById(R.id.route_duration);
            TextView creator = (TextView) v.findViewById(R.id.route_creator);
            ImageView type = (ImageView) v.findViewById(R.id.route_type);
            ImageView thumbnailView = (ImageView) v.findViewById(R.id.route_image);

            //Picasso.with(context).load(p.getMap()).into(imageView);

            if (name != null)
                name.setText(p.getName());
            if (length != null)
                length.setText(String.format("%.01f", Float.parseFloat(p.getLength())/1000) + " km");
            if (duration != null) {
                int durationInSeconds = Integer.parseInt(p.getDuration());
                duration.setText(String.valueOf(durationInSeconds/3600) + " h " + String.valueOf((durationInSeconds/60)%60) + " m " + String.valueOf(durationInSeconds%60) + " s");
            }
            if (creator != null)
                creator.setText(p.getCreator());
            if (type != null)
                type.setImageResource(p.getTypeColor());
            if (thumbnailView != null)
                Picasso.with(context).load(p.getThumbnailURL()).into(thumbnailView);
        }

        return v;

    }
}

// adapter for items in the grid view
/*class MyAdapter extends BaseAdapter {
    private List<Item> items = new ArrayList<Item>();
    private LayoutInflater inflater;

    public MyAdapter(Context context) {
        inflater = LayoutInflater.from(context);

        items.add(new Item("Pasdo Delle Capannelle",       R.drawable.ic_launcher));
        items.add(new Item("La Scarzuola",   R.drawable.ic_launcher));
        items.add(new Item("Spinaceto Palmarola", R.drawable.ic_launcher));
        items.add(new Item("Raduno A Leonessa",      R.drawable.ic_launcher));
        items.add(new Item("Giro Di Prova",     R.drawable.ic_launcher));
        items.add(new Item("Prova",      R.drawable.ic_launcher));
        items.add(new Item("Prova",      R.drawable.ic_launcher));
        items.add(new Item("Prova",      R.drawable.ic_launcher));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return items.get(i).drawableId;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        ImageView picture;
        TextView name;

        if(v == null) {
            v = inflater.inflate(R.layout.search_grid_item, viewGroup, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text, v.findViewById(R.id.text));
        }

        picture = (ImageView)v.getTag(R.id.picture);
        name = (TextView)v.getTag(R.id.text);

        Item item = (Item)getItem(i);

        picture.setImageResource(item.drawableId);
        name.setText(item.name);

        return v;
    }

    // class for items in the grid view
    private class Item {
        final String name;
        final int drawableId;

        Item(String name, int drawableId) {
            this.name = name;
            this.drawableId = drawableId;
        }
    }
}*/
