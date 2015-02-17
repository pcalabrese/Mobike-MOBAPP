package com.mobike.mobike;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    public static final String downloadURL = "qualcosa";
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
        String gpx = "<gpx xmlns=\"http://www.topografix.com/GPX/1/0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" creator=\"MoBike Mobile App\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n" +
                "<metadata>\n" +
                "<name>Spinaceto Palmarola</name>\n" +
                "<desc>Casa di Silvia to Casa di Bruno</desc>\n" +
                "</metadata>\n" +
                "<trk>\n" +
                "<name>Spinaceto Palmarola</name>\n" +
                "<desc>Casa di Silvia to Casa di Bruno</desc>\n" +
                "<trkseg>\n" +
                "<trkpt lat=\"41.785943\" lon=\"12.4322157\">\n" +
                "<ele>97.0</ele>\n" +
                "<time>2015-01-08T16-31-50</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7856063\" lon=\"12.4318234\">\n" +
                "<ele>83.0</ele>\n" +
                "<time>2015-01-08T16-31-55</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7854428\" lon=\"12.4313371\">\n" +
                "<ele>88.0</ele>\n" +
                "<time>2015-01-08T16-32-00</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7851966\" lon=\"12.4310484\">\n" +
                "<ele>89.0</ele>\n" +
                "<time>2015-01-08T16-32-05</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7853045\" lon=\"12.4304058\">\n" +
                "<ele>86.0</ele>\n" +
                "<time>2015-01-08T16-32-10</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7862962\" lon=\"12.4285927\">\n" +
                "<ele>87.0</ele>\n" +
                "<time>2015-01-08T16-32-23</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7872474\" lon=\"12.4270257\">\n" +
                "<ele>87.0</ele>\n" +
                "<time>2015-01-08T16-32-33</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7875363\" lon=\"12.4262905\">\n" +
                "<ele>89.0</ele>\n" +
                "<time>2015-01-08T16-32-43</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7873121\" lon=\"12.42631\">\n" +
                "<ele>86.0</ele>\n" +
                "<time>2015-01-08T16-32-53</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7880592\" lon=\"12.425282\">\n" +
                "<ele>85.0</ele>\n" +
                "<time>2015-01-08T16-33-03</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7891245\" lon=\"12.4238746\">\n" +
                "<ele>90.0</ele>\n" +
                "<time>2015-01-08T16-33-13</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7898605\" lon=\"12.4229976\">\n" +
                "<ele>87.0</ele>\n" +
                "<time>2015-01-08T16-33-23</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7905715\" lon=\"12.4222133\">\n" +
                "<ele>90.0</ele>\n" +
                "<time>2015-01-08T16-33-33</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.791433\" lon=\"12.4215673\">\n" +
                "<ele>88.0</ele>\n" +
                "<time>2015-01-08T16-33-44</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7919927\" lon=\"12.4211504\">\n" +
                "<ele>84.0</ele>\n" +
                "<time>2015-01-08T16-33-53</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7928792\" lon=\"12.4207306\">\n" +
                "<ele>90.0</ele>\n" +
                "<time>2015-01-08T16-34-04</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7943874\" lon=\"12.4200674\">\n" +
                "<ele>87.0</ele>\n" +
                "<time>2015-01-08T16-34-15</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7957376\" lon=\"12.4192582\">\n" +
                "<ele>90.0</ele>\n" +
                "<time>2015-01-08T16-34-25</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.796563\" lon=\"12.4186164\">\n" +
                "<ele>82.0</ele>\n" +
                "<time>2015-01-08T16-34-34</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7972501\" lon=\"12.4179831\">\n" +
                "<ele>85.0</ele>\n" +
                "<time>2015-01-08T16-34-46</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7983407\" lon=\"12.417516\">\n" +
                "<ele>86.0</ele>\n" +
                "<time>2015-01-08T16-34-56</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.7995326\" lon=\"12.4172326\">\n" +
                "<ele>91.0</ele>\n" +
                "<time>2015-01-08T16-35-06</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8001026\" lon=\"12.418317\">\n" +
                "<ele>91.0</ele>\n" +
                "<time>2015-01-08T16-35-16</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8004668\" lon=\"12.4179296\">\n" +
                "<ele>95.0</ele>\n" +
                "<time>2015-01-08T16-35-26</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8014085\" lon=\"12.4180654\">\n" +
                "<ele>87.0</ele>\n" +
                "<time>2015-01-08T16-35-35</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8029359\" lon=\"12.4186402\">\n" +
                "<ele>95.0</ele>\n" +
                "<time>2015-01-08T16-35-46</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8044928\" lon=\"12.4194162\">\n" +
                "<ele>100.0</ele>\n" +
                "<time>2015-01-08T16-35-56</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8063065\" lon=\"12.4200646\">\n" +
                "<ele>102.0</ele>\n" +
                "<time>2015-01-08T16-36-06</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8085813\" lon=\"12.4210689\">\n" +
                "<ele>104.0</ele>\n" +
                "<time>2015-01-08T16-36-18</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8100035\" lon=\"12.4231296\">\n" +
                "<ele>108.0</ele>\n" +
                "<time>2015-01-08T16-36-29</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8108231\" lon=\"12.4238716\">\n" +
                "<ele>107.0</ele>\n" +
                "<time>2015-01-08T16-36-39</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8111068\" lon=\"12.4230722\">\n" +
                "<ele>96.0</ele>\n" +
                "<time>2015-01-08T16-36-47</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8100256\" lon=\"12.4220292\">\n" +
                "<ele>44.0</ele>\n" +
                "<time>2015-01-08T16-36-58</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8091278\" lon=\"12.4205518\">\n" +
                "<ele>87.0</ele>\n" +
                "<time>2015-01-08T16-37-07</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8081518\" lon=\"12.4191178\">\n" +
                "<ele>94.0</ele>\n" +
                "<time>2015-01-08T16-37-18</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8082451\" lon=\"12.4171599\">\n" +
                "<ele>45.0</ele>\n" +
                "<time>2015-01-08T16-37-27</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8086611\" lon=\"12.4141094\">\n" +
                "<ele>45.0</ele>\n" +
                "<time>2015-01-08T16-37-38</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8088748\" lon=\"12.4106595\">\n" +
                "<ele>43.0</ele>\n" +
                "<time>2015-01-08T16-37-47</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8093898\" lon=\"12.4073506\">\n" +
                "<ele>46.0</ele>\n" +
                "<time>2015-01-08T16-37-57</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8100029\" lon=\"12.4042558\">\n" +
                "<ele>47.0</ele>\n" +
                "<time>2015-01-08T16-38-08</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8108685\" lon=\"12.4008374\">\n" +
                "<ele>33.0</ele>\n" +
                "<time>2015-01-08T16-38-18</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8125565\" lon=\"12.3970944\">\n" +
                "<ele>46.0</ele>\n" +
                "<time>2015-01-08T16-38-29</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8143695\" lon=\"12.3945396\">\n" +
                "<ele>49.0</ele>\n" +
                "<time>2015-01-08T16-38-39</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8166297\" lon=\"12.3921805\">\n" +
                "<ele>50.0</ele>\n" +
                "<time>2015-01-08T16-38-49</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8192515\" lon=\"12.3905985\">\n" +
                "<ele>50.0</ele>\n" +
                "<time>2015-01-08T16-38-59</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8225813\" lon=\"12.38953\">\n" +
                "<ele>45.0</ele>\n" +
                "<time>2015-01-08T16-39-11</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8257996\" lon=\"12.3884225\">\n" +
                "<ele>66.0</ele>\n" +
                "<time>2015-01-08T16-39-22</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8282857\" lon=\"12.3877333\">\n" +
                "<ele>52.0</ele>\n" +
                "<time>2015-01-08T16-39-31</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8313697\" lon=\"12.3867848\">\n" +
                "<ele>62.0</ele>\n" +
                "<time>2015-01-08T16-39-41</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8341542\" lon=\"12.3860491\">\n" +
                "<ele>63.0</ele>\n" +
                "<time>2015-01-08T16-39-51</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8373274\" lon=\"12.3849989\">\n" +
                "<ele>66.0</ele>\n" +
                "<time>2015-01-08T16-40-01</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8407637\" lon=\"12.3837911\">\n" +
                "<ele>68.0</ele>\n" +
                "<time>2015-01-08T16-40-12</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8437538\" lon=\"12.3825562\">\n" +
                "<ele>76.0</ele>\n" +
                "<time>2015-01-08T16-40-23</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8463311\" lon=\"12.381429\">\n" +
                "<ele>71.0</ele>\n" +
                "<time>2015-01-08T16-40-32</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8491336\" lon=\"12.380297\">\n" +
                "<ele>74.0</ele>\n" +
                "<time>2015-01-08T16-40-42</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8517346\" lon=\"12.3788971\">\n" +
                "<ele>73.0</ele>\n" +
                "<time>2015-01-08T16-40-52</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8545467\" lon=\"12.3779117\">\n" +
                "<ele>78.0</ele>\n" +
                "<time>2015-01-08T16-41-03</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8567841\" lon=\"12.3769897\">\n" +
                "<ele>80.0</ele>\n" +
                "<time>2015-01-08T16-41-12</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8594129\" lon=\"12.3758409\">\n" +
                "<ele>74.0</ele>\n" +
                "<time>2015-01-08T16-41-22</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8623208\" lon=\"12.3746899\">\n" +
                "<ele>79.0</ele>\n" +
                "<time>2015-01-08T16-41-33</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8650772\" lon=\"12.3738651\">\n" +
                "<ele>84.0</ele>\n" +
                "<time>2015-01-08T16-41-42</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8676019\" lon=\"12.3741351\">\n" +
                "<ele>84.0</ele>\n" +
                "<time>2015-01-08T16-41-52</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8707015\" lon=\"12.37585\">\n" +
                "<ele>84.0</ele>\n" +
                "<time>2015-01-08T16-42-03</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.873767\" lon=\"12.3774698\">\n" +
                "<ele>78.0</ele>\n" +
                "<time>2015-01-08T16-42-15</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8767623\" lon=\"12.3790351\">\n" +
                "<ele>78.0</ele>\n" +
                "<time>2015-01-08T16-42-25</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8798885\" lon=\"12.3804389\">\n" +
                "<ele>82.0</ele>\n" +
                "<time>2015-01-08T16-42-35</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8825025\" lon=\"12.3809194\">\n" +
                "<ele>83.0</ele>\n" +
                "<time>2015-01-08T16-42-44</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8839299\" lon=\"12.380973\">\n" +
                "<ele>82.0</ele>\n" +
                "<time>2015-01-08T16-42-49</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8875539\" lon=\"12.3810661\">\n" +
                "<ele>78.0</ele>\n" +
                "<time>2015-01-08T16-43-01</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8906346\" lon=\"12.3812414\">\n" +
                "<ele>87.0</ele>\n" +
                "<time>2015-01-08T16-43-10</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8938114\" lon=\"12.3815877\">\n" +
                "<ele>84.0</ele>\n" +
                "<time>2015-01-08T16-43-21</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8966456\" lon=\"12.3818404\">\n" +
                "<ele>91.0</ele>\n" +
                "<time>2015-01-08T16-43-29</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.8996097\" lon=\"12.3827659\">\n" +
                "<ele>86.0</ele>\n" +
                "<time>2015-01-08T16-43-39</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9023668\" lon=\"12.3836707\">\n" +
                "<ele>95.0</ele>\n" +
                "<time>2015-01-08T16-43-49</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9053857\" lon=\"12.3843712\">\n" +
                "<ele>96.0</ele>\n" +
                "<time>2015-01-08T16-44-01</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9079749\" lon=\"12.3848112\">\n" +
                "<ele>96.0</ele>\n" +
                "<time>2015-01-08T16-44-10</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9109589\" lon=\"12.3852996\">\n" +
                "<ele>96.0</ele>\n" +
                "<time>2015-01-08T16-44-19</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9140765\" lon=\"12.3853032\">\n" +
                "<ele>104.0</ele>\n" +
                "<time>2015-01-08T16-44-31</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9167887\" lon=\"12.3847019\">\n" +
                "<ele>92.0</ele>\n" +
                "<time>2015-01-08T16-44-41</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9192235\" lon=\"12.3838726\">\n" +
                "<ele>99.0</ele>\n" +
                "<time>2015-01-08T16-44-51</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9216038\" lon=\"12.3826691\">\n" +
                "<ele>99.0</ele>\n" +
                "<time>2015-01-08T16-45-01</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9236552\" lon=\"12.3818534\">\n" +
                "<ele>101.0</ele>\n" +
                "<time>2015-01-08T16-45-11</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9258661\" lon=\"12.381516\">\n" +
                "<ele>100.0</ele>\n" +
                "<time>2015-01-08T16-45-21</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.927708\" lon=\"12.3813161\">\n" +
                "<ele>97.0</ele>\n" +
                "<time>2015-01-08T16-45-31</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9287725\" lon=\"12.3823743\">\n" +
                "<ele>100.0</ele>\n" +
                "<time>2015-01-08T16-45-42</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9293011\" lon=\"12.3825456\">\n" +
                "<ele>104.0</ele>\n" +
                "<time>2015-01-08T16-45-51</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9294469\" lon=\"12.381892\">\n" +
                "<ele>106.0</ele>\n" +
                "<time>2015-01-08T16-46-02</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9283698\" lon=\"12.3809232\">\n" +
                "<ele>107.0</ele>\n" +
                "<time>2015-01-08T16-46-14</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9285809\" lon=\"12.3801428\">\n" +
                "<ele>107.0</ele>\n" +
                "<time>2015-01-08T16-46-21</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.929369\" lon=\"12.3797847\">\n" +
                "<ele>107.0</ele>\n" +
                "<time>2015-01-08T16-46-32</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9304511\" lon=\"12.3802341\">\n" +
                "<ele>109.0</ele>\n" +
                "<time>2015-01-08T16-46-42</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9316217\" lon=\"12.3800885\">\n" +
                "<ele>110.0</ele>\n" +
                "<time>2015-01-08T16-46-52</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9326641\" lon=\"12.3795755\">\n" +
                "<ele>110.0</ele>\n" +
                "<time>2015-01-08T16-47-03</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.933632\" lon=\"12.3796894\">\n" +
                "<ele>108.0</ele>\n" +
                "<time>2015-01-08T16-47-13</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9343963\" lon=\"12.380219\">\n" +
                "<ele>104.0</ele>\n" +
                "<time>2015-01-08T16-47-23</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9346612\" lon=\"12.3811714\">\n" +
                "<ele>112.0</ele>\n" +
                "<time>2015-01-08T16-47-42</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9345597\" lon=\"12.3813971\">\n" +
                "<ele>113.0</ele>\n" +
                "<time>2015-01-08T16-47-48</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9346554\" lon=\"12.3820115\">\n" +
                "<ele>104.0</ele>\n" +
                "<time>2015-01-08T16-47-56</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9351168\" lon=\"12.382449\">\n" +
                "<ele>102.0</ele>\n" +
                "<time>2015-01-08T16-48-04</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9355898\" lon=\"12.3826539\">\n" +
                "<ele>104.0</ele>\n" +
                "<time>2015-01-08T16-48-14</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9358182\" lon=\"12.3829741\">\n" +
                "<ele>137.0</ele>\n" +
                "<time>2015-01-08T16-48-34</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9364782\" lon=\"12.3836395\">\n" +
                "<ele>139.0</ele>\n" +
                "<time>2015-01-08T16-48-46</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9372032\" lon=\"12.3844271\">\n" +
                "<ele>128.0</ele>\n" +
                "<time>2015-01-08T16-48-57</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9375662\" lon=\"12.38473\">\n" +
                "<ele>125.0</ele>\n" +
                "<time>2015-01-08T16-49-06</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9382225\" lon=\"12.3853613\">\n" +
                "<ele>111.0</ele>\n" +
                "<time>2015-01-08T16-49-16</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.939125\" lon=\"12.3858552\">\n" +
                "<ele>120.0</ele>\n" +
                "<time>2015-01-08T16-49-28</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9388278\" lon=\"12.3857369\">\n" +
                "<ele>121.0</ele>\n" +
                "<time>2015-01-08T16-49-38</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9394077\" lon=\"12.3859839\">\n" +
                "<ele>123.0</ele>\n" +
                "<time>2015-01-08T16-49-47</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9398586\" lon=\"12.386357\">\n" +
                "<ele>121.0</ele>\n" +
                "<time>2015-01-08T16-49-56</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9401819\" lon=\"12.3867644\">\n" +
                "<ele>120.0</ele>\n" +
                "<time>2015-01-08T16-50-06</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9407269\" lon=\"12.3868893\">\n" +
                "<ele>130.0</ele>\n" +
                "<time>2015-01-08T16-50-17</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9411373\" lon=\"12.3870782\">\n" +
                "<ele>113.0</ele>\n" +
                "<time>2015-01-08T16-50-26</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9413607\" lon=\"12.3872179\">\n" +
                "<ele>125.0</ele>\n" +
                "<time>2015-01-08T16-50-36</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9420556\" lon=\"12.3888255\">\n" +
                "<ele>128.0</ele>\n" +
                "<time>2015-01-08T16-50-48</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9433647\" lon=\"12.3900077\">\n" +
                "<ele>141.0</ele>\n" +
                "<time>2015-01-08T16-50-57</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9446838\" lon=\"12.3909385\">\n" +
                "<ele>136.0</ele>\n" +
                "<time>2015-01-08T16-51-07</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9461219\" lon=\"12.3916394\">\n" +
                "<ele>145.0</ele>\n" +
                "<time>2015-01-08T16-51-18</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9470817\" lon=\"12.3921396\">\n" +
                "<ele>140.0</ele>\n" +
                "<time>2015-01-08T16-51-28</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9471672\" lon=\"12.3923771\">\n" +
                "<ele>135.0</ele>\n" +
                "<time>2015-01-08T16-51-39</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9475153\" lon=\"12.3924747\">\n" +
                "<ele>143.0</ele>\n" +
                "<time>2015-01-08T16-51-57</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9477932\" lon=\"12.3926803\">\n" +
                "<ele>158.0</ele>\n" +
                "<time>2015-01-08T16-52-07</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.94791\" lon=\"12.3924021\">\n" +
                "<ele>170.0</ele>\n" +
                "<time>2015-01-08T16-52-29</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9481856\" lon=\"12.3920087\">\n" +
                "<ele>172.0</ele>\n" +
                "<time>2015-01-08T16-52-39</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9484411\" lon=\"12.3912661\">\n" +
                "<ele>171.0</ele>\n" +
                "<time>2015-01-08T16-52-50</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.948659\" lon=\"12.3907514\">\n" +
                "<ele>167.0</ele>\n" +
                "<time>2015-01-08T16-53-00</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9489501\" lon=\"12.3901071\">\n" +
                "<ele>167.0</ele>\n" +
                "<time>2015-01-08T16-53-10</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.9493007\" lon=\"12.3903625\">\n" +
                "<ele>151.0</ele>\n" +
                "<time>2015-01-08T16-54-37</time>\n" +
                "</trkpt>\n" +
                "<trkpt lat=\"41.948981\" lon=\"12.3902743\">\n" +
                "<ele>153.0</ele>\n" +
                "<time>2015-01-08T16-54-45</time>\n" +
                "</trkpt>\n" +
                "</trkseg>\n" +
                "</trk>\n" +
                "</gpx>";
        ArrayList<Route> arrayList = new ArrayList<>();
        // popolo l'arrayList
        arrayList.add(new Route("Spinaceto - Palmarola", description, "Created by Andrea Donati", "50 km", "44m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), gpx, "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Sora", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), gpx, "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Viterbo", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), "gpx", "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Perugia", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), gpx, "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Terni", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), gpx, "3", "3", "Mountain"));
        arrayList.add(new Route("Roma - Bolsena", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), gpx, "7", "5", "Mountain"));
        arrayList.add(new Route("Roma - Frosinone", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), gpx, "7", "5", "Mountain"));
        arrayList.add(new Route("Roma - Sutri", description, "Created by Andrea Donati", "150 km", "1h 32m 06s", BitmapFactory.decodeResource(getActivity().getResources(),
                R.drawable.staticmap), gpx, "7", "5", "Mountain"));
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

    public void showRouteList(JSONObject json){
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
        ArrayList<Route> arrayList = new ArrayList<>();
        // TODO popolo l'arrayList con i dati presi dal json

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
            return HTTPGetRoutes(downloadURL);
        }

        @Override
        protected void onPostExecute(String result) {
            try{
                JSONObject json = new JSONObject(result);
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
