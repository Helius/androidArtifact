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
import java.util.Locale;

/**
 * Created by eugene on 28.04.17.
 */
public class GalleryFragment extends Fragment {

    private ArrayList<Author> authors;
    private ArrayList<Picture> pictures;
    private ArrayList<Movement> movements;
    private String locale;

    public void init(ArrayList<Author> authors, ArrayList<Picture> pictures, ArrayList<Movement> movements) {
        this.authors = authors;
        this.pictures = pictures;
        this.movements = movements;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        locale = Locale.getDefault().getLanguage();
        View v = inflater.inflate(R.layout.gallery_fragment, container, false);
        ((TextView) v.findViewById(R.id.authors_count)).setText(getResources().getString(R.string.authors_count, authors.size()));

        int level_1_cnt = 0;
        int level_2_cnt = 0;
        int level_3_cnt = 0;
        for (Picture p: pictures) {
            if (p.level == 1) {
                level_1_cnt++;
            } else if (p.level == 2) {
                level_2_cnt++;
            } else if (p.level == 3) {
                level_3_cnt++;
            }
        }
        ((TextView) v.findViewById(R.id.movements_count))
                .setText(getResources().getString(R.string.movements_count, level_1_cnt, level_2_cnt, level_3_cnt));

        ListView listView = (ListView) v.findViewById(R.id.gallery_listview);
        listView.setAdapter(new AuthorListAdapter(getActivity().getApplicationContext(), authors));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ArrayList<Picture> authors_picture = new ArrayList<Picture>();
                for (Picture p: pictures) {
                    if (p.author == authors.get(i).id) {
                        authors_picture.add(p);
                    }
                }
                PictureListFragment pictureListFragment = (PictureListFragment) getActivity().getSupportFragmentManager().findFragmentByTag("pictureList");
                if (pictureListFragment == null) {
                    pictureListFragment = new PictureListFragment();
                }
                pictureListFragment.init(authors_picture);
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
            if (locale.equals("ru")) {
                ((TextView) view.findViewById(R.id.gallery_author_name)).setText(authors.get(i).name_ru);
            } else {
                ((TextView) view.findViewById(R.id.gallery_author_name)).setText(authors.get(i).name_en);
            }
            int pictures_count = 0;
            for(Picture p: pictures) {
                if (p.author == authors.get(i).id) {
                    pictures_count++;
                }
            }
            ((TextView) view.findViewById(R.id.picture_count)).setText(Integer.toString(pictures_count));
            String movement_name = "Not defined";
            for(Movement m: movements) {
                if (authors.get(i).movement_id == m.id) {
                    if (locale.equals("ru")) {
                        movement_name = m.name_ru;
                    } else {
                        movement_name = m.name_en;
                    }
                    break;
                }
            }
            ((TextView) view.findViewById(R.id.movement_name)).setText(movement_name);
            return view;
        }
    }
}
