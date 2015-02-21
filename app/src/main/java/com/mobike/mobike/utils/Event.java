package com.mobike.mobike.utils;

/**
 * Created by Andrea-PC on 04/02/2015.
 */
public class Event {
    private String name, date, creator, description, invited, routeID;

    public Event(String name, String date, String creator, String description, String routeID, String invited) {
        this.name = name;
        this.date = date;
        this.creator = creator;
        this.description = description;
        this.routeID = routeID;
        this.invited = invited;
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
}