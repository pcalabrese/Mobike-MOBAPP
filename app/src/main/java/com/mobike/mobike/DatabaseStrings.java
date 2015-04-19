package com.mobike.mobike;

/**
 * Created by marco on 22/12/14.
 *
 * Questa classe esiste solo per questioni organizzative del codice. Qui sono definiti i nomi delle
 * colonne e delle tabelle. Tenendole qui ed importandole come costanti, se dovessero cambiare
 * il resto del codice non necessiterebbe di modifiche
 */
public class DatabaseStrings {

    public static final String TABLELOC="locations";
    public static final String TABLEPOI="pointsofint";
// The followings are the names of the locations table fields
    public static final String FIELD_ID = "order_id";
    public static final String FIELD_LAT = "latitude";
    public static final String FIELD_LNG = "longitude";
    public static final String FIELD_TIME = "instant";
    public static final String FIELD_ALT = "altitude";
    public static final String FIELD_DIST = "distance";
    // The followings are the names of the pointsofint table fields
    public static final String FIELD_ID_POI = "id";
    public static final String FIELD_LAT_POI = "latitude";
    public static final String FIELD_LNG_POI = "longitude";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_CAT = "category";
}
