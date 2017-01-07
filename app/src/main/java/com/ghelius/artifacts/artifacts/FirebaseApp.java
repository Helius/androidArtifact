package com.ghelius.artifacts.artifacts;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        /* Enable disk persistence  */
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
