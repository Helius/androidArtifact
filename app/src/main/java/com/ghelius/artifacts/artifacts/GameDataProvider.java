package com.ghelius.artifacts.artifacts;

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
    private static GameDataProvider instance_;
    private boolean initialized = false;

    private ArrayList<DataChangedListener> mDataChangedListeners = new ArrayList<>();



    interface DataChangedListener {
        void dataChanged();
    }

    public void addDataChangedListener(DataChangedListener listener) {
        mDataChangedListeners.add(listener);
    }

    public void removeDataChangedListener(DataChangedListener listener) {
        mDataChangedListeners.remove(listener);
    }


    public static GameDataProvider instance() {
        if (instance_ == null) {
            instance_ = new GameDataProvider();
        }
        return instance_;
    }

    public void initialize(byte[] bytes, int level) throws JSONException {
        this.level = level;
        JSONObject db_data = new JSONObject(new String(bytes));


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
        initialized = true;
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
    }

    public void setLevel(int level) {
        if (this.level != level) {
            this.level = level;
            createFilteredCollections();
        }
        for (DataChangedListener listener: mDataChangedListeners) {
            listener.dataChanged();
        }
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

    public boolean initialized() {
        return initialized;
    }

    public final Picture getPictureByPath(String img_path) {
        for (final Picture pic: pictures) {
            if (pic.path.equals(img_path)) {
                return pic;
            }
        }
        return null;
    }
}
