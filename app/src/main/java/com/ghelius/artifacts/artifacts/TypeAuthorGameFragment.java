package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import java.util.Random;

public class TypeAuthorGameFragment extends Fragment implements GameSetFinishedDialog.DialogEventListener {

    public static final String TAG = "TypeAuthor";

    private ArrayList<Picture> pictures;
    private ArrayList<Author> authors;
    private ArrayList<Game> games;
    private int gameCount = 10;
    private int gameIndex = 0;
    private ImageView mImageView;
    Random rnd;
    private StorageReference mStorageRef;
    AutoCompleteTextView mEditText;
    private View loader;
    GameSetFinishedDialog dialog;
    BaseGameStatistic sessionStatistic;
    private TextView authorHint;
    private UserData userData;

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
        mStorageRef = FirebaseStorage.getInstance().getReference();
        setRetainInstance(true);
        games = createNewGame(gameCount);
        sessionStatistic = new BaseGameStatistic();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (games == null) {
            sessionStatistic = new BaseGameStatistic();
            games = createNewGame(gameCount);
        }

        dialog = (GameSetFinishedDialog) getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (dialog == null) {
            dialog = new GameSetFinishedDialog();
        }
        dialog.init(sessionStatistic, userData.getGameStatistic(TAG), userData.getLevel());
        dialog.setEventListener(this);

        View v = inflater.inflate(R.layout.fragment_type_author_game, container, false);
        mImageView = (ImageView) v.findViewById(R.id.main_pic);
        mEditText = (AutoCompleteTextView) v.findViewById(R.id.text_input);
        ArrayList<String> suggestList = new ArrayList<>();
        for(Author a: authors) {
            suggestList.add(a.name_ru);
            suggestList.add(a.name_en);
            Log.d(TAG, a.name_en + " " + a.name_ru);
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
        playGame(gameIndex);


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
        getActivity().setTitle(R.string.game_1_title);
    }

    private void checkResult() {
        sessionStatistic.addAttempt();
        if (mEditText.getText().toString().equals(games.get(gameIndex).author.name_ru) ||
                mEditText.getText().toString().equals(games.get(gameIndex).author.name_en)) {
            sessionStatistic.addRight();
            playGame(++gameIndex);
        } else {
            //TODO: wrong! try again! (3 times)
            Log.d(TAG, "wrong! try again (" + games.get(gameIndex).author.name_ru);
            mImageView.setImageBitmap(null);
            authorHint.setText(games.get(gameIndex).author.name_ru);
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    playGame(++gameIndex);
                }
            }, 1200);
        }

        mEditText.getText().clear();
    }


    public void setServerResources(UserData userData, GameDataProvider gameDataProvider) {
        this.userData = userData;
        this.pictures = gameDataProvider.getPictures();
        this.authors = gameDataProvider.getAuthors();
    }

    private void playGame (int index) {
        loader.setVisibility(View.VISIBLE);
        authorHint.setText("");
        if (gameIndex < gameCount ) {
            games.get(index).loadPicture();
            if (index + 1 < gameCount) {
                games.get(index + 1).loadPicture();
            }
        } else {
            dialog.init(sessionStatistic, userData.getGameStatistic(TAG), userData.getLevel());
            userData.updateGameStatistic(TAG, sessionStatistic);
            dialog.show(getActivity().getSupportFragmentManager(), "dialog");
        }
    }

    private ArrayList<Game> createNewGame(int num)
    {
        ArrayList<Game> games = new ArrayList<>();
        for (int i = 0; i < num; ++i) {
            // create game with random author
            games.add(new Game(authors.get(rnd.nextInt(authors.size())), i));
        }
        return games;
    }

    @Override
    public void moreButtonPressed() {
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
        games = createNewGame(gameCount);
        playGame(gameIndex);
    }

    @Override
    public void finishButtonPressed() {
        games = null;
        getActivity().getSupportFragmentManager().popBackStack();
    }

    class Game {
        Author author;
        Picture picture;
        private int id;

         SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>(600,600) {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                Log.d(TAG, "helius: image " + id + "loaded from ??");
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
                    .into(target);
        }
    }

}
