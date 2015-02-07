package com.mobike.mobike.utils;

import android.graphics.Bitmap;

/**
 * Created by Andrea-PC on 07/02/2015.
 */
public class Route {
    private String name, description, creator, length, duration, gpx;
    private Bitmap map;

    public Route(String name, String description, String creator, String length, String duration, Bitmap map, String gpx) {
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.length = length;
        this.duration = duration;
        this.map = map;
        this.gpx = gpx;
    }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getCreator() { return creator; }

    public String getLength() { return length; }

    public String getDuration() { return duration; }

    public Bitmap getMap() { return map; }

    public String getGpx() { return gpx; }
}
