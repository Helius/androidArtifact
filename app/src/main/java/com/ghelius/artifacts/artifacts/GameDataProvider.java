package com.ghelius.artifacts.artifacts;


import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class GameDataProvider {
    static private final String TAG = "GameDataProvider";
    private ArrayList<Movement> movements;
    private ArrayList<Author> authors;
    private ArrayList<Picture> pictures;
    private ArrayList<Movement> movements_leveled = new ArrayList<>();
    private ArrayList<Author> authors_leveled = new ArrayList<>();
    private ArrayList<Picture> pictures_leveled = new ArrayList<>();
    private int level = 0;

    public GameDataProvider(byte[] bytes) throws JSONException {
        JSONObject db_data = new JSONObject(new String(bytes));

//        Log.d(TAG, "start updating game data");
        pictures = new ArrayList<>();
        try {
            JSONArray array = db_data.getJSONObject("content").getJSONArray("pictures");
            for (int i = 0; i < array.length(); i++) {
                pictures.add(new Picture(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        authors = new ArrayList<>();
        try {
            JSONArray array = db_data.getJSONObject("content").getJSONArray("authors");
            for (int i = 0; i < array.length(); i++) {
                authors.add(new Author(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        movements = new ArrayList<>();
        try {
            JSONArray array = db_data.getJSONObject("content").getJSONArray("movements");
            for (int i = 0; i < array.length(); i++) {
                movements.add(new Movement(array.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        createFilteredCollections();
    }

    void createFilteredCollections() {
        pictures_leveled.clear();
        for (Picture p: pictures) {
            if (p.level <= level + 1) {
                pictures_leveled.add(p);
            }
        }
        authors_leveled.clear();
        for(Author a: authors) {
            for(Picture p: pictures_leveled) {
                if (p.author == a.id) {
                    authors_leveled.add(a);
                    break;
                }
            }
        }
        movements_leveled.clear();
        for(Movement m: movements) {
            for(Picture p: pictures_leveled) {
                if (m.id == p.movement_id) {
                    movements_leveled.add(m);
                    break;
                }
            }
        }
//        Log.d(TAG, "data for level: "
//                + pictures_leveled.size() + ", " +
//                + authors_leveled.size()  + ", " +
//                + movements_leveled.size()+ ", "
//        );
//        Log.d(TAG, "stop updating game data");

    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        createFilteredCollections();
    }

    public ArrayList<Movement> getMovements() {
        return movements_leveled;
    }

    public ArrayList<Author> getAuthors () {
        return authors_leveled;
    }

    public ArrayList<Picture> getPictures() {
        return pictures_leveled;
    }

    public Author getAuthorById (int id) {
        for (Author a : authors) {
            if (a.id == id) {
                return a;
            }
        }
        return null;
    }

    public Movement getMovementById (int id) {
        for (Movement a : movements) {
            if (a.id == id) {
                return a;
            }
        }
        return null;
    }

    public ArrayList<Author> getFullAuthors() {
        return authors;
    }

    public ArrayList<Picture> getFullPictures() {
        return pictures;
    }

    public ArrayList<Movement> getFullMovements() {
        return movements;
    }
}
