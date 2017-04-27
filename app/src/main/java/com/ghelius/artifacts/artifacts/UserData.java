package com.ghelius.artifacts.artifacts;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by eugene on 26.04.17.
 */

public abstract class UserData {

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
        int totalCount = gameStat.optInt("total", 0);
        int rightCount = gameStat.optInt("right", 0);
        return new BaseGameStatistic(totalCount, rightCount);

    }

    void updateGameStatistic(String gameKey, BaseGameStatistic statistic) {
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
            int totalCount = gameStat.optInt("total", 0);
            int rightCount = gameStat.optInt("right", 0);
            totalCount += statistic.totalAttempt;
            rightCount += statistic.successfullAttempt;
            try {
                gameStat.put("total", totalCount);
                gameStat.put("right", rightCount);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("UserData", "Something wrong with JSON object, can't put object:"
                        + e.getMessage());
            }
        } catch (JSONException e) {
            gameStat = new JSONObject();
            try {
                gameStat.put("total", statistic.totalAttempt);
                gameStat.put("right", statistic.successfullAttempt);
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
