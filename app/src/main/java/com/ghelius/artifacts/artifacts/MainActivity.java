package com.ghelius.artifacts.artifacts;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="mainActivity";
    ChooseAuthorGameFragment chooseAuthorGameFragment;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        MainMenuFragment mainMenuFragment = new MainMenuFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_fragment_holder, mainMenuFragment)
                .commit();

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    if (actionBar != null) {
                    }
                }
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                }
                return true;
            }
            return super.onOptionsItemSelected(item);
    }

    public void launchChooseTypeAuthor(View view) {
        Toast.makeText(getApplicationContext(), "Sorry! not implemented yet!", Toast.LENGTH_SHORT).show();
    }

    public void launchChooseMovements(View view) {
        Toast.makeText(getApplicationContext(), "Sorry! not implemented yet!", Toast.LENGTH_SHORT).show();
    }

    public void launchChoosePaint(View view) {
        Toast.makeText(getApplicationContext(), "Sorry! not implemented yet!", Toast.LENGTH_SHORT).show();
    }

    public void launchChooseAuthor(View view) {
        chooseAuthorGameFragment = new ChooseAuthorGameFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment_holder, chooseAuthorGameFragment)
                .addToBackStack("game").commit();
    }
}
