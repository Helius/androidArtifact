package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

@IgnoreExtraProperties
public class Movement {
    public int id;
    private String name_en;
    private String name_ru;


    public Movement(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getInt("id");
        this.name_en = jsonObject.getString("name_en");
        this.name_ru = jsonObject.getString("name_ru");
    }

    public String getName() {
        return Locale.getDefault().getLanguage().equals("ru") ? name_ru : name_en;
    }
}
