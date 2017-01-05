package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by eugene on 29.11.16.
 */

@IgnoreExtraProperties
public class Picture {
    public Long author;
    public Long level;
    public Long movement_id;
    public String path;


    public Picture() {}

    public Picture(Long author, Long level, Long movement_id, String path) {
        this.author = author;
        this.level = level;
        this.movement_id = movement_id;
        this.path = path;
    }
}
