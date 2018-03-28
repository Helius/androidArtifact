package com.ghelius.artifacts.artifacts;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class PictureListFragment extends Fragment {

    private StorageReference mStorageRef;
    private ArrayList<Picture> pictures;
    private Author author;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private class ButtonAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private ArrayList<Picture> pictures;

        ButtonAdapter(Context context, ArrayList<Picture> pictures) {
            this.pictures = pictures;
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return pictures.size();
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
        public View getView(int i, View view, final ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(R.layout.choose_paint_item, viewGroup, false);
            }
            final Picture picture = pictures.get(i);
            final ImageView pic = (ImageView) view.findViewById(R.id.picture);
            pic.setImageResource(R.drawable.picture_dashed_placeholder);
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(picture.path))
                    .listener(new RequestListener<StorageReference, GlideDrawable>() {

                        @Override
                        public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .override(240, 240)
                    .into(pic);
            return view;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mStorageRef = FirebaseStorage.getInstance().getReference();
        View v = inflater.inflate(R.layout.picture_list_fragment, container, false);
        GridView gridView = (GridView) v.findViewById(R.id.paint_grid);
        gridView.setAdapter(new ButtonAdapter(getActivity().getApplicationContext(), pictures));
        return v;
    }

    public void init(Author author) {
        pictures = GameDataProvider.instance().getPicturesByAuthor(author);
        this.author = author;
    }

    @Override
    public void onResume() {
        getActivity().setTitle(author.getName());
        super.onResume();
    }
}
