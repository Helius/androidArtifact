package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by eugene on 29.11.16.
 */

@IgnoreExtraProperties
public class Picture  {
    public int author;
    public int level;
    public int movement_id;
    public String path;
    public String year;
    private HashMap<String, String> name = new HashMap<>();
    private HashMap<String, String> link = new HashMap<>();
    private HashMap<String, String> holder = new HashMap<>();

    HashMap parseLangArray(JSONArray arr) {
        HashMap map = new HashMap<String, String>();
        if (arr == null)
            return map;
        for (int i = 0; i < arr.length(); ++i) {
            try {
                JSONObject lan = arr.getJSONObject(i);
                if (lan != null && lan.keys().hasNext()) {
                    String key = lan.keys().next();
                    map.put(key, lan.getString(key));
                }
            } catch (Exception e) {
            }
        }
        return map;
    }

    public String getName() {
        return name.get(Locale.getDefault().getLanguage().equals("ru") ? "ru" : "en");
    }
    public String getLink() {
        return link.get(Locale.getDefault().getLanguage().equals("ru") ? "ru" : "en");
    }
    public String getHolder() {
        return holder.get(Locale.getDefault().getLanguage().equals("ru") ? "ru" : "en");
    }

    public Picture(JSONObject jsonObject) throws JSONException {
        this.author = jsonObject.getInt("author");
        this.level = jsonObject.getInt("level");
        this.movement_id = jsonObject.getInt("movement_id");
        this.path = jsonObject.getString("path");
        try {
            this.year = jsonObject.getString("date");
        } catch (JSONException e) {
        }
        try {
            name = parseLangArray(jsonObject.getJSONArray("name"));
        } catch (JSONException e) {
        }
        try {
            link = parseLangArray(jsonObject.getJSONArray("link"));
        } catch (JSONException e) {
        }
        try {
            holder = parseLangArray(jsonObject.getJSONArray("holder"));
        } catch (JSONException e) {
        }
    }
    public Picture () {

    }
}
