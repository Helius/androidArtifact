package com.ghelius.artifacts.artifacts;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

/**
 * Created by eugene on 27.04.17.
 */

public class ChooseLevelDialog extends DialogFragment {

    private UserData userData;

    void init(UserData userData) {
        this.userData = userData;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.select_level_dialog_title);
        View v = inflater.inflate(R.layout.choose_level_dialog, null);
        RadioGroup g = (RadioGroup) v.findViewById(R.id.levels_button);
        switch (userData.getLevel()) {
            case 1:
                g.check(R.id.level1);
                break;
            case 2:
                g.check(R.id.level2);
                break;
            default:
                g.check(R.id.level0);
                break;
        }
        g.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.level1:
                        userData.setLevel(1);
                        break;
                    case R.id.level2:
                        userData.setLevel(2);
                        break;
                    default:
                        userData.setLevel(0);
                        break;
                }
            }
        });
        getDialog().setCanceledOnTouchOutside(true);
        return v;
    }
}
