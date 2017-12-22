package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ChooseAuthorGameFragment extends BaseGameFragment {

    public static final String TAG = "ChooseAuthor";

    private ImageView mImageView;
    private Random rnd;
    private boolean buttonBlocked;
    private TextButtonAdapter mAdapter = null;
    private ArrayList<ChooseAuthorGame> games = null;


    private void init () {
        sessionStatistic = new BaseGameStatistic();
        rnd = new Random(System.currentTimeMillis());
        buttonBlocked = false;
    }

    public ChooseAuthorGameFragment() {
        // Required empty public constructor
        init();
    }


    private void showButtonBlock(boolean show) {
        View v = getView();
        if (v == null)
            return;
        if (show) {
            v.findViewById(R.id.choose_button_grid_view).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.choose_button_grid_view).setVisibility(View.INVISIBLE);
        }
        buttonBlocked = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_author_game, container, false);
        mImageView = (ImageView) view.findViewById(R.id.main_pic);
        ExpandableWidthGridView mGridView = (ExpandableWidthGridView) view.findViewById(R.id.choose_button_grid_view);
        mAdapter = new TextButtonAdapter(getActivity().getApplicationContext());
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                buttonSelected(i);
            }
        });
        if (games == null) {
            games = createNewGame(gameCount);
        }
        if(view.findViewById(R.id.layout_land_marker) != null) {
            mGridView.setExpanded(true);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        playGame(gameIndex);
        getActivity().setTitle(R.string.game_0_title);
    }

    private void buttonSelected(int ind) {

        if (buttonBlocked)
            return;
        buttonBlocked = true;
        int timeout = 500;
        boolean result = false;
        if (games.get(gameIndex).picture.author == games.get(gameIndex).authors_variant.get(ind).id) {
            // Right )
            result = true;
            mAdapter.getButton(ind).state = TextButton.State.True;
        } else {
            // Fail (
            timeout = 1500;
            mAdapter.getButton(ind).state = TextButton.State.False;

            for(TextButton button : mAdapter.getButtons()) {
                if(button.id == games.get(gameIndex).picture.author) {
                    button.state = TextButton.State.True;
                }
            }
        }
        addToHistory(new GameHistory.GameHistoryItem(games.get(gameIndex).picture, result));
        sessionStatistic.addAttempt(result);
        userData.updateGameStatistic(getContext(), games.get(gameIndex).picture, result, TAG);
        mAdapter.update();

        Handler h = new Handler();

        h.postDelayed(new Runnable() {
            @Override
            public void run() {

                showButtonBlock(true);
                if (gameIndex+1 < games.size()) {
                    showButtonBlock(false);
                    playGame(++gameIndex);
                } else {
                    dialog.init(sessionStatistic, userData);
                    dialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            }
        }, timeout);

    }

    @Override
    public void moreButtonPressed() {
        games = createNewGame(gameCount);
        playGame(gameIndex);
    }

    @Override
    public void finishButtonPressed() {
        // delete games here, when we will return, we create new one, instead using existing
        super.finishButtonPressed();
        games = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        games = createNewGame(gameCount);
    }


    private void playGame(int gameIndex) {
        if (gameIndex < games.size()) {
            ChooseAuthorGame game = games.get(gameIndex);
            game.loadPicture();
            mAdapter.clearButton();
            for (Author author : game.authors_variant) {
                mAdapter.addNewButton(new TextButton(author.getName(), author.id));
            }
            mAdapter.update();

            if (gameIndex + 1 < games.size()) {
                games.get(gameIndex + 1).cachePicture();
            }
        } else { // we played all game and now just show last one
            ChooseAuthorGame game = games.get(gameIndex-1);
            game.loadPicture();
            mAdapter.update();
        }
    }



    ArrayList<ChooseAuthorGame> createNewGame(int count)
    {
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
        ArrayList<ChooseAuthorGame> games = new ArrayList<>();

        ArrayList<Picture> local_pics = getShuffledPictures(count, 0);

        for (int i = 0; i < count; i++) {
            ChooseAuthorGame game = new ChooseAuthorGame();
            if (local_pics.size() > 0) {
                game.picture = local_pics.remove(rnd.nextInt(local_pics.size()));
                game.authors_variant.add(dataProvider.getAuthorById(game.picture.author));
                int author_count = 3;
                while (author_count > 0) {
                    Author a = dataProvider.getAuthors().get(rnd.nextInt(dataProvider.getAuthors().size()));
                    if (!game.authors_variant.contains(a)) {
                        game.authors_variant.add(a);
                        author_count--;
                    }
                }
                Collections.shuffle(game.authors_variant, rnd);
            }
            games.add(game);
        }
        games.get(0).loadPicture();
        games.get(1).cachePicture();
        for (ChooseAuthorGame g : games) {
            Log.d("helius game:", (g.picture.path));
        }
        return games;
    }

    class ChooseAuthorGame {
        Picture picture;
        ArrayList<Author> authors_variant = new ArrayList<>();

        ChooseAuthorGame() {
        }

        void loadPicture() {
            Activity a = getActivity();
            if (a == null)
                return;

            final View v = getView();
            if (v == null)
                return;
            final View progress_view = getView().findViewById(R.id.progress_view);
            if(progress_view != null) {
                progress_view.setVisibility(View.VISIBLE);
            }
            Glide.with(a.getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(picture.path))
                    .listener(new RequestListener<StorageReference, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if (progress_view != null) {
                                progress_view.setVisibility(View.GONE);
                            }
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(600, 600)
                    .into(mImageView)
            ;
            showButtonBlock(true);
        }

        public void cachePicture() {
            Activity a = getActivity();
            if (a == null)
                return;

            Glide.with(a.getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(picture.path))
                    .downloadOnly(0,0);
        }
    }

}
