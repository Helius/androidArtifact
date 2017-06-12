package com.ghelius.artifacts.artifacts;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public abstract class UserData {

    private static final String TrueKey = "true";
    private static final String FalseKey = "false";
    JSONObject data = null;

    abstract boolean saveUserData(JSONObject data);
    abstract JSONObject loadUserData();
    abstract void onLevelChanged();

    public UserData() {
        data = loadUserData();
    }

    public int getLevel() {
        return data.optInt("level", 0);
    }

    public void setLevel(int level) {
        try {
            data.put("level", level);
            saveUserData(data);
            onLevelChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    BaseGameStatistic getGameStatistic(String gameKey) {
        JSONObject gameStat;
        try {
            gameStat = data.getJSONObject("stats").getJSONObject("level" + getLevel()).getJSONObject(gameKey);
        } catch (JSONException e) {
            return new BaseGameStatistic();
        }
        int trueCount = gameStat.optInt(TrueKey, 0);
        int falseCount = gameStat.optInt(FalseKey, 0);
        return new BaseGameStatistic(trueCount, falseCount);

    }

    void updateGameStatistic(String gameKey, Picture pic, boolean result) {
        updateLocalGameStatistic(gameKey, result);
        pushToOnlineDb();
    }

    private void pushToOnlineDb() {
    }

    void updateLocalGameStatistic(String gameKey, boolean result) {
        JSONObject stats;
        JSONObject level;
        JSONObject gameStat;
        try {
            stats = data.getJSONObject("stats");
        } catch (JSONException e) {
            stats = new JSONObject();
            try {
                data.put("stats", stats);
            } catch (JSONException e1) {
                e1.printStackTrace();
                Log.e("UserData", "Something wrong with JSON object, can't put object:"
                        + e1.getMessage());
            }
        }

        try {
            level = stats.getJSONObject("level" + getLevel());
        } catch (JSONException e) {
            level = new JSONObject();
            try {
                stats.put("level" + getLevel(), level);
            } catch (JSONException e1) {
                e1.printStackTrace();
                Log.e("UserData", "Something wrong with JSON object, can't put object:"
                        + e1.getMessage());
            }
        }

        try {
            gameStat = level.getJSONObject(gameKey);
            int trueCount = gameStat.optInt(TrueKey, 0);
            int falseCount = gameStat.optInt(FalseKey, 0);
            if (result) {
                trueCount++;
            } else {
                falseCount++;
            }
            try {
                gameStat.put(TrueKey, trueCount);
                gameStat.put(FalseKey, falseCount);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("UserData", "Something wrong with JSON object, can't put object:"
                        + e.getMessage());
            }
        } catch (JSONException e) {
            gameStat = new JSONObject();
            try {
                if (result) {
                    gameStat.put(TrueKey, 1);
                    gameStat.put(FalseKey, 0);
                } else {
                    gameStat.put(TrueKey, 0);
                    gameStat.put(FalseKey, 1);
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
                Log.e("UserData", "Something wrong with JSON object, can't put object:"
                        + e1.getMessage());
            }
            try {
                level.put(gameKey, gameStat);
            } catch (JSONException e1) {
                e1.printStackTrace();
                Log.e("UserData", "Something wrong with JSON object, can't put object:"
                        + e1.getMessage());
            }
        }
        saveUserData(data);
    }

}
