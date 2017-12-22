package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;

public abstract class BaseGameFragment extends Fragment implements GameSetFinishedDialog.DialogEventListener, GameDataProvider.DataChangedListener {

    private HistoryFragment historyFragment;
    private View historyButton = null;
    private GameHistory gameHistory;


    public StorageReference mStorageRef;
    public GameDataProvider dataProvider = null;
    public GameSetFinishedDialog dialog;
    public UserData userData = null;
    public BaseGameStatistic sessionStatistic;
    public int gameIndex = 0;
    public int gameCount = 10;
    private ArrayList<Picture> pictureToGame;


    public BaseGameFragment() {
        // Required empty public constructor
        dataProvider = GameDataProvider.instance();
        pictureToGame = new ArrayList<>();
    }

    public void setServerResources(UserData userData) {
        this.userData = userData;
        gameHistory = GameHistory.instance();
    }

    public void addToHistory(GameHistory.GameHistoryItem item) {
        gameHistory.addItem(item);
        if (gameHistory.size() > 0) {
            enableHistoryButton(true);
        } else {
            enableHistoryButton(false);
        }
    }


    private void enableHistoryButton(boolean enable) {
        if (historyButton != null) {
            historyButton.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void updateShuffledPictures(int movementId) {
        pictureToGame.clear();
        if (movementId == 0) {
            pictureToGame.addAll(dataProvider.getPictures());
        } else {
            for(Picture p : dataProvider.getPictures()) {
                if (p.movement_id != 0) {
                    pictureToGame.add(p);
                }
            }
        }
        Collections.shuffle(pictureToGame);
    }

    public ArrayList<Picture> getShuffledPictures(int size, int movementId) {
        if(pictureToGame.size() < size) {
            updateShuffledPictures(movementId);
        }
        return pictureToGame;
    }

    private void openHistory() {
        Bundle bundle = new Bundle();
        bundle.putString("from", "game");
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        mFirebaseAnalytics.logEvent("goHistory", bundle);
        historyFragment = (HistoryFragment) getActivity().getSupportFragmentManager().findFragmentByTag("history");
        if (historyFragment == null) {
            historyFragment = new HistoryFragment();
        }
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_holder, historyFragment)
                .addToBackStack("history").commit();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        sessionStatistic = new BaseGameStatistic();
        pictureToGame.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dialog = (GameSetFinishedDialog) getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (dialog == null) {
            dialog = new GameSetFinishedDialog();
        }
        dialog.init(sessionStatistic, userData);
        dialog.setEventListener(this);

        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity a = getActivity();
        if (a != null) {
            View historyButton = a.findViewById(R.id.action_bar_info_button);
            if (historyButton != null) {
                this.historyButton = historyButton;
                historyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openHistory();
                    }
                });
                if(gameHistory.size() > 0) {
                    historyButton.setVisibility(View.VISIBLE);
                } else {
                    historyButton.setVisibility(View.INVISIBLE);
                }
            }
        }
        dataProvider.addDataChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Activity a = getActivity();
        if (a != null) {
            View historyButton = a.findViewById(R.id.action_bar_info_button);

            if (historyButton != null) {
                historyButton.setOnClickListener(null);
            }
            historyButton.setVisibility(View.INVISIBLE);
            this.historyButton = null;
        }
        dataProvider.removeDataChangedListener(this);
    }

    @Override
    public void moreButtonPressed() {

    }

    @Override
    public void finishButtonPressed() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void dataChanged() {
        pictureToGame.clear();
    }
}
