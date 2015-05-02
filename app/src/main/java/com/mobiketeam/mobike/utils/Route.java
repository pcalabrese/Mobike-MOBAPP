package com.mobiketeam.mobike.utils;

import com.mobiketeam.mobike.R;

/**
 * Created by Andrea-PC on 07/02/2015.
 */
public class Route {
    private String name, id, description, creator, length, duration, gpx, difficulty, bends, type, thumbnailURL, startLocation, endLocation;
    private int rating, ratingNumber;
    public static final String MOUNTAIN = "Mountain";
    public static final String PLAIN = "Plain";
    public static final String COAST = "Coast";
    public static final String HILL = "Hill";
    public static final String MIXED = "Mixed";

    public Route(String name, String id, String description, String creator, String length, String duration, String difficulty, String bends, String type, String thumbnailURL, String startLocation, String endLocation, int rating, int ratingNumber) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.creator = creator;
        this.length = length;
        this.duration = duration;
        this.difficulty = difficulty;
        this.bends = bends;
        this.type = type;
        this.thumbnailURL = thumbnailURL;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.rating = rating;
        this.ratingNumber = ratingNumber;
    }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getCreator() { return creator; }

    public String getLength() { return length; }

    public String getDuration() { return duration; }

    public String getThumbnailURL() { return thumbnailURL; }

    public String getDifficulty() { return difficulty; }

    public String getBends() { return bends; }

    public String getType() { return type; }

    public int getTypeColor() {
        String t = type;

        if (t.equals(MOUNTAIN))
            return R.color.routeMountain;
        else if (t.equals(PLAIN))
            return R.color.routePlain;
        else if (t.equals(HILL))
            return R.color.routeHill;
        else if (t.equals(COAST))
            return R.color.routeCoast;
        else if (t.equals(MIXED))
            return R.color.routeMixed;

        return R.color.routeMixed;
    }

    public String getID() { return id; }

    public String getStartLocation() { return startLocation; }

    public String getEndLocation() { return endLocation; }

    public int getRating() { return rating; }

    public int getRatingNumber() { return ratingNumber; }

    public static int getStaticTypeColor(String type) {
        String t = type.toLowerCase();

        if (t.equals(MOUNTAIN))
            return R.color.routeMountain;
        else if (t.equals(PLAIN))
            return R.color.routePlain;
        else if (t.equals(HILL))
            return R.color.routeHill;
        else if (t.equals(COAST))
            return R.color.routeCoast;
        else if (t.equals(MIXED))
            return R.color.routeMixed;

        return R.color.routeMixed;
    }
}
