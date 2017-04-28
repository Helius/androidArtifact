package com.ghelius.artifacts.artifacts;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.text.TextDirectionHeuristicCompat;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by eugene on 29.04.17.
 */
public class PictureListFragment extends Fragment {

    private String locale;
    private int authorId;
    private ArrayList<Picture> pictures;
    private StorageReference mStorageRef;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        locale = Locale.getDefault().getLanguage();
        View v = inflater.inflate(R.layout.picture_list_fragment, container, false);
        ListView listView = (ListView) v.findViewById(R.id.picture_list_view);
        listView.setAdapter(new PictureListAdapter(getActivity().getApplicationContext(), pictures));
        return v;
    }

    public void init(ArrayList<Picture> pictures) {
        this.pictures = pictures;
    }

    private class PictureListAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        public PictureListAdapter(Context context, ArrayList<Picture> picture) {
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(R.layout.gallery_picture_item, viewGroup, false);
            }
            ImageView imageView = (ImageView) view.findViewById(R.id.pic);
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(pictures.get(i).path))
                    .into(imageView);
            ((TextView) view.findViewById(R.id.picture_level_text)).
                    setText(getResources().getString(R.string.picture_level_text, pictures.get(i).level));
            ((TextView) view.findViewById(R.id.picture_path_text)).
                    setText(pictures.get(i).path);


            return view;
        }
    }
}
