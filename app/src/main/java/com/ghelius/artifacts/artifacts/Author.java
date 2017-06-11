package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by eugene on 01.12.16.
 */

@IgnoreExtraProperties
public class Author {
    public int id;
    public int movement_id;
    public String name_en;
    public String name_ru;


    public Author(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getInt("id");
        this.movement_id = jsonObject.getInt("movement_id");
        this.name_en = jsonObject.getString("name_en");
        this.name_ru = jsonObject.getString("name_ru");
    }
}
