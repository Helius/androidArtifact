package com.ghelius.artifacts.artifacts;


import android.animation.ValueAnimator;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.test.espresso.core.deps.guava.base.Predicates;
import android.support.test.espresso.core.deps.guava.collect.Collections2;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "mainActivity";
    UserData userData;
    ChooseLevelDialog chooseLevelDialog;
    StatisticFragment statisticFragment;
    ChooseAuthorGameFragment chooseAuthorGameFragment;
    ChooseMovementGameFragment chooseMovementGameFragment;
    ChoosePaintGameFragment choosePaintGameFragment;
    TypeAuthorGameFragment typeAuthorGameFragment;
    ActionBarDrawerToggle toggle;
    ValueAnimator arrowForwardAnimation;
    ValueAnimator arrowBackAnimation;

    private FirebaseDatabase mDatabase;
    private ArrayList<Picture> pictures;
    private ArrayList<Movement> movements;
    private ArrayList<Author> authors;

    private ArrayList<Picture> pictures_leveled = new ArrayList<>();
    private ArrayList<Movement> movements_leveled = new ArrayList<>();
    private ArrayList<Author> authors_leveled = new ArrayList<>();

    private boolean picturesReady;
    private boolean authorReady;
    private boolean movementsReady;
    private TextView sbMainText;
    private GalleryFragment galleryFragment;


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_level) {
            chooseLevelDialog = (ChooseLevelDialog) getSupportFragmentManager().findFragmentByTag("level_dialog");
            if (chooseLevelDialog == null) {
                chooseLevelDialog = new ChooseLevelDialog();
            }
            chooseLevelDialog.init(getUserData());
            chooseLevelDialog.show(getSupportFragmentManager(), "level_dialog");
        } else if (id == R.id.nav_settings) {
            Log.d(TAG, "pressed nav_settings");
        } else if (id == R.id.nav_statistics) {
            statisticFragment = (StatisticFragment) getSupportFragmentManager().findFragmentByTag("statistics");
            if (statisticFragment == null) {
                statisticFragment = new StatisticFragment();
                statisticFragment.init(userData);
                final MainMenuFragment mainMenuFragment = (MainMenuFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.main_menu_fragment);
                getSupportFragmentManager().beginTransaction()
                        .hide(mainMenuFragment)
                        .replace(R.id.main_fragment_holder, statisticFragment)
                        .addToBackStack("statistic").commit();
            }

        } else if (id == R.id.nav_view) {
            Log.d(TAG, "pressed nav_view");
        } else if (id == R.id.nav_gallery) {
            galleryFragment = (GalleryFragment) getSupportFragmentManager().findFragmentByTag("gallery");
            if (galleryFragment == null) {
                galleryFragment = new GalleryFragment();
                galleryFragment.init(authors, pictures, movements);
                final MainMenuFragment mainMenuFragment = (MainMenuFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.main_menu_fragment);
                getSupportFragmentManager().beginTransaction()
                        .hide(mainMenuFragment)
                        .replace(R.id.main_fragment_holder, galleryFragment)
                        .addToBackStack("gallery").commit();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        initAnimation();
        View headerLayout = navigationView.getHeaderView(0);

        sbMainText = (TextView) headerLayout.findViewById(R.id.side_bar_main_text);


        final MainMenuFragment mainMenuFragment = (MainMenuFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_menu_fragment);
        mainMenuFragment.setMainMenuListener(new MainMenuFragment.MainMenuListener() {
            @Override
            public void menuClicked(final int number, final MainMenuFragment.GameEntry gameEntry) {
                switch (number) {
                    case 0:
                        if (chooseAuthorGameFragment == null) {
                            chooseAuthorGameFragment = new ChooseAuthorGameFragment();
                            chooseAuthorGameFragment.setServerResources(getUserData(), pictures_leveled, authors_leveled);
                        }

                        getSupportFragmentManager().beginTransaction()
                                .hide(mainMenuFragment)
                                .replace(R.id.main_fragment_holder, chooseAuthorGameFragment)
                                .addToBackStack("game").commit();

                        break;
                    case 1:
                        if (typeAuthorGameFragment == null) {
                            typeAuthorGameFragment = new TypeAuthorGameFragment();
                            typeAuthorGameFragment.setServerResources(getUserData(), pictures_leveled, authors_leveled);
                        }
                        getSupportFragmentManager().beginTransaction()
                                .hide(mainMenuFragment)
                                .replace(R.id.main_fragment_holder, typeAuthorGameFragment)
                                .addToBackStack("game").commit();
                        break;
                    case 2:
                        if (choosePaintGameFragment == null) {
                            choosePaintGameFragment = new ChoosePaintGameFragment();
                            choosePaintGameFragment.setServerResources(getUserData(), pictures_leveled, authors_leveled);
                        }

                        getSupportFragmentManager().beginTransaction()
                                .hide(mainMenuFragment)
                                .replace(R.id.main_fragment_holder, choosePaintGameFragment)
                                .addToBackStack("game").commit();
                        break;
                    case 3:
                        if (chooseMovementGameFragment == null) {
                            chooseMovementGameFragment = new ChooseMovementGameFragment();
                            chooseMovementGameFragment.setServerResources(getUserData(), pictures_leveled, movements_leveled);
                        }

                        getSupportFragmentManager().beginTransaction()
                                .hide(mainMenuFragment)
                                .replace(R.id.main_fragment_holder, chooseMovementGameFragment)
                                .addToBackStack("game").commit();
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Sorry! not implemented yet!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            toggle.syncState();
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                        arrowBackAnimation.start();
                        toggle.syncState();
                    }
                }
            });
            arrowForwardAnimation.start();
        }


        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    toggle.syncState();
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                                getSupportFragmentManager().popBackStack();
                                arrowBackAnimation.start();
                                toggle.syncState();
                            }
                        }
                    });
                    arrowForwardAnimation.start();
                } else {
                    toggle.syncState();
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            drawer.openDrawer(GravityCompat.START);
                        }
                    });
                    arrowBackAnimation.start();
                    setTitle(R.string.app_name);
                }
            }
        });

        mDatabase = FirebaseDatabase.getInstance();
        loadPictData();
        loadAuthorData();
        loadMovementsData();
        findViewById(R.id.main_progress_fade).setVisibility(View.VISIBLE);
        updateSideBarInfo();

        chooseLevelDialog = (ChooseLevelDialog) getSupportFragmentManager().findFragmentByTag("level_dialog");
        if (chooseLevelDialog != null) {
            chooseLevelDialog.init(getUserData());
        }
    }

    private UserData getUserData () {
        if (userData == null) {
            userData = new UserData() {
                @Override
                boolean saveUserData(JSONObject data) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                            edit().putString("userData", data.toString()).
                            apply();
                    return true;
                }

                @Override
                JSONObject loadUserData() {
                    String data = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userData", "");
                    Log.d(TAG, data);
                    JSONObject result;
                    try {
                        result = new JSONObject(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result = new JSONObject();
                    }
                    return result;
                }

                @Override
                void onLevelChanged() {
                    updateSideBarInfo();
                    updateGameData();
                }
            };
        }
        return userData;
    }

    private void updateGameData() {
        pictures_leveled.clear();
        for (Picture p: pictures) {
            if (p.level <= userData.getLevel() + 1) {
                pictures_leveled.add(p);
            }
        }
        authors_leveled.clear();
        for(Author a: authors) {
            for(Picture p: pictures_leveled) {
                if (p.author == a.id) {
                    authors_leveled.add(a);
                    break;
                }
            }
        }
        movements_leveled.clear();
        for(Movement m: movements) {
            for(Picture p: pictures_leveled) {
                if (m.id == p.movement_id) {
                    movements_leveled.add(m);
                    break;
                }
            }
        }
        Log.d(TAG, "data for level: "
                + pictures_leveled.size() + ", " +
                + authors_leveled.size()  + ", " +
                + movements_leveled.size()+ ", "
        );
    }

    private void updateSideBarInfo() {
        if (sbMainText != null) {
            String msg;
            switch (getUserData().getLevel()) {
                case 1:
                    msg = getString(R.string.level_1_text);
                    break;
                case 2:
                    msg = getString(R.string.level_2_text);
                    break;
                default:
                    msg = getString(R.string.level_0_text);
                    break;
            }
            sbMainText.setText(msg);
        }
    }

    private void initAnimation() {
        arrowForwardAnimation = ValueAnimator.ofFloat(0, 1);
        arrowForwardAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                toggle.onDrawerSlide(drawer, slideOffset);
            }
        });
        arrowForwardAnimation.setInterpolator(new DecelerateInterpolator());
        arrowForwardAnimation.setDuration(500);

        arrowBackAnimation = ValueAnimator.ofFloat(1, 0);
        arrowBackAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float slideOffset = (Float) valueAnimator.getAnimatedValue();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                toggle.onDrawerSlide(drawer, slideOffset);
            }
        });
        arrowBackAnimation.setInterpolator(new DecelerateInterpolator());
        arrowBackAnimation.setDuration(500);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        toggle.onConfigurationChanged(newConfig);
    }

    // game stuff ~~~

    private void loadPictData() {
        DatabaseReference dbRef = mDatabase.getReference("content").child("pictures");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                GenericTypeIndicator<ArrayList<Picture>> t = new GenericTypeIndicator<ArrayList<Picture>>() {
                };
                pictures = dataSnapshot.getValue(t);
                picturesReady = true;
                checkAllDataReady();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void loadMovementsData() {
        DatabaseReference dbRef = mDatabase.getReference("content").child("movements");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                GenericTypeIndicator<ArrayList<Movement>> t = new GenericTypeIndicator<ArrayList<Movement>>() {
                };
                movements = dataSnapshot.getValue(t);
                movementsReady = true;
                checkAllDataReady();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void loadAuthorData() {
        DatabaseReference dbRef1 = mDatabase.getReference("content").child("authors");

        dbRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                GenericTypeIndicator<ArrayList<Author>> t = new GenericTypeIndicator<ArrayList<Author>>() {
                };
                authors = dataSnapshot.getValue(t);
                authorReady = true;
                checkAllDataReady();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void checkAllDataReady() {
        Log.d(TAG, "checkAllDataReady" + (picturesReady && authorReady && movementsReady));
        if (picturesReady && authorReady && movementsReady) {
            //TODO: now we can enable game buttons
            findViewById(R.id.main_progress_fade).setVisibility(View.GONE);
            DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("content");
            scoresRef.keepSynced(true);
            updateGameData();
        }
    }
}
