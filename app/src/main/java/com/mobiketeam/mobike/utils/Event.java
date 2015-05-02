package com.mobiketeam.mobike.utils;

import android.util.Log;

import com.mobiketeam.mobike.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Andrea-PC on 04/02/2015.
 */
public class Event {
    public static final int NOT_INVITED = 0;
    public static final int INVITED = 1;
    public static final int ACCEPTED = 2;
    public static final int REFUSED = 3;

    private static final String TAG = "Event";

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

    //costruttore per l'upload del nuovo evento
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

    public JSONObject exportInJSON(int userID, HashMap<String, Integer> usersMap) {
        JSONObject result = new JSONObject(), event = new JSONObject(), user = new JSONObject();
        Crypter crypter = new Crypter();

        try {
            event.put("name", name);
            event.put("description", description);
            JSONObject c = new JSONObject();
            c.put("nickname", creator);
            c.put("id", userID);
            event.put("owner", c);
            event.put("startdate", startDate);
            event.put("startlocation", startLocation);
            event.put("creationdate", creationDate);
            JSONObject route = new JSONObject();
            route.put("id", routeID);
            event.put("route", route);

            // creo un JSONArray con gli invitati e lo metto nel campo "participants" dell'oggetto result (solo nickname)
            JSONArray array = new JSONArray();
            String[] invitedArray = participants.split("\n");
            for (int i = 0; i < invitedArray.length; i++) {
                JSONObject invited = new JSONObject();
                invited.put("id", usersMap.get(invitedArray[i]));
                invited.put("nickname", invitedArray[i]);
                array.put(invited);
            }
            event.put("usersInvited", array);

            user.put("id", userID);
            user.put("nickname", creator);
            result.put("event", crypter.encrypt(event.toString()));
            result.put("user", crypter.encrypt(user.toString()));
        } catch (JSONException e) {}

        Log.v(TAG, "event: " + event.toString() + "\nuser: " + user.toString());

        return result;
    }
}