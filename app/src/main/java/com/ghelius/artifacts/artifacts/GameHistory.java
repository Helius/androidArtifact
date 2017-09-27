package com.ghelius.artifacts.artifacts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by eugene on 12.09.17.
 * Represent one game step image info
 */

public class GameHistory {

    private ArrayList<GameHistoryItem> items;
    private static GameHistory history;
    private static final int HistoryCount = 30;

    public void load(String history) {
        items.clear();
        try {
            JSONArray arr = new JSONArray(history);
            for (int i = 0; i < arr.length(); ++i ) {
                JSONObject o = arr.getJSONObject(i);
                items.add(new GameHistoryItem(o.getString("path"), o.getBoolean("res")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String save() {
        JSONArray arr = new JSONArray();
        for(GameHistoryItem i : items) {
            JSONObject item = new JSONObject();
            try {
                item.put("path", i.img_path);
                item.put("res", i.success);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            arr.put(item);
        }
        return arr.toString();
    }

    static public class GameHistoryItem {
        String img_path;
        boolean success;

        GameHistoryItem(Picture picture, boolean success) {
            this.img_path = picture.path;
            this.success = success;
        }

        GameHistoryItem(String path, boolean success) {
            this.img_path = path;
            this.success = success;
        }
    }

    public GameHistoryItem getItem(int i) {
        return items.get(i);
    }

    public void addItem(GameHistoryItem item) {
        items.add(0,item);
        if (history.size() > HistoryCount) {
            items.remove(history.size()-1);
        }
    }

    public int size() {
        return items.size();
    }

    private GameHistory() {
        items = new ArrayList<>();
    }

    public static GameHistory instance() {
        if (history == null) {
            history = new GameHistory();
        }
        return history;
    }
}

