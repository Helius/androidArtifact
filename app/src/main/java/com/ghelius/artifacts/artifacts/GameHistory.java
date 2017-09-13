package com.ghelius.artifacts.artifacts;

import java.util.ArrayList;

/**
 * Created by eugene on 12.09.17.
 * Represent one game step image info
 */

public class GameHistory {

    private final GameDataProvider dataProvider;
    private ArrayList<GameHistoryItem> items;

    public GameHistoryItem getItem(int i) {
        return items.get(i);
    }


    static public class GameHistoryItem {
        Picture picture;
        boolean success;
        private Author author;
        private Movement movement;

        GameHistoryItem(Picture picture, boolean success) {
            this.picture = picture;
            this.success = success;
        }

        public Author getAuthor() {
            return author;
        }

        public void setAuthor(Author author) {
            this.author = author;
        }

        public Movement getMovement() {
            return movement;
        }

        public void setMovement(Movement movement) {
            this.movement = movement;
        }
    }


    public void addItem(GameHistoryItem item) {
        item.setAuthor(dataProvider.getAuthorById(item.picture.author));
        item.setMovement(dataProvider.getMovementById(item.picture.movement_id));
        items.add(0,item);
    }

    public void clear() {
        items.clear();
    }

    public int size() {
        return items.size();
    }

    GameHistory(GameDataProvider gameDataProvider) {
        items = new ArrayList<>();
        this.dataProvider = gameDataProvider;
    }
}

