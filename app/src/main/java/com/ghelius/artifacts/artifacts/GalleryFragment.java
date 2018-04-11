package com.ghelius.artifacts.artifacts;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class GalleryFragment extends Fragment {

    private ArrayList<Author> authors;
    private ArrayList<Picture> pictures;
    private ArrayList<Movement> movements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        GameDataProvider gameDataProvider = GameDataProvider.instance();
        this.authors = gameDataProvider.getFullAuthors();
        this.pictures = gameDataProvider.getFullPictures();
        this.movements = gameDataProvider.getFullMovements();
        Collections.sort(authors, new Author.AuthorNameComparator());


    }

    @Override
    public void onResume() {
        getActivity().setTitle(R.string.gallery_title);
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery_fragment, container, false);
        ListView listView = (ListView) v.findViewById(R.id.gallery_listview);
        listView.setAdapter(new AuthorListAdapter(getActivity().getApplicationContext(), authors));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PictureListFragment pictureListFragment = (PictureListFragment) getActivity().getSupportFragmentManager().findFragmentByTag("pictureList");
                if (pictureListFragment == null) {
                    pictureListFragment = new PictureListFragment();
                }
                pictureListFragment.init(authors.get(i));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .hide(GalleryFragment.this)
                        .replace(R.id.main_fragment_holder, pictureListFragment)
                        .addToBackStack("pictureList").commit();
            }
        });
        return v;

    }

    private class AuthorListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private ArrayList<Author> authors;

        private AuthorListAdapter(Context context, ArrayList<Author> authors) {
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.authors = authors;
        }

        @Override
        public int getCount() {
            return authors.size();
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
                view = mInflater.inflate(R.layout.gallery_author_item, viewGroup, false);
            }

            String name_string = authors.get(i).getName();

            String years = authors.get(i).getYears();
            if (!years.isEmpty()) {
                name_string += " (" + years + ")";
            }

            ((TextView) view.findViewById(R.id.gallery_author_name)).setText(name_string);

            String movement_name = "";
            for(Movement m: movements) {
                if (authors.get(i).movement_id == m.id) {
                    movement_name = m.getName();
                    break;
                }
            }

            ((TextView) view.findViewById(R.id.movement_name)).setText(movement_name);
            return view;
        }
    }
}
