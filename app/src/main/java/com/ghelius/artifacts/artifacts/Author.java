package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by eugene on 01.12.16.
 */

@IgnoreExtraProperties
public class Author {
    public int id;
    public int movement_id;
    private String name_en;
    private String name_ru;


    public Author(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getInt("id");
        this.movement_id = jsonObject.getInt("movement_id");
        this.name_en = jsonObject.getString("name_en");
        this.name_ru = jsonObject.getString("name_ru");
    }

    public String getName() {
        return Locale.getDefault().getLanguage().equals("ru") ? name_ru : name_en;
    }

    public String getName(String lang) {
        return lang.equals("ru") ? name_ru : name_en;
    }
}
