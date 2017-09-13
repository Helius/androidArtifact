package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class ChooseMovementGameFragment extends BaseGameFragment {

    public static final String TAG = "ChooseMovements";

    private ImageView mImageView;
    private Random rnd;
    private boolean buttonBlocked;
    private TextButtonAdapter mAdapter;
    private ArrayList<ChooseMovementGame> games = null;


    private void init () {
        sessionStatistic = new BaseGameStatistic();
        gameIndex = 0;
        rnd = new Random(System.currentTimeMillis());
        buttonBlocked = false;
    }

    public ChooseMovementGameFragment() {
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
        View view = inflater.inflate(R.layout.fragment_choose_movement_game, container, false);
        mImageView = (ImageView) view.findViewById(R.id.main_pic);
        GridView mGridView = (GridView) view.findViewById(R.id.choose_button_grid_view);
        mAdapter = new TextButtonAdapter(getContext());
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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        playGame(gameIndex);
        getActivity().setTitle(R.string.game_3_title);
    }

    private void buttonSelected(int ind) {

        if (buttonBlocked)
            return;
        buttonBlocked = true;
        int timeout = 500;
        boolean result = false;
        if (games.get(gameIndex).picture.movement_id == games.get(gameIndex).movement_variant.get(ind).id) {
            // Right )
            result = true;
            mAdapter.getButton(ind).state = TextButton.State.True;
        } else {
            // Fail (
            timeout = 1500;
            mAdapter.getButton(ind).state = TextButton.State.False;

            for(TextButton button : mAdapter.getButtons()) {
                if(button.id == games.get(gameIndex).picture.movement_id) {
                    button.state = TextButton.State.True;
                }
            }
        }
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
        super.finishButtonPressed();
        games = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        games = createNewGame(gameCount);
        init();
    }


    private void playGame(int gameIndex) {
        if (gameIndex < games.size()) {
//            Log.d(TAG, "play game " + gameIndex);
            ChooseMovementGame game = games.get(gameIndex);
            game.loadPicture();
            mAdapter.clearButton();
            for (Movement movement : game.movement_variant) {
                if (locale.equals("ru")) {
                    mAdapter.addNewButton(new TextButton(movement.name_ru, movement.id));
                } else {
                    mAdapter.addNewButton(new TextButton(movement.name_en, movement.id));
                }
            }
            mAdapter.update();

            if (gameIndex + 1 < games.size()) {
                games.get(gameIndex + 1).loadPicture();
            }
        } else { // we played all game and now just show last one
            ChooseMovementGame game = games.get(gameIndex-1);
            game.loadPicture();
            mAdapter.update();
        }
    }



    ArrayList<ChooseMovementGame> createNewGame(int count)
    {
//        Log.d(TAG, "create new " + count + "games");
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
        ArrayList<ChooseMovementGame> games = new ArrayList<>();

        ArrayList<Picture> tmp_pic = new ArrayList<>();
        for (Picture pic: dataProvider.getPictures()) {
            if (pic.movement_id != 0) {
                tmp_pic.add(pic);
            }
        }
        Collections.shuffle(tmp_pic);


        for (int i = 0; i < count; i++) {
            ChooseMovementGame game = new ChooseMovementGame(i);
            if (tmp_pic.size() > 0) {
                game.picture = tmp_pic.remove(rnd.nextInt(tmp_pic.size()));
                game.movement_variant.add(dataProvider.getMovementById(game.picture.movement_id));
                int movement_count = 3;
                while (movement_count > 0) {
                    Movement a = dataProvider.getMovements().get(rnd.nextInt(dataProvider.getMovements().size()));
                    if (!game.movement_variant.contains(a)) {
                        game.movement_variant.add(a);
                        movement_count--;
                    }
                }
                Collections.shuffle(game.movement_variant, rnd);
            }
            games.add(game);
        }
        games.get(0).loadPicture();
        games.get(1).loadPicture();
        return games;
    }


    class ChooseMovementGame {
        Picture picture;
        ArrayList<Movement> movement_variant = new ArrayList<>();
        private int id;


        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>(300,300) {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
//                Log.d(TAG, "helius: image " + id + "loaded from ??");
                if (id == gameIndex) {
                    mImageView.setImageBitmap(bitmap);
                    showButtonBlock(true);
                    View v = getView();
                    if (v != null) {
                        getView().findViewById(R.id.progress_view).setVisibility(View.GONE);
                    }
                }
            }
        };

        ChooseMovementGame(int id) {
            this.id = id;
        }

        void loadPicture() {
            final View v = getView();
            if (v == null)
                return;
            v.findViewById(R.id.progress_view).setVisibility(View.VISIBLE);
            mStorageRef.child(picture.path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Activity a = getActivity();
                    if (a == null)
                        return;

                    Glide.with(a.getApplicationContext())
                            .load(uri.toString())
                            .asBitmap()
                            .into(target);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
    }

}
