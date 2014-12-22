package com.mobike.mobike;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import static com.mobike.mobike.DatabaseStrings.*;
/**
 * Solitamente questa classe ha due caratteristiche:  contiene un riferimento all’oggetto helper
 * definito nella classe interna, e contiene i metodi con cui, dalle altre componenti dell’app,
 * verranno richieste operazioni e selezioni sui dati.
 */

public class GPSDatabase
{
    private Context context;
    private DbHelper dbHelper; //il riferimento di cui sopra

    public final String DBNAME="gpsdb";
    public final int DBVERSION=1;

    // codice SQLite per l'inizializzazione della tabella
    public final String CREATERDB="create table location(orderId integer primary key, " +
            "latitude text not null, longitude text not null, altitude text, " +
            "instant datetime default current_timestamp);";


    //constructor
    public GPSDatabase(Context context){
        this.context=context;
        dbHelper=new DbHelper(context);
    }


    /*
        Questa classe serve a gestire la nascita e l’aggiornamento
        del database su memoria fisica e a recuperare un riferimento all’oggetto SQLiteDatabase,
         usato come accesso ai dati;
     */

    public class DbHelper extends SQLiteOpenHelper {

        //constructor
        public DbHelper(Context context){
            super(context,DBNAME,null,DBVERSION);
        }

        /*
            Questo metodo viene invocato una volta sola, cioe' quando
            non esiste un db con nome DBNAME, ed esegue il codice "raw" contenuto in CREATERDB
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATERDB);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // NOT TODO Auto-generated method stub
        }
    }

    /*
        Questo metodo... vabbe' si capisce
     */
    public long insertRow(double lat, double lng, double alt)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues value=new ContentValues();
        value.put(FIELD_LAT, lat + "");
        value.put(FIELD_LNG, lng + "");
        value.put(FIELD_ALT, alt + "");
        db.insert(TABLENAME,null,value);
        try
        {
            return db.insert(DatabaseStrings.TABLENAME, null, value);
        }
        catch (SQLiteException sqle)
        {
            // Gestione delle eccezioni
        }
        return 0;
    }


    //non so ancora se questo metodo servirà, quindi lo lascio commentato
    /*public Cursor getAllRows(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(TABLENAME,
                new String[]{FIELD_ID,FIELD_LAT,FIELD_LNG,FIELD_ALT, FIELD_TIME}, null,null, null, null, null);
    }*/

    /*
     * Semplicemente crea un riferimento al db
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