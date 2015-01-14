package com.mobike.mobike;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.mobike.mobike.DatabaseStrings.*;
/**
 * This class has two important features: it has a reference to the helper object defined
 * in the inner class and contains the method that the other components of the app
 * will use to perform operations and queries on the database.
 *
 */

public class GPSDatabase
{
    private Context context;
    private DbHelper dbHelper; //the reference to the Helper object

    public final String DBNAME="gpsdb";
    public final int DBVERSION=1;

    // The raw code to initialize the database and the (only) table it will use.
    public final String CREATERDB="CREATE TABLE "+ TABLENAME+"("+ FIELD_ID +" INTEGER PRIMARY KEY, " +
            FIELD_LAT+" VARCHAR NOT NULL, "+ FIELD_LNG+" VARCHAR NOT NULL, "+FIELD_ALT +" VARCHAR, " +
            FIELD_TIME+" INTEGER, " + FIELD_DIST + " REAL);";


    /**
     * The constructor, that creates the DBHelper object.
     * @param context the context
     */
    public GPSDatabase(Context context){
        this.context = context;
        dbHelper = new DbHelper(context);
    }


    /**
     * This class manages the creation and the upgrade of the database and gives a
     * reference to the helper object to retrieve data from the actual database.
     */
     public class DbHelper extends SQLiteOpenHelper {

        /**
         * This constructor method create the object that will make possible to perform operation
         * on the database DBNAME.
         * @param context the context of the app
         */
        public DbHelper(Context context){
            super(context,DBNAME,null,DBVERSION);
        }


        /** This method is invoked only once, when it does not exists a database DBNAME.
         * It executes the raw SQLite code in CREATERDB.
         * @param db the db to create
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATERDB);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // to implement (maybe)
        }
    }

    /**
     * This method adds a new row to the database.
     * @param lat the latitude of the new location
     * @param lng the longitude of the new location
     * @param alt the latitude of the new location
     * @return a random long
     */
    public long insertRow(double lat, double lng, double alt, float dist)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues value=new ContentValues();
        value.put(FIELD_LAT, lat + "");
        value.put(FIELD_LNG, lng + "");
        value.put(FIELD_ALT, alt + "");
        value.put(FIELD_DIST, dist + "");
        value.put(FIELD_TIME, System.currentTimeMillis());
        try
        {
            long l = db.insert(TABLENAME, null, value);
            db.close();
            return l;
        }
        catch (SQLiteException sqle)
        {
            db.close();
            return -2;
        }
    }

    /**
     * This method performs a query for all the rows in the table TABLENAME.
     * @return cursor, a Cursor object.
     */
    private Cursor getAllRows(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLENAME,
                new String[]{FIELD_ID,FIELD_LAT,FIELD_LNG,FIELD_ALT, FIELD_TIME, FIELD_DIST}, null,null, null, null, null);
        //db.close();
        return cursor;
    }

    /**
     * This method deletes all the rows from the table.
     */
    public void deleteTable(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLENAME);
        db.close();
    }

    /**
     * This method creates a JSON that contains all the data in the database.
     * @return JSONArray the jsonArray object representing the table
     */
    private JSONArray getTableInJSON(){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = getAllRows();
        cursor.moveToFirst();

        JSONArray resultSet = new JSONArray();
        int totalColumn;

        while (!cursor.isAfterLast()){
            totalColumn = cursor.getColumnCount();

            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i< totalColumn ; i++ ){
                if( cursor.getColumnName(i) != null ){
                    try{
                        if( cursor.getString(i) != null ){
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else{
                            rowObject.put( cursor.getColumnName(i) ,  JSONObject.NULL );
                        }
                    }
                    catch( Exception e ){
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        Log.d("TAG_NAME", resultSet.toString() );
        return resultSet;
    }

    /**
     * This method performs a query on the columns of the latitude and the longitude
     * for all the rows.
     * @return an ArrayList<LatLng> containing all the recorded locations.
     */
    public ArrayList<LatLng> getAllLocations() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLENAME,
                new String[]{FIELD_LAT, FIELD_LNG}, null, null, null, null, null);

        ArrayList<LatLng> returnLst = new ArrayList<LatLng>();

        cursor.moveToFirst();

        double latitude;
        double longitude;

        while (!cursor.isAfterLast()) {
            latitude = Double.parseDouble(cursor.getString(0));
            longitude = Double.parseDouble(cursor.getString(1));

            returnLst.add(new LatLng(latitude, longitude) );

            cursor.moveToNext();
        }

        cursor.close();
        db.close();
        return returnLst;
    }

    /**
     * This method gives the total lenght of the route, that is the distance of the
     * last point from the first one.
     * @return the total length of the path.
     */
    public float getTotalLength() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLENAME, new String[]{FIELD_DIST}, null, null, null, null, null);
        cursor.moveToLast();
        float totLen = Float.parseFloat(cursor.getString(0));
        cursor.close();
        db.close();
        return totLen;
    }

    /**
     * This method gives the duration of the route by performing the difference between
     * the timestamps of the last and the first row of the table.
     *
     * @return The duration expressed in seconds
     */
    public long getTotalDuration() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLENAME, new String[]{FIELD_TIME}, null, null, null, null, null);
        cursor.moveToFirst();
        long start = cursor.getLong(0);
        cursor.moveToLast();
        long end = cursor.getLong(0);
        cursor.close();
        db.close();
        return (end - start)/1000;
    }

    /**
     * This method takes the name and the description the user gave to the route and creates
     * a string of the route in the GPX 1.1 format
     * @param email the user email
     * @param name the name the user gave to the route
     * @param description the description the user gave to the route
     * @return a String representing the route in the GPX 1.1 format
     */
    private String getTableInGPX(String email, String name, String description) {
        String gpxString = "";
        gpxString += "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n" +
                "<gpx\n" +
                "  xmlns=\"http://www.topografix.com/GPX/1/0\"\n" +
                "  version=\"1.0\" creator=\"MoBike Mobile App\"\n" +
                "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n";
        gpxString += "<metadata>\n"+
                "<name>"+name+"</name>\n" +
                "<desc>"+description+"</desc>\n" +
               "<author>\n"+
                "<name>"+email.substring(0, email.indexOf("@"))+"</name>\n"+
                "<email " +
                "id=\""+    email.substring(0, email.indexOf("@"))+"\""+
                "domain=\""+email.substring(email.indexOf("@")+1)+"\">\n"+
                "</author>\n"+
                "</metadata>\n";

        gpxString += "<trk><name>"+name+"</name>\n" +
                "<desc>"+description+"</desc>\n" +
                "<trkseg>\n";


        JSONArray array = getTableInJSON();
        String lat, lng, alt;
        long time;
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject row = array.getJSONObject(i);
                lat = row.getString(FIELD_LAT);
                lng = row.getString(FIELD_LNG);
                alt = row.getString(FIELD_ALT);
                time = row.getLong(FIELD_TIME);

                gpxString += "<trkpt lat=\"" + lat + "\" lon=\"" + lng + "\"><ele>" +
                        alt + "</ele><time>"+millisTimeToStr(time)+"</time></trkpt>\n";
            } catch (JSONException e) {/*not implemented yet*/ }
        }

        gpxString += "</trkseg>\n" +
                "</trk>\n" +
                "</gpx>";
        return gpxString;
    }

    /**
     * This method is invoked when the user saves the route.
     * This method creates the JSONObject representing the json file to be sent using
     * the REST protocol.
     * @param email the user email
     * @param name the name the user gave to the route
     * @param description the description the user gave to the route
     * @return the JSONObject containing all the informations on the route.
     */
    public JSONObject exportRouteInJson(String email, String name, String description){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("creatorEmail", email);
            jsonObject.put("description", description);
            jsonObject.put("duration", getTotalDuration());
            jsonObject.put("length", getTotalLength());
            jsonObject.put("name", name);
            jsonObject.put("gpxString", getTableInGPX(email, name, description));
        }
        catch(JSONException e){/*not implemented yet*/ }
        return jsonObject;
    }

    /**
     * This method converts a timestamp expressed in unix epoch time (milliseconds since 1/1/1970)
     * to a readable date/time format, in particular the xsd:datetime format of the XML standard,
     * because this one is used in GPX 1.1 format too.
     * @param millis the ecpoch time
     * @return a date in xsd:datetime format
     */
    private static String millisTimeToStr(long millis){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return format.format(calendar.getTime());
    }

    /**
     * This method creates a reference to the database.
     * @throws SQLException
     */
    public void open() throws SQLException {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //return true;
    }


    public void close(){
        dbHelper.close();
        //return true;
    }
}