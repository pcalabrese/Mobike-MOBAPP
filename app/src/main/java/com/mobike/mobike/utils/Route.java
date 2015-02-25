package com.mobike.mobike.utils;

import android.graphics.Bitmap;

/**
 * Created by Andrea-PC on 07/02/2015.
 */
public class Route {
    private String name, description, creator, length, duration, gpx, difficulty, bends, type, id;
    private Bitmap map;

    public Route(String name, String description, String creator, String length, String duration, Bitmap map, String gpx, String difficulty, String bends, String type, String id) {
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.length = length;
        this.duration = duration;
        this.map = map;
        this.gpx = gpx;
        this.difficulty = difficulty;
        this.bends = bends;
        this.type = type;
        this.id = id;
    }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getCreator() { return creator; }

    public String getLength() { return length; }

    public String getDuration() { return duration; }

    public Bitmap getMap() { return map; }

    public String getGpx() { return gpx; }

    public String getDifficulty() { return difficulty; }

    public String getBends() { return bends; }

    public String getType() { return type; }

    public String getID() { return id; }

    public void setID(String id) {
        this.id = id;
    }
}
