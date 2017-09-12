package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public abstract class BaseGameFragment extends Fragment implements GameSetFinishedDialog.DialogEventListener {

    private View historyButton = null;
    private GameHistory gameHistory = new GameHistory();

    public GameDataProvider gameDataProvider = null;
    public GameSetFinishedDialog dialog;
    public UserData userData = null;
    public BaseGameStatistic sessionStatistic;
    public String locale;
    public int gameIndex = 0;
    public int gameCount = 10;

    public BaseGameFragment() {
        // Required empty public constructor
    }

    public void setServerResources(UserData userData, GameDataProvider gameDataProvider) {
        this.gameDataProvider = gameDataProvider;
        this.userData = userData;
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

    private void openHistory() {
        //TODO: open history
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        locale = Locale.getDefault().getLanguage();

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
            }
        }
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
    }

    @Override
    public void moreButtonPressed() {

    }

    @Override
    public void finishButtonPressed() {

    }
}
