package com.ghelius.artifacts.artifacts;

/**
 * Created by eugene on 26.04.17.
 */
public class BaseGameStatistic {
    int trueAttempt = 0;
    int falseAttempt = 0;

    public BaseGameStatistic() {
        this.trueAttempt = 0;
        this.falseAttempt = 0;
    }

    public BaseGameStatistic(int trueCount, int falseCount) {
        this.trueAttempt = trueCount;
        this.falseAttempt = falseCount;
    }

    public void addAttempt(boolean attempt) {
        if (attempt) {
            this.trueAttempt++;
        } else {
            this.falseAttempt++;
        }
    }
}
