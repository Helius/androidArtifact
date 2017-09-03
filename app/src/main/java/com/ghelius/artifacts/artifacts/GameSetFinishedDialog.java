package com.ghelius.artifacts.artifacts;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.test.espresso.matcher.PreferenceMatchers;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class GameSetFinishedDialog extends android.support.v4.app.DialogFragment {

    private int trueCount;
    private int from;
    private UserData userData;
    private String gameTag;


    public interface DialogEventListener {
        void moreButtonPressed();
        void finishButtonPressed();
    }

    private DialogEventListener mListener = null;

    public void setEventListener(DialogEventListener listener) {
        mListener = listener;
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if(mListener != null) {
            mListener.finishButtonPressed();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int rating = 0;

        if (from != 0) {
            rating = (10*trueCount)/from;
        }

        String title;
        if (rating < 4) {
            getDialog().setTitle(R.string.game_finished_dialog_title_low);
        } else if (rating >= 4 && rating < 9) {
            getDialog().setTitle(R.string.game_finished_dialog_title_mid);
        } else {
            getDialog().setTitle(R.string.game_finished_dialog_title_high);
        }

        View v = inflater.inflate(R.layout.game_finished_dialog, null);
        Button moreButton = (Button)v.findViewById(R.id.more_button);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.moreButtonPressed();
                }
                dismiss();
            }
        });
        Button finishButton = (Button) v.findViewById(R.id.finish_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.finishButtonPressed();
                }
                dismiss();
            }
        });
        TextView textView = (TextView)v.findViewById(R.id.guessed_message);
        textView.setText(getResources().getString(R.string.finish_dialog_message, trueCount, from)); //"Вы угадали " + trueCount + " из " + from + "!");
        getDialog().setCanceledOnTouchOutside(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (from == 0)
            return;

        if (userData.getLevel() < userData.getMaxLevel()) {
            final String key = "SuccessSessionCount";
            SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            int cnt = p.getInt(key, 0) + 1;

            if((trueCount * 10) / from > 8) {
                //TODO: eventLog to analytics
                if (cnt == 3) {
                    cnt = 0;
                    showRizeLevelDialog();
                }
            } else {
                cnt = 0;
            }
            p.edit().putInt(key, cnt);
        }
    }

    public void init(BaseGameStatistic sessionStatistic, UserData userData, String gameTag) {
        this.trueCount = sessionStatistic.trueAttempt;
        this.from = sessionStatistic.trueAttempt + sessionStatistic.falseAttempt;
        this.userData = userData;
        this.gameTag = gameTag;
    }


    private void showRizeLevelDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getString(R.string.rize_level_dialog_title, userData.getLevelName(userData.getLevel(), getActivity().getApplicationContext())));
        alertDialog.setMessage(getString(R.string.rize_level_dialog_text, userData.getLevelName(userData.getLevel() + 1, getActivity().getApplicationContext())));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        userData.setLevel(userData.getLevel()+1);
                        //logEvent("UserAcceptLevelUp", String.valueOf(getLevel()));
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: eventLog to analytics
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
