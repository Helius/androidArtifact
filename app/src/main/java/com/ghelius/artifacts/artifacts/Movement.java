package com.ghelius.artifacts.artifacts;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Movement {
    public Long id;
    public String name_en;
    public String name_ru;


    public Movement() {}

    public Movement(Long id, String name_en, String name_ru) {
        this.id = id;
        this.name_en = name_en;
        this.name_ru = name_ru;
    }
}
