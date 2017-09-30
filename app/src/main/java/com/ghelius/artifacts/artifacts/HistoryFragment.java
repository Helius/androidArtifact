package com.ghelius.artifacts.artifacts;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
public class HistoryFragment extends Fragment {

    private StorageReference mStorageRef;
    private Adapter mAdapter;

    private class Adapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        Adapter(Context context) {
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return GameHistory.instance().size();
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
            TextView author;
            TextView pic_name;
            TextView movement;
            TextView year;
            View button;
        }

        @Override
        public View getView(int i, View view, final ViewGroup viewGroup) {
            final ViewHolder viewHolder;
            GameHistory.GameHistoryItem item = GameHistory.instance().getItem(i);
            final Picture p = GameDataProvider.instance().getPictureByPath(item.img_path);
//            final Picture p = GameDataProvider.instance().getPictureByPath("Винсент Ван Гог/1_10_the_starry_night.jpg");
            if (p == null) {
                return view;
            }
            final Author a = GameDataProvider.instance().getAuthorById(p.author);

            if (view == null) {
                view = mInflater.inflate(R.layout.history_item, viewGroup, false);
                view.setDrawingCacheEnabled(true);
                viewHolder = new ViewHolder();
                viewHolder.image = (ImageView) view.findViewById(R.id.hist_image);
                viewHolder.author = (TextView) view.findViewById(R.id.hist_author);
                viewHolder.year = (TextView) view.findViewById(R.id.hist_year);
                viewHolder.pic_name = (TextView) view.findViewById(R.id.hist_pic_name);
                viewHolder.movement = (TextView) view.findViewById(R.id.hist_movement);
                viewHolder.button = view.findViewById(R.id.history_info_button);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            if (p.getLink() != null && !p.getLink().isEmpty()) {
                viewHolder.button.setVisibility(View.VISIBLE);
                viewHolder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(p.getLink()));
                        startActivity(i);
                    }
                });
            } else {
                viewHolder.button.setVisibility(View.GONE);
            }

            viewHolder.author.setText(a.getName());

            if (p.year != null && !p.year.isEmpty()) {
                viewHolder.year.setVisibility(View.VISIBLE);
                viewHolder.year.setText(", " + p.year);
            } else {
                viewHolder.year.setVisibility(View.GONE);
            }


            if (p.getName() != null && !p.getName().isEmpty()) {
                viewHolder.pic_name.setVisibility(View.VISIBLE);
                viewHolder.pic_name.setText(p.getName());
            } else {
                viewHolder.pic_name.setVisibility(View.GONE);
            }

            String movement_str;
            if (p.movement_id != 0) {
                movement_str = GameDataProvider.instance().getMovementById(p.movement_id).getName();
            } else {
                movement_str = GameDataProvider.instance().getMovementById(GameDataProvider.instance().getAuthorById(p.author).movement_id).getName();
            }
            if (!p.getHolder().isEmpty()) {
                movement_str += ", " + p.getHolder();
            }
            viewHolder.movement.setText(movement_str);

            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(item.img_path))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(2000,2000)
                    .into(viewHolder.image);
            return view;
        }
    }

    public HistoryFragment() {
        // Required empty public constructor
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
        mAdapter = new Adapter(getContext());
        list.setAdapter(mAdapter);
        setRetainInstance(true);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.history_title);
        getView().findViewById(R.id.history_empty_view).setVisibility(mAdapter.getCount() > 0 ? View.INVISIBLE : View.VISIBLE);
    }
}
