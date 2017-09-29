package com.ghelius.artifacts.artifacts;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        ListView listView = (ListView) v.findViewById(R.id.statistic_listview);

        listView.setAdapter(new StatisticItemAdapter(getActivity().getApplicationContext(), userData));
        return v;
    }

    private class StatisticItemAdapter extends BaseAdapter {

        LayoutInflater mInflater;
        private UserData userData = null;

        private StatisticItemAdapter(Context context, UserData userData) {
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.userData = userData;
        }

        @Override
        public int getCount() {
            return 4; //TODO: hardcode!!!
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(R.layout.statistic_game_item, viewGroup, false);
            }

            SquareImageView icon = (SquareImageView) view.findViewById(R.id.game_icon);
            TextView name = (TextView) view.findViewById(R.id.game_name);
            TextView trueCount = (TextView) view.findViewById(R.id.true_count);
            TextView totalCount = (TextView) view.findViewById(R.id.total_count);
            TextView percent = (TextView) view.findViewById(R.id.stat_percent);
            RatingBar bar = (RatingBar) view.findViewById(R.id.ratingBar);

            String gameTag;

            switch (i) {
                case 0:
                    icon.setImageResource(R.drawable.icon0);
                    name.setText(R.string.game_0_title);
                    gameTag = ChooseAuthorGameFragment.TAG;
                    break;
                case 1:
                    icon.setImageResource(R.drawable.icon1);
                    name.setText(R.string.game_1_title);
                    gameTag = TypeAuthorGameFragment.TAG;
                    break;
                case 2:
                    icon.setImageResource(R.drawable.icon2);
                    name.setText(R.string.game_2_title);
                    gameTag = ChoosePaintGameFragment.TAG;
                    break;
                default:
                    icon.setImageResource(R.drawable.icon3);
                    name.setText(R.string.game_3_title);
                    gameTag = ChooseMovementGameFragment.TAG;
                    break;
            }

            int trueValue = userData.getGameStatistic(gameTag).trueAttempt;
            int falseValue = userData.getGameStatistic(gameTag).falseAttempt;
            trueCount.setText(String.valueOf(trueValue));
            totalCount.setText(String.valueOf(trueValue + falseValue));
            bar.setRating(0);
            if (trueValue + falseValue > 0) {
                percent.setText(String.valueOf((100 * trueValue) / (trueValue + falseValue)) + "%");
                float rating = (float) trueValue / (trueValue + falseValue);
                bar.setRating(rating * 5);
            }

            return view;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.statistics_title, userData.getLevelName(userData.getLevel(), getContext())));
    }
}
