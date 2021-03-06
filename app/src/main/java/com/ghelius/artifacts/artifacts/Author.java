package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

@IgnoreExtraProperties
public class Author {
    public int id;
    public int movement_id;
    private String name_en;
    private String name_ru;
    private String years;


    public Author(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getInt("id");
        this.movement_id = jsonObject.getInt("movement_id");
        this.name_en = jsonObject.getString("name_en");
        this.name_ru = jsonObject.getString("name_ru");
        this.years = jsonObject.getString("years");
    }

    public String getName() {
        return getName(Locale.getDefault().getLanguage());
    }

    public String getYears() {
        return years;
    }

    public String getName(String lang) {
        return lang.equals("ru") ? name_ru : name_en;
    }

    public static String authorsToString(ArrayList<Author> authors, int count) {
        String res = "";
        for (int i = 0; i < authors.size() && i < count; ++i) {
            res += authors.get(i).getName();
            if (i < authors.size()-1 && i < count-1) {
                res += ", ";
            }
        }
        return res;
    }

    public static class AuthorNameComparator implements Comparator<Author> {
        public int compare(Author a1, Author a2) {
            return a1.getName().compareTo(a2.getName());
        }
    }
}
