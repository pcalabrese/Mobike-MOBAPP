package com.mobike.mobike.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.mobike.mobike.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Andrea-PC on 04/02/2015.
 */
public class Event {
    public static final int NOT_INVITED = 0;
    public static final int INVITED = 1;
    public static final int ACCEPTED = 2;
    public static final int REFUSED = 3;

    private String name, id, startDate, creator, routeID, startLocation;
    private int acceptedSize, invitedSize, refusedSize, state;

    // variabili per l'upload dell'evento
    private String description, creationDate, participants;


    public Event(String name, String id, String startDate, String creator, String routeID, String startLocation, int acceptedSize, int invitedSize, int refusedSize, int state) {
        this.name = name;
        this.id = id;
        this.startDate = startDate;
        this.creator = creator;
        this.routeID = routeID;
        this.startLocation = startLocation;
        this.acceptedSize = acceptedSize;
        this.invitedSize = invitedSize;
        this.refusedSize = refusedSize;
        this.state = state;
    }

    public Event(String name, String description, String creator, String startDate, String startLocation, String creationDate, String routeID, String participants) {
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.startDate = startDate;
        this.startLocation = startLocation;
        this.creationDate = creationDate;
        this.routeID = routeID;
        this.participants = participants;
    }

    public String getName() {
        return name;
    }

    public String getId() { return id; }

    public String getStartDate() {
        return startDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getRouteID() { return routeID; }

    public String getStartLocation() { return startLocation; }

    public int getAcceptedSize() { return acceptedSize; }

    public int getInvitedSize() { return invitedSize; }

    public int getRefusedSize() { return refusedSize; }

    public int getState() { return state; }

    public int getColorState() {
        switch (state) {
            case NOT_INVITED:
                return R.color.material_grey;
            case INVITED:
                return R.color.colorAccent;
            case ACCEPTED:
                return R.color.material_green;
            case REFUSED:
                return R.color.material_red;
        }
        return R.color.material_grey;
    }

    public JSONObject exportInJSON(int userID) {
        JSONObject result = new JSONObject();
        try {
            result.put("name", name);
            result.put("description", description);
            JSONObject c = new JSONObject();
            c.put("nickname", creator);
            c.put("userID", userID);
            result.put("creator", c);
            result.put("startDate", startDate);
            result.put("startLocation", startLocation);
            result.put("creationDate", creationDate);
            result.put("routeID", routeID);

            // creo un JSONArray con gli invitati e lo metto nel campo "participants" dell'oggetto result (solo nickname)
            JSONArray array = new JSONArray();
            String[] invitedArray = participants.split("\n");
            for (int i = 0; i < invitedArray.length; i++)
                array.put(new JSONObject().put("nickname", invitedArray[i]));
            result.put("participants", array);
        } catch (JSONException e) {}
        return result;
    }
}