package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

@IgnoreExtraProperties
public class Movement {
    public int id;
    public String name_en;
    public String name_ru;


    public Movement(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getInt("id");
        this.name_en = jsonObject.getString("name_en");
        this.name_ru = jsonObject.getString("name_ru");
    }
}
