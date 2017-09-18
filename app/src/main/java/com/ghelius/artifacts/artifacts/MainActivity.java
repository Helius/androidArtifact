package com.ghelius.artifacts.artifacts;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "mainActivity";
    private FirebaseAnalytics mFirebaseAnalytics;
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
    private StorageReference mStorageRef;

    private TextView sbMainText;
    private GalleryFragment galleryFragment;

    public void logEvent(String event) {
        logEvent(event, null);
    }

    public void logEvent(String event, String value) {
        Bundle bundle = new Bundle();
        if (value != null && value.length() > 0) {
            bundle.putString("aValue", value);
        }
        mFirebaseAnalytics.logEvent(event, bundle);
    }

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
            logEvent("OpenLevelDialogWith", String.valueOf(userData.getLevel()));
            chooseLevelDialog.init(getUserData());
            chooseLevelDialog.show(getSupportFragmentManager(), "level_dialog");
        } else if (id == R.id.nav_favorites) {
        //TODO: open favorites screen with list and zero-screen
        } else if (id == R.id.nav_history) {
            HistoryFragment historyFragment = (HistoryFragment) getSupportFragmentManager().findFragmentByTag("history");
            if (historyFragment == null) {
                historyFragment = new HistoryFragment();
            }
            final MainMenuFragment mainMenuFragment = (MainMenuFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.main_menu_fragment);
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(mainMenuFragment)
                    .replace(R.id.main_fragment_holder, historyFragment)
                    .addToBackStack("history").commit();
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
        } else if (id == R.id.nav_about) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Helius/androidArtifact"));
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
                            chooseAuthorGameFragment.setServerResources(getUserData(), mainMenuFragment.getGameDataProvider());
                        }
                        getSupportFragmentManager().beginTransaction()
                                .hide(mainMenuFragment)
                                .replace(R.id.main_fragment_holder, chooseAuthorGameFragment)
                                .addToBackStack("game").commit();
                        logEvent("ChooseAuthorGameStarted");
                        break;
                    case 1:
                        if (typeAuthorGameFragment == null) {
                            typeAuthorGameFragment = new TypeAuthorGameFragment();
                            typeAuthorGameFragment.setServerResources(getUserData(), mainMenuFragment.getGameDataProvider());
                        }
                        getSupportFragmentManager().beginTransaction()
                                .hide(mainMenuFragment)
                                .replace(R.id.main_fragment_holder, typeAuthorGameFragment)
                                .addToBackStack("game").commit();
                        logEvent("TypeAuthorGameStarted");
                        break;
                    case 2:
                        if (choosePaintGameFragment == null) {
                            choosePaintGameFragment = new ChoosePaintGameFragment();
                            choosePaintGameFragment.setServerResources(getUserData(), mainMenuFragment.getGameDataProvider());
                        }

                        getSupportFragmentManager().beginTransaction()
                                .hide(mainMenuFragment)
                                .replace(R.id.main_fragment_holder, choosePaintGameFragment)
                                .addToBackStack("game").commit();
                        logEvent("ChoosePaintGameStarted");
                        break;
                    case 3:
                        if (chooseMovementGameFragment == null) {
                            chooseMovementGameFragment = new ChooseMovementGameFragment();
                            chooseMovementGameFragment.setServerResources(getUserData(), mainMenuFragment.getGameDataProvider());
                        }

                        getSupportFragmentManager().beginTransaction()
                                .hide(mainMenuFragment)
                                .replace(R.id.main_fragment_holder, chooseMovementGameFragment)
                                .addToBackStack("game").commit();
                        logEvent("ChooseMovementGameStarted");
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
//        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("content");
//        scoresRef.keepSynced(true);

        FirebaseStorage.getInstance().setMaxDownloadRetryTimeMillis(10000);
        mStorageRef = FirebaseStorage.getInstance().getReference();


        findViewById(R.id.main_progress_fade).setVisibility(View.VISIBLE);
        updateSideBarInfo();

        chooseLevelDialog = (ChooseLevelDialog) getSupportFragmentManager().findFragmentByTag("level_dialog");
        if (chooseLevelDialog != null) {
            chooseLevelDialog.init(getUserData());
        }
        loadGameData(mainMenuFragment);
    }

    private void loadGameData(final MainMenuFragment holder) {
        if (!hasInternet()) {
            showInternetDialog();
            logEvent("NoInternetDialogShowed");
            return;
        }
        if (holder.getGameDataProvider() != null) {
            findViewById(R.id.main_progress_fade).setVisibility(View.GONE);
            return;
        }
        final long ONE_MEGABYTE = 1024 * 1024;
        StorageReference fileRef = mStorageRef.child("out_db.json");
        fileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    holder.setGameDataProvider(new GameDataProvider(bytes, userData.getLevel()));
                    findViewById(R.id.main_progress_fade).setVisibility(View.GONE);
                } catch (JSONException e) {
                    Log.d(TAG, "Can't parse json db");
                    logEvent("LoadDbDataError");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG,"can't download db from storage");
                logEvent("FailToDownloadDbData");
            }
        });
    }

    private boolean hasInternet () {
        ConnectivityManager cm = (ConnectivityManager)getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }


    private void showInternetDialog() {
        final AppCompatActivity activity = this;
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(getString(R.string.internet_dialog_title));
        alertDialog.setMessage(getString(R.string.internet_dialog_message));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.finish();
                    }
                });
        alertDialog.show();
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
                    final MainMenuFragment mainMenuFragment = (MainMenuFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.main_menu_fragment);
                    mainMenuFragment.getGameDataProvider().setLevel(getLevel());
                    logEvent("LevelChangedTo", String.valueOf(getLevel()));
                    if (chooseAuthorGameFragment != null)
                        chooseAuthorGameFragment.setServerResources(getUserData(), mainMenuFragment.getGameDataProvider());
                    if (chooseMovementGameFragment != null)
                        chooseMovementGameFragment.setServerResources(getUserData(), mainMenuFragment.getGameDataProvider());
                    if (choosePaintGameFragment != null)
                        choosePaintGameFragment.setServerResources(getUserData(), mainMenuFragment.getGameDataProvider());
                    if (typeAuthorGameFragment != null)
                        typeAuthorGameFragment.setServerResources(getUserData(), mainMenuFragment.getGameDataProvider());

                }
            };
        }
        return userData;
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
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
        logEvent("ScreenOrientation", newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? "Landscape" : "Portrait");
    }

}
