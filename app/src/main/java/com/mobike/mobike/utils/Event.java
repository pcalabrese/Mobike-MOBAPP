package com.mobike.mobike.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Andrea-PC on 04/02/2015.
 */
public class Event {
    private String name, date, creator, description, invited, routeID, startLocation, creationDate;

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

    public Event(String name, String date, String creator){
        this.name = name;
        this.date = date;
        this.creator = creator;
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

    public JSONObject exportInJSON() {
        JSONObject result = new JSONObject();
        try {
            result.put("name", name);
            result.put("description", description);
            result.put("date", date);
            result.put("creator", creator);
            result.put("routeId", routeID);
            // creo un JSONArray con gli invitati e lo metto nel campo "invited" dell'oggetto result (solo nomi, o nickname)
        } catch (JSONException e) {}
        return result;
    }
}