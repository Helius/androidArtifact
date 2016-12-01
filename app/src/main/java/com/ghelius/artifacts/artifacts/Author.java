package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by eugene on 01.12.16.
 */

@IgnoreExtraProperties
public class Author {
    public Long id;
    public Long movement_id;
    public String name_en;
    public String name_ru;


    public Author() {}

    public Author(Long id, Long movement_id, String name_en, String name_ru) {
        this.id = id;
        this.movement_id = movement_id;
        this.name_en = name_en;
        this.name_ru = name_ru;
    }
}
