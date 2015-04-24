package com.mobike.mobike.utils;

/**
 * Created by Andrea-PC on 23/04/2015.
 */
public class POI {
    public static final String RESTAURANT = "Restaurant";
    public static final String VIEWPOINT = "Viewpoint";
    public static final String GAS_STATION = "Gas Station";
    public static final String OTHER = "Other";

    public static int stringToIntType(String type) {
        if (type.equals(VIEWPOINT))
            return 0;
        else if (type.equals(GAS_STATION))
            return 1;
        else if (type.equals(RESTAURANT))
            return 2;
        else
            return 3;
    }

    public static String intToStringType(int type) {
        switch (type) {
            case 0:
                return VIEWPOINT;
            case 1:
                return GAS_STATION;
            case 2:
                return RESTAURANT;
            case 3:
                return OTHER;
        }
        return OTHER;
    }
}
