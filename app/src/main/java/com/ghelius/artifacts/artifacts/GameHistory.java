package com.ghelius.artifacts.artifacts;

import java.util.ArrayList;

/**
 * Created by eugene on 12.09.17.
 * Represent one game step image info
 */

public class GameHistory {

    ArrayList<GameHistoryItem> items;


    public class GameHistoryItem {
        ArrayList<Picture> pictures;
        boolean success;

        GameHistoryItem(ArrayList<Picture> pictures, boolean success) {
            this.pictures = pictures;
            this.success = success;
        }
    }


    public void addItem(GameHistoryItem item) {
        items.add(item);
    }

    public void clear() {
        items.clear();
    }

    public int size() {
        return items.size();
    }

    GameHistory() {
        items = new ArrayList<>();
    }
}

