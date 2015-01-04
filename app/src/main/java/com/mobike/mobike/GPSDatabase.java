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
import org.json.JSONObject;

import java.util.ArrayList;

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
            FIELD_TIME+" DATETIME DEFAULT CURRENT_TIMESTAMP, " + FIELD_DIST + " REAL);";


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
     * @param lng the longitude of the new locatio
     * @param alt the latitude of the new locatio
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
        try
        {
            long l = db.insert(TABLENAME, null, value);
            db.close();
            return l;
        }
        catch (SQLiteException sqle)
        {
            // not yet implemented
        }
        db.close();
        return 0;
    }

    /**
     * This method performs a query for all the rows in the table TABLENAME.
     * @return cursor, a Cursor object.
     */
    private Cursor getAllRows(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TABLENAME,
                new String[]{FIELD_ID,FIELD_LAT,FIELD_LNG,FIELD_ALT, FIELD_TIME, FIELD_DIST}, null,null, null, null, null);
        db.close();
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
     * @return JSONArray
     */
    public JSONArray getTableInJSON(){
        Cursor cursor = getAllRows();
        cursor.moveToFirst();

        JSONArray resultSet = new JSONArray();
        int totalColumn;

        while (!cursor.isAfterLast()){
            totalColumn = cursor.getColumnCount();

            JSONObject rowObject = new JSONObject();

            for( int i=0 ;  i< totalColumn ; i++ )
            {
                if( cursor.getColumnName(i) != null )
                {
                    try
                    {
                        if( cursor.getString(i) != null )
                        {
                            rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                        }
                        else
                        {
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    }
                    catch( Exception e )
                    {
                        Log.d("TAG_NAME", e.getMessage());
                    }
                }

            }

            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
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

        ArrayList<LatLng> returnLst = new ArrayList<>();

        cursor.moveToFirst();

        double latitude;
        double longitude;

        while (!cursor.isAfterLast()) {
            latitude = Double.parseDouble(cursor.getString(0));
            longitude = Double.parseDouble(cursor.getString(1));

            LatLng latLng = new LatLng(latitude, longitude);
            returnLst.add(latLng);

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
        db.close();
        cursor.moveToFirst();
        float totLen=0;
        while(!cursor.isAfterLast()) {
            totLen = Float.parseFloat(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return totLen;
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