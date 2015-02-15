package com.mobike.mobike.utils;

/**
 * Created by Andrea-PC on 04/02/2015.
 */
public class Event {
    private String name, date, creator, description, invited;
    private Route route;

    public Event(String name, String date, String creator, String description, Route route, String invited) {
        this.name = name;
        this.date = date;
        this.creator = creator;
        this.description = description;
        this.route = route;
        this.invited = invited;
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

    public Route getRoute() { return route; }

    public String getInvited() { return invited; }
}
