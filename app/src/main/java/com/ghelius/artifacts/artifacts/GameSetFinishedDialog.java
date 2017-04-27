package com.ghelius.artifacts.artifacts;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class GameSetFinishedDialog extends android.support.v4.app.DialogFragment {

    private int trueCount;
    private int from;


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
        getDialog().setTitle("Title!");
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

    public void init(BaseGameStatistic sessionStatistic, BaseGameStatistic totalStatistic, int level) {
        this.trueCount = sessionStatistic.successfullAttempt;
        this.from = sessionStatistic.totalAttempt;
    }
}
