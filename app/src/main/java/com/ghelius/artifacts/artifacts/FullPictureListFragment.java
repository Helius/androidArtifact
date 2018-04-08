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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FullPictureListFragment extends Fragment {
    private StorageReference mStorageRef;
    private Adapter mAdapter;
    private Author author;
    private ArrayList<Picture> pictures;
    private int initPictureIndex;
    private ListView list;

    void init(Author author, int pictureIndex) {
        this.author = author;
        this.pictures = GameDataProvider.instance().getPicturesByAuthor(author);
        this.initPictureIndex = pictureIndex;
    }

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
            ImageView guess_mark;
            TextView author;
            TextView pic_name;
            TextView holder;
            TextView year;
            View button;
            public int height = -1;
        }

        @Override
        public View getView(int i, View view, final ViewGroup viewGroup) {
            final ViewHolder viewHolder;

            final Picture p = pictures.get(i);
            if (p == null) {
                return view;
            }
            final Author a = GameDataProvider.instance().getAuthorById(p.author);

            if (view == null) {
                view = mInflater.inflate(R.layout.history_item, viewGroup, false);
                view.setDrawingCacheEnabled(true);
                viewHolder = new ViewHolder();
                viewHolder.image = (ImageView) view.findViewById(R.id.history_image);

                viewHolder.author = (TextView) view.findViewById(R.id.history_line_1);

                viewHolder.pic_name = (TextView) view.findViewById(R.id.history_line_2_first);

                viewHolder.holder = (TextView) view.findViewById(R.id.history_line_3_first);

                viewHolder.button = view.findViewById(R.id.history_info_button);
                viewHolder.guess_mark = (ImageView) view.findViewById(R.id.guessed_mark);
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

            viewHolder.guess_mark.setImageResource(0);

            String author_text = a.getName();
            if (p.movement_id != 0) {
                author_text += ". " + GameDataProvider.instance().getMovementById(p.movement_id).getName();
            } else {
                author_text += ". " + GameDataProvider.instance().getMovementById(GameDataProvider.instance().getAuthorById(p.author).movement_id).getName();
            }
            viewHolder.author.setText(author_text);

            String name_year = "";
            if (p.getName() != null && !p.getName().isEmpty()) {
                name_year = p.getName();
            }
            if (p.year != null && !p.year.isEmpty()) {
                if (name_year.isEmpty()) {
                    name_year = p.year;
                } else {
                    name_year += ". " + p.year;
                }
            }

            if (!name_year.isEmpty()) {
                viewHolder.pic_name.setVisibility(View.VISIBLE);
                viewHolder.pic_name.setText(name_year);
            } else {
                viewHolder.pic_name.setVisibility(View.GONE);
            }

            if(p.getHolder() != null && !p.getHolder().isEmpty())
            {
                viewHolder.holder.setVisibility(View.VISIBLE);
            } else {
                viewHolder.holder.setVisibility(View.GONE);
            }
            viewHolder.holder.setText(p.getHolder());

            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(p.path))
                    .listener(new RequestListener<StorageReference, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            viewHolder.height = viewHolder.image.getMinimumHeight();
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(1080,1080)
                    .into(viewHolder.image);


            if (viewHolder.height != -1) {
                view.setMinimumHeight(viewHolder.image.getMinimumHeight());
            }

            return view;
        }
    }

    public FullPictureListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history_list, container, false);
        list = (ListView) v.findViewById(R.id.history_list);
        list.setDrawingCacheEnabled(true);
        list.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
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
        list.setSelection(initPictureIndex);
    }
}
