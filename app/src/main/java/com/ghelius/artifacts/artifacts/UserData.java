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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    BaseGameStatistic getGameStatistic(int gameId) {
        JSONArray stats;
        try {
            stats = data.getJSONArray("stats");
            if (stats.length() > 0) {
                for (int i = 0; i < stats.length(); i++) {
                    JSONObject gameStat = stats.getJSONObject(i);
                    if (gameStat.optInt("gameId", -1) == gameId) {
                        int totalCount = gameStat.optInt("total", 0);
                        int rightCount = gameStat.optInt("right", 0);
                        return new BaseGameStatistic(totalCount, rightCount);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new BaseGameStatistic();
    }

    void updateGameStatistic(int gameId, BaseGameStatistic statistic) {
        JSONArray stats;
        JSONObject gameStat = null;
        try {
            stats = data.getJSONArray("stats");
        } catch (JSONException e) {
            stats = new JSONArray();
            try {
                data.put("stats", stats);
            } catch (JSONException e1) {
                e1.printStackTrace();
                Log.e("UserData", "Something wrong with JSON object, can't put array:"
                        + e1.getMessage());
            }
        }

        if (stats.length() > 0) {
            for (int i = 0; i < stats.length(); i++) {
                try {
                    JSONObject tmpGameStat = stats.getJSONObject(i);
                    if (tmpGameStat.optInt("gameId", -1) == gameId) {
                        gameStat = tmpGameStat;
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // there is no certain game
        if (gameStat == null) {
            gameStat = new JSONObject();
            try {
                gameStat.put("gameId", gameId);
                gameStat.put("total", statistic.totalAttempt);
                gameStat.put("right", statistic.successfullAttempt);
                stats.put(gameStat);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            int totalCount = gameStat.optInt("total", 0);
            int rightCount = gameStat.optInt("right", 0);
            try {
                totalCount += statistic.totalAttempt;
                rightCount += statistic.successfullAttempt;
                gameStat.put("total", totalCount);
                gameStat.put("right", rightCount);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        saveUserData(data);
    }

}
