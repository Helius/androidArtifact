package com.ghelius.artifacts.artifacts;

import java.util.ArrayList;

/**
 * Created by eugene on 12.09.17.
 * Represent one game step image info
 */

public class GameHistory {

    private ArrayList<GameHistoryItem> items;
    private static GameHistory history;
    private static final int HistoryCount = 30;

    static public class GameHistoryItem {
        String img_path;
        boolean success;

        GameHistoryItem(Picture picture, boolean success) {
            this.img_path = picture.path;
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

