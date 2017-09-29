package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.Locale;
import java.util.Random;

public class TypeAuthorGameFragment extends BaseGameFragment {

    public static final String TAG = "TypeAuthor";

    private ArrayList<Game> games;
    private ImageView mImageView;
    Random rnd;
    AutoCompleteTextView mEditText;
    private View loader;
    private TextView authorHint;

    public TypeAuthorGameFragment() {
        // Required empty public constructor
        rnd = new Random(System.currentTimeMillis());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        final InputMethodManager imm = (InputMethodManager) getActivity().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        games = createNewGame(gameCount);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        if (games == null) {
            sessionStatistic = new BaseGameStatistic();
            games = createNewGame(gameCount);
        }

        View v = inflater.inflate(R.layout.fragment_type_author_game, container, false);
        mImageView = (ImageView) v.findViewById(R.id.main_pic);
        mEditText = (AutoCompleteTextView) v.findViewById(R.id.text_input);
        ArrayList<String> suggestList = new ArrayList<>();
        for(Author a: dataProvider.getAuthors()) {
            suggestList.add(a.getName("ru"));
            suggestList.add(a.getName("en"));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, suggestList);
        mEditText.setAdapter(adapter);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                            || (i == EditorInfo.IME_ACTION_DONE)) {
                        checkResult();
                    }
                return false;
            }
        });
        mEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                checkResult();
            }
        });
        loader = v.findViewById(R.id.progress_view);
        authorHint = (TextView)v.findViewById(R.id.author_name_hint);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        getActivity().setTitle(R.string.game_1_title);
        playGame(gameIndex);
    }

    private void checkResult() {
        boolean result = false;
        int timeout = 1200;
        if (mEditText.getText().toString().equals(games.get(gameIndex).author.getName("ru")) ||
                mEditText.getText().toString().equals(games.get(gameIndex).author.getName("en"))) {
            result = true;
            timeout = 1;
        } else {
//            mImageView.setImageBitmap(null);
            authorHint.setVisibility(View.VISIBLE);
            authorHint.setText(games.get(gameIndex).author.getName());
        }
        addToHistory(new GameHistory.GameHistoryItem(games.get(gameIndex).picture, result));
        sessionStatistic.addAttempt(result);
        userData.updateGameStatistic(getContext(), games.get(gameIndex).picture, result, TAG);
        mEditText.getText().clear();

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                playGame(++gameIndex);
            }
        }, timeout);
    }


    private void playGame (int index) {
        loader.setVisibility(View.VISIBLE);
        authorHint.setText("");
        authorHint.setVisibility(View.INVISIBLE);
        if (index < games.size()) {
            games.get(index).loadPicture();
            if (index + 1 < games.size()) {
                games.get(index + 1).loadPicture();
            }
        } else {
            dialog.init(sessionStatistic, userData);
            dialog.show(getActivity().getSupportFragmentManager(), "dialog");
        }
    }

    private ArrayList<Game> createNewGame(int num)
    {
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
        ArrayList<Game> games = new ArrayList<>();
        ArrayList<Author> tmp_authors = new ArrayList<>();

        tmp_authors.addAll(dataProvider.getAuthors());
        Collections.shuffle(tmp_authors);

        for (int i = 0; i < num; ++i) {
            // create game with random author
            int index = rnd.nextInt(tmp_authors.size());
            games.add(new Game(tmp_authors.get(index), i));
            tmp_authors.remove(index);
        }
        return games;
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

    class Game {
        Author author;
        Picture picture;
        private int id;

         SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>(600,600) {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
//                Log.d(TAG, "helius: image " + id + "loaded from ??");
                if (id == gameIndex) {
                    loader.setVisibility(View.GONE);
                    mImageView.setImageBitmap(bitmap);
                }
            }
        };

        Game (Author a, int id) {
            this.author = a;
            this.id = id;
            ArrayList<Picture> pic = new ArrayList<>();
            // find random picture of given author
            for (Picture p : dataProvider.getPictures()) {
                if (p.author == author.id) {
                    pic.add(p);
                }
            }
            picture = pic.get(rnd.nextInt(pic.size()));
        }

        void loadPicture() {
            Activity a = getActivity();
            if (a == null)
                return;


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
