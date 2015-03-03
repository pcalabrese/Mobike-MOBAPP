package com.mobike.mobike.utils;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andrea-PC on 04/02/2015.
 */
public class Event {
    private String name, date, creator, description, routeID, startLocation, creationDate, invited;

    public Event(String name, String date, String creator, String description, String routeID, String startLocation, String creationDate, String invited) {
        this.name = name;
        this.date = date;
        this.creator = creator;
        this.description = description;
        this.routeID = routeID;
        this.invited = invited;
        this.startLocation = startLocation;
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getCreator() {
        return creator;
    }

    public String getDescription() { return description; }

    public String getRouteID() { return routeID; }

    public String getStartLocation() { return startLocation; }

    public String getCreationDate() { return creationDate; }

    public String getInvited() { return invited; }

    public void setDescription(String desc){
        this.description = desc;
    }

    public void setRouteID(String s){
        this.routeID = s;
    }

    public void setInvited(String invited){
        this.invited = invited;
    }

    public JSONObject exportInJSON(int userID) {
        JSONObject result = new JSONObject();
        try {
            result.put("name", name);
            result.put("description", description);
            result.put("startDate", date);
            JSONObject c = new JSONObject();
            c.put("nickname", creator);
            c.put("userID", userID);
            result.put("creator", c);
            result.put("routeID", routeID);
            result.put("startLocation", startLocation);
            // creo un JSONArray con gli invitati e lo metto nel campo "invited" dell'oggetto result (solo nickname)
            JSONArray array = new JSONArray();
            String[] invitedArray = invited.split("\n");
            for (int i = 0; i < invitedArray.length; i++)
                array.put(new JSONObject().put("nickname", invitedArray[i]));

        } catch (JSONException e) {}
        return result;
    }
}