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
        TextView t = (TextView) v.findViewById(R.id.statistic_label);
        t.setText(getString(R.string.statistics_title, userData.getLevelName(userData.getLevel(), getContext())));
        ListView listView = (ListView) v.findViewById(R.id.statistic_listview);


//        your_array_list = new ArrayList<String>();
//
//        your_array_list.add(ChooseAuthorGameFragment.TAG + ": "
//                + userData.getGameStatistic(ChooseAuthorGameFragment.TAG).trueAttempt
//                + " / "+
//                + (userData.getGameStatistic(ChooseAuthorGameFragment.TAG).trueAttempt
//                + userData.getGameStatistic(ChooseAuthorGameFragment.TAG).falseAttempt)
//        );
//        your_array_list.add(TypeAuthorGameFragment.TAG + ": "
//                + userData.getGameStatistic(TypeAuthorGameFragment.TAG).trueAttempt
//                + " / "+
//                + (userData.getGameStatistic(TypeAuthorGameFragment.TAG).trueAttempt
//                + userData.getGameStatistic(TypeAuthorGameFragment.TAG).falseAttempt)
//        );
//        your_array_list.add(ChoosePaintGameFragment.TAG + ": "
//                + userData.getGameStatistic(ChoosePaintGameFragment.TAG).trueAttempt
//                + " / "+
//                + (userData.getGameStatistic(ChoosePaintGameFragment.TAG).trueAttempt
//                + userData.getGameStatistic(ChoosePaintGameFragment.TAG).falseAttempt)
//        );
//        your_array_list.add(ChooseMovementGameFragment.TAG + ": "
//                + userData.getGameStatistic(ChooseMovementGameFragment.TAG).trueAttempt
//                + " / "+
//                + (userData.getGameStatistic(ChooseMovementGameFragment.TAG).trueAttempt
//                + userData.getGameStatistic(ChooseMovementGameFragment.TAG).falseAttempt)
//        );

//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
//                getActivity().getApplicationContext(),
//                R.layout.simple_expandable_list_item_my,
//                your_array_list );
        listView.setAdapter(new StatisticItemAdapter(getActivity().getApplicationContext(), userData));
        return v;
    }

    private class StatisticItemAdapter extends BaseAdapter {

        LayoutInflater mInflater;
        private UserData userData = null;
        String locale = Locale.getDefault().getLanguage();


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
            if (trueValue + falseValue > 0) {
                percent.setText(String.valueOf((100 * trueValue) / (trueValue + falseValue)) + "%");
            }

            return view;
        }
    }

}
