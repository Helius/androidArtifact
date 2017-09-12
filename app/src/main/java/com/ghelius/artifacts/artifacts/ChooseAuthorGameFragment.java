package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class ChooseAuthorGameFragment extends BaseGameFragment {

    public static final String TAG = "ChooseAuthor";

    private StorageReference mStorageRef;
    private ImageView mImageView;
    private Random rnd;
    private boolean buttonBlocked;
    private ArrayList<ChooseButton> mButtons;
    private ButtonAdapter mButtonAdapter;
    private ArrayList<ChooseAuthorGame> games = null;

    enum ButtonState {Normal, True, False};

    private class ChooseButton {
        String text;
        ButtonState state;
        int author_id;

        ChooseButton(String text, int author_id) {
            this.text = text;
            this.state = ButtonState.Normal;
            this.author_id = author_id;
        }
    }


    private class ButtonAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private ArrayList<ChooseButton> mButtons;

        ButtonAdapter(Context context, ArrayList<ChooseButton> buttons) {
            mButtons = buttons;
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mButtons.size();
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
                view = mInflater.inflate(R.layout.choose_button_item, viewGroup, false);
            }
            TextView text = (TextView) view.findViewById(R.id.text);
            ChooseButton button = mButtons.get(i);
            text.setText(button.text);
            switch (button.state) {
                case True:
                    text.setBackgroundResource(R.drawable.choose_button_true_background_shape);
                    break;
                case False:
                    text.setBackgroundResource(R.drawable.choose_button_false_background_shape);
                    break;
                default:
                    text.setBackgroundResource(0);
                    break;
            }

            return view;
        }

        void update(int i) {
            notifyDataSetChanged();
        }
    }


    private void init () {
        sessionStatistic = new BaseGameStatistic();
        rnd = new Random(System.currentTimeMillis());
        mButtons = new ArrayList<>();
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
        GridView mGridView = (GridView) view.findViewById(R.id.choose_button_grid_view);
        mButtons = new ArrayList<>();
        mButtonAdapter = new ButtonAdapter(getActivity().getApplicationContext(), mButtons);
        mGridView.setAdapter(mButtonAdapter);
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
            mButtons.get(ind).state = ButtonState.True;
        } else {
            // Fail (
            timeout = 1500;
            mButtons.get(ind).state = ButtonState.False;

            for(ChooseButton button : mButtons) {
                if(button.author_id == games.get(gameIndex).picture.author) {
                    button.state = ButtonState.True;
                }
            }
        }
        sessionStatistic.addAttempt(result);
        userData.updateGameStatistic(getContext(), games.get(gameIndex).picture, result, TAG);
        mButtonAdapter.update(ind);

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
        games = null;
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        games = createNewGame(gameCount);
        init();
    }


    private void playGame(int gameIndex) {
        if (gameIndex < games.size()) {
//            Log.d(TAG, "play game " + gameIndex);
            ChooseAuthorGame game = games.get(gameIndex);
            game.loadPicture();
            mButtons.clear();
            for (Author author : game.authors_variant) {
                if (locale.equals("ru")) {
                    mButtons.add(new ChooseButton(author.name_ru, author.id));
                } else {
                    mButtons.add(new ChooseButton(author.name_en, author.id));
                }
            }
            mButtonAdapter.update(0);

            if (gameIndex + 1 < games.size()) {
                games.get(gameIndex + 1).loadPicture();
            }
        } else { // we played all game and now just show last one
            ChooseAuthorGame game = games.get(gameIndex-1);
            game.loadPicture();
            mButtonAdapter.update(0);
        }
    }



    ArrayList<ChooseAuthorGame> createNewGame(int count)
    {
//        Log.d(TAG, "create new " + count + "games");
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
        ArrayList<ChooseAuthorGame> games = new ArrayList<>();

        ArrayList<Picture> tmp_pic = new ArrayList<>();
        tmp_pic.addAll(gameDataProvider.getPictures());
        Collections.shuffle(tmp_pic);

        for (int i = 0; i < count; i++) {
            ChooseAuthorGame game = new ChooseAuthorGame(i);
            if (tmp_pic.size() > 0) {
                game.picture = tmp_pic.remove(rnd.nextInt(tmp_pic.size()));
                game.authors_variant.add(gameDataProvider.getAuthorById(game.picture.author));
                int author_count = 3;
                while (author_count > 0) {
                    Author a = gameDataProvider.getAuthors().get(rnd.nextInt(gameDataProvider.getAuthors().size()));
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
        games.get(1).loadPicture();
        return games;
    }

    class ChooseAuthorGame {
        Picture picture;
        ArrayList<Author> authors_variant = new ArrayList<>();
        private int id;


        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>(600,600) {
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

        ChooseAuthorGame(int id) {
            this.id = id;
        }

        void loadPicture() {
            Activity a = getActivity();
            if (a == null)
                return;

            final View v = getView();
            if (v == null)
                return;
            if (id == gameIndex) {
                getView().findViewById(R.id.progress_view).setVisibility(View.VISIBLE);
            }
            Glide.with(a.getApplicationContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(picture.path))
                    .asBitmap()
                    .override(600,600)
                    .fitCenter()
                    .into(target);
        }
    }

}
