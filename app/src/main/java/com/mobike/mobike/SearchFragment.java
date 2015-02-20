package com.mobike.mobike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mobike.mobike.utils.Route;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
public class SearchFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public static final String ROUTE_NAME = "com.mobike.mobike.SearchFragment.route_name";
    public static final String ROUTE_DESCRIPTION = "com.mobike.mobike.SearchFragment.route_description";
    public static final String ROUTE_CREATOR = "com.mobike.mobike.SearchFragment.route_creator";
    public static final String ROUTE_LENGTH = "com.mobike.mobike.SearchFragment.route_length";
    public static final String ROUTE_DURATION = "com.mobike.mobike.SearchFragment.route_duration";
    public static final String ROUTE_GPX = "com.mobike.mobike.SearchFragment.route_gpx";
    public static final String ROUTE_DIFFICULTY = "com.mobike.mobike.SearchFragment.route_difficulty";
    public static final String ROUTE_BENDS = "com.mobike.mobike.SearchFragment.route_bends";
    public static final String ROUTE_TYPE = "com.mobike.mobike.SearchFragment.route_type";

    public static final String downloadRoutesURL = "http://mobike.ddns.net/SRV/routes/retrieveall";
    public static final String GPXURL = "http://mobike.ddns.net/SRV/routes";
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
    }

    @Override
    public void onStart() {
        super.onStart();

        new DownloadRoutesTask().execute();

        Spinner spinner = (Spinner) getView().findViewById(R.id.route_types);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.route_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        // grid view
        GridView gridView = (GridView) getView().findViewById(R.id.gridview);
        String description = "Route description. There will be all the route's details written by the creator at the creation. There will be more lines.";
        ArrayList<Route> arrayList = new ArrayList<>();
        // popolo l'arrayList
        arrayList.add(new Route("Spinaceto - Palmarola", description, "Created by Andrea Donati", "50 km", "44m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx", "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Sora", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx", "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Viterbo", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx", "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Perugia", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx", "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Terni", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx", "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Bolsena", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx", "7", "5", "Mountain"));
        arrayList.add(new Route("Roma - Frosinone", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx", "7", "5", "Mountain"));
        arrayList.add(new Route("Roma - Sutri", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx", "7", "5", "Mountain"));
        // creo il gridAdapter
        GridAdapter gridAdapter = new GridAdapter(getActivity(), R.layout.search_grid_item, arrayList);
        // imposto l'adapter
        gridView.setAdapter(gridAdapter);
        // imposto il listener
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // faccio partire l'activity per la visualizzazione del percorso, passando i campi di Route in un bundle
                Route route = (Route) adapterView.getAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), RouteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(ROUTE_NAME, route.getName());
                bundle.putString(ROUTE_DESCRIPTION, route.getDescription());
                bundle.putString(ROUTE_CREATOR, route.getCreator());
                bundle.putString(ROUTE_LENGTH, route.getLength());
                bundle.putString(ROUTE_DURATION, route.getDuration());
                bundle.putString(ROUTE_GPX, route.getGpx());
                bundle.putString(ROUTE_DIFFICULTY, route.getDifficulty());
                bundle.putString(ROUTE_BENDS, route.getBends());
                bundle.putString(ROUTE_TYPE, route.getType());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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
        switch (position) {
            case 0: // first item in the spinner
                break;
            case 1: // second item
                break;
        }
    }

    //method called when no items in Spinner are selected
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void showRouteList(JSONArray json) {
/*        Spinner spinner = (Spinner) getView().findViewById(R.id.route_types);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.route_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
*/

        // grid view
        GridView gridView = (GridView) getView().findViewById(R.id.gridview);
        ArrayList<Route> arrayList = new ArrayList<>();
        // TODO popolo l'arrayList con i dati presi dal json
        AsyncTask<String, Void, String> a = new GPXTask();

        JSONObject jsonRoute;
        String name, description, creator, duration, length, gpx, difficulty, bends, type; // ora type non c'è nel json
        Bitmap map;

        for (int i = 0; i< json.length(); i++){
            try{
                jsonRoute = json.getJSONObject(i);
                name = jsonRoute.getString("name");
                description = jsonRoute.getString("description");
                creator = jsonRoute.getString("creatorEmail");
                length = jsonRoute.getDouble("length") + "";
                duration = jsonRoute.getInt("duration")+"";
                map = BitmapFactory.decodeResource(getActivity().getResources(),R.drawable.staticmap);
                gpx = jsonRoute.getString("url");
                difficulty = jsonRoute.getInt("difficulty") + "";
                bends = jsonRoute.getInt("bends") + "";
                type = "DefaultRouteType";
                new Route(name, description, creator, length, duration, map, gpx,
                        difficulty, bends, type);

                arrayList.add(new Route(name, description, creator, length, duration, map, gpx, difficulty, bends, type));

            }catch(JSONException e){e.printStackTrace();}
        }


        // creo il gridAdapter
        GridAdapter gridAdapter = new GridAdapter(getActivity(), R.layout.search_grid_item, arrayList);
        // imposto l'adapter
        gridView.setAdapter(gridAdapter);
        // imposto il listener
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // faccio partire l'activity per la visualizzazione del percorso, passando i campi di Route in un bundle
                Route route = (Route) adapterView.getAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), RouteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(ROUTE_NAME, route.getName());
                bundle.putString(ROUTE_DESCRIPTION, route.getDescription());
                bundle.putString(ROUTE_CREATOR, route.getCreator());
                bundle.putString(ROUTE_LENGTH, route.getLength());
                bundle.putString(ROUTE_DURATION, route.getDuration());
                bundle.putString(ROUTE_GPX, route.getGpx());
                bundle.putString(ROUTE_DIFFICULTY, route.getDifficulty());
                bundle.putString(ROUTE_BENDS, route.getBends());
                bundle.putString(ROUTE_TYPE, route.getType());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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

    private class DownloadRoutesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return HTTPGetRoutes(downloadRoutesURL);
        }

        @Override
        protected void onPostExecute(String result) {
            try{
                JSONArray json = new JSONArray(result);
                showRouteList(json);
            }catch(JSONException e)
            { e.printStackTrace();};
        }

        private String HTTPGetRoutes(String url){
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

    private class GPXTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return HTTPGetRoutes(GPXURL+"/"+ urls[0] + "/gpx");
        }

        @Override
        protected void onPostExecute(String result) {

        }

        private String HTTPGetRoutes(String url){
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



class GridAdapter extends ArrayAdapter<Route> {

    public GridAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public GridAdapter(Context context, int resource, List<Route> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.search_grid_item, null);

        }

        Route p = getItem(position);

        if (p != null) {

            TextView textView = (TextView) v.findViewById(R.id.text);
            ImageView imageView = (ImageView) v.findViewById(R.id.picture);

            if (textView != null) {
                textView.setText(p.getName());
            }
            if (imageView != null) {

                imageView.setImageBitmap(p.getMap());
            }
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
