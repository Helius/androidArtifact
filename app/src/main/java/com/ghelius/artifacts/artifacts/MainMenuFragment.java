package com.ghelius.artifacts.artifacts;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainMenuFragment extends Fragment {

    private ArrayList<GameEntry> mGameEntries;
    private MainMenuListener mListener;
    private LinearLayout toast_layout;
    private String toast_text = "";

    public interface MainMenuListener {
        void menuClicked(int number, GameEntry entry);
    }

    public class GameEntry {
        GameEntry(Integer thumbRes, String title) {
            this.thumbRes = thumbRes;
            this.title = title;
        }
        Integer thumbRes;
        String title;
    }

    public void setMainMenuListener(MainMenuListener listener) {
        mListener = listener;
    }

    public MainMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.game_bottons_layout, container, false);

        GridView gridView = (GridView) v.findViewById(R.id.main_menu_grid);
        gridView.setAdapter(new MainMenuListAdapter(getActivity().getApplicationContext()));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mListener != null) {
                    mListener.menuClicked(i, mGameEntries.get(i));
                }
            }
        });

        toast_layout = (LinearLayout) v.findViewById(R.id.main_menu_toast);
        v.findViewById(R.id.toast_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideToast();
            }
        });
        if (!toast_text.isEmpty()) {
            showToast(toast_text);
        }
        return v;
    }


    public void showToast(String str) {
        toast_text = str;
        TransitionManager.beginDelayedTransition(toast_layout);
        ((TextView)toast_layout.findViewById(R.id.toast_text)).setText(str);
        toast_layout.setVisibility(View.VISIBLE);
    }

    public void hideToast() {
        TransitionManager.beginDelayedTransition(toast_layout);
        toast_layout.setVisibility(View.GONE);
        toast_text="";
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mGameEntries = new ArrayList<>();
        mGameEntries.add(new GameEntry(R.drawable.icon0, getString(R.string.game_0_title)));
        mGameEntries.add(new GameEntry(R.drawable.icon1, getString(R.string.game_1_title)));
        mGameEntries.add(new GameEntry(R.drawable.icon2, getString(R.string.game_2_title)));
        mGameEntries.add(new GameEntry(R.drawable.icon3, getString(R.string.game_3_title)));
    }


    // Item adapter ~~~

    private class MainMenuListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        private MainMenuListAdapter(Context context) {
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mGameEntries.size();
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
                view = mInflater.inflate(R.layout.game_button_item, viewGroup, false);
            }
            ((ImageView) view.findViewById(R.id.icon)).setImageResource(mGameEntries.get(i).thumbRes);
            ((TextView) view.findViewById(R.id.title)).setText(mGameEntries.get(i).title);

            return view;
        }
    }
}
