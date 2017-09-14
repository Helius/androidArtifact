package com.ghelius.artifacts.artifacts;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryList extends Fragment {

    private GameHistory history;
    private StorageReference mStorageRef;
    private Adapter mAdapter;

    private class Adapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final GameHistory history;

        Adapter(Context context, GameHistory history) {
            this.history = history;
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (history != null) {
                return history.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        class ViewHolder {
            ImageView image;
        }

        @Override
        public View getView(int i, View view, final ViewGroup viewGroup) {
            ViewHolder viewHolder;
            GameHistory.GameHistoryItem item = history.getItem(i);
            if (view == null) {
                view = mInflater.inflate(R.layout.history_item, viewGroup, false);
                viewHolder = new ViewHolder();
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.image = (ImageView) view.findViewById(R.id.hist_image);
            TextView author_name = (TextView) view.findViewById(R.id.hist_pic_name);

            author_name.setText(item.getAuthor().name_ru + ", 1937");
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(item.picture.path))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.image);
            return view;
        }
    }

    public HistoryList() {
        // Required empty public constructor
    }

    public void init(GameHistory gameHistory) {
        history = gameHistory;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history_list, container, false);
        ListView list = (ListView) v.findViewById(R.id.history_list);
        list.setDivider(null);
        list.setDividerHeight(0);
        mAdapter = new Adapter(getContext(), history);
        list.setAdapter(mAdapter);
        setRetainInstance(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.history_title);
    }
}
