package com.ghelius.artifacts.artifacts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="mainActivity";
    ChooseAuthorGameFragment chooseAuthorGameFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseAuthorGameFragment = new ChooseAuthorGameFragment();
        getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_holder, chooseAuthorGameFragment).commit();

    }

}
