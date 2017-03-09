package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Random;

public class TypeAuthorGameFragment extends Fragment {

    private static final String TAG = "TypeAuthorGame";

    private ArrayList<Picture> pictures;
    private ArrayList<Author> authors;
    private ArrayList<Game> games;
    private int GameCount = 10;
    private int gameIndex = 0;
    private ImageView mImageView;
    Random rnd;
    private StorageReference mStorageRef;

    public TypeAuthorGameFragment() {
        // Required empty public constructor
        rnd = new Random(System.currentTimeMillis());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (games == null) {
            games = createNewGames(GameCount);
        }
        View v = inflater.inflate(R.layout.fragment_type_author_game, container, false);
        mImageView = (ImageView) v.findViewById(R.id.main_pic);
        playGame(gameIndex);
        return v;
    }

    public void setServerResources(ArrayList<Picture> pictures, ArrayList<Author> authors) {
        this.pictures = pictures;
        this.authors = authors;
    }

    private void playGame (int index) {
        games.get(index).loadPicture();
    }

    private ArrayList<Game> createNewGames(int num)
    {
        ArrayList<Game> games = new ArrayList<>();
        for (int i = 0; i < num; ++i) {
            // create game with random author
            games.add(new Game(authors.get(rnd.nextInt(authors.size()))));
        }
        return games;
    }

    class Game {
        Author author;
        Picture picture;
        Game (Author a) {
            this.author = a;
            ArrayList<Picture> pic = new ArrayList<>();
            // find random picture of given author
            for (Picture p : pictures) {
                if (p.author == author.id) {
                    pic.add(p);
                }
            }
            picture = pic.get(rnd.nextInt(pic.size()));
        }

        public void loadPicture() {
            Activity a = getActivity();
            if (a == null)
                return;

            Glide.with(a.getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(picture.path))
                    .asBitmap()
                    .override(600,600)
                    .fitCenter()
                    .into(mImageView);
        }
    }
}
