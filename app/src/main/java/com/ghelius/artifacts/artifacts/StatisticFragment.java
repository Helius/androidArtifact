package com.ghelius.artifacts.artifacts;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class StatisticFragment extends Fragment {

    private UserData userData;
    private List<String> your_array_list;

    public StatisticFragment() {
        // Required empty public constructor
    }
    
    public void init (UserData userData) {
        this.userData = userData;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_statistic, container, false);
        TextView t = (TextView) v.findViewById(R.id.statistic_label);
        t.setText(getString(R.string.statistics_title) + " for level " + userData.getLevel());
        ListView listView = (ListView) v.findViewById(R.id.statistic_listview);

        your_array_list = new ArrayList<String>();

        your_array_list.add(ChooseAuthorGameFragment.TAG + ": "
                + userData.getGameStatistic(ChooseAuthorGameFragment.TAG).successfullAttempt
                + " / "+
                + userData.getGameStatistic(ChooseAuthorGameFragment.TAG).totalAttempt
        );
        your_array_list.add(TypeAuthorGameFragment.TAG + ": "
                + userData.getGameStatistic(TypeAuthorGameFragment.TAG).successfullAttempt
                + " / "+
                + userData.getGameStatistic(TypeAuthorGameFragment.TAG).totalAttempt
        );
        your_array_list.add(ChoosePaintGameFragment.TAG + ": "
                + userData.getGameStatistic(ChoosePaintGameFragment.TAG).successfullAttempt
                + " / "+
                + userData.getGameStatistic(ChoosePaintGameFragment.TAG).totalAttempt
        );
        your_array_list.add(ChooseMovementGameFragment.TAG + ": "
                + userData.getGameStatistic(ChooseMovementGameFragment.TAG).successfullAttempt
                + " / "+
                + userData.getGameStatistic(ChooseMovementGameFragment.TAG).totalAttempt
        );

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity().getApplicationContext(),
                R.layout.simple_expandable_list_item_my,
                your_array_list );

        listView.setAdapter(arrayAdapter);
        return v;
    }

}
