package com.ghelius.artifacts.artifacts;

/**
 * Created by eugene on 26.04.17.
 */
public class BaseGameStatistic {
    int totalAttempt = 0;
    int successfullAttempt = 0;

    public BaseGameStatistic() {
        this.totalAttempt = 0;
        this.successfullAttempt = 0;
    }

    public BaseGameStatistic(int totalCount, int rightCount) {
        this.totalAttempt = totalCount;
        this.successfullAttempt = rightCount;
    }

    public void addRight() {
        this.successfullAttempt++;
    }

    public void addAttempt() {
        this.totalAttempt++;
    }
}
