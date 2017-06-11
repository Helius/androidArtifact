package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by eugene on 29.11.16.
 */

@IgnoreExtraProperties
public class Picture  {
    public int author;
    public int level;
    public int movement_id;
    public String path;


    public Picture(JSONObject jsonObject) throws JSONException {
        this.author = jsonObject.getInt("author");
        this.level = jsonObject.getInt("level");
        this.movement_id = jsonObject.getInt("movement_id");
        this.path = jsonObject.getString("path");
    }
}
