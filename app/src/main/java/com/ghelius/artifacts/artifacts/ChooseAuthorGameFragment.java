package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class ChooseAuthorGameFragment extends Fragment {

    private static final String TAG = "ChooseAuthor";

    private ArrayList<Picture> pictures;
    private ArrayList<Author> authors;

    private StorageReference mStorageRef;
    private ImageView mImageView;
    private Random rnd;
    private ArrayList<TextView> buttons;
    private boolean buttonBlocked;

    private ArrayList<ChooseAuthorGame> games;
    private int gameIndex;
    GameSetFinishedDialog dialog;
    private int GameCount = 10;
    private int trueAnswerCount;

    private void init () {
        gameIndex = 0;
        trueAnswerCount = 0;
        rnd = new Random(System.currentTimeMillis());
        buttons = new ArrayList<>();
        buttonBlocked = false;
    }

    public ChooseAuthorGameFragment() {
        // Required empty public constructor
        init();
    }


    private void showButtonBlock(boolean b) {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setVisibility(b ? View.VISIBLE : View.INVISIBLE);
        }
        buttonBlocked = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_author_game, container, false);
        mImageView = (ImageView) view.findViewById(R.id.main_pic);

        buttons.add((TextView)view.findViewById(R.id.button_0));
        buttons.add((TextView)view.findViewById(R.id.button_1));
        buttons.add((TextView)view.findViewById(R.id.button_2));
        buttons.add((TextView)view.findViewById(R.id.button_3));

        for (int i = 0; i < buttons.size(); i++) {
            final int index = i;
            buttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buttonSelected(index);
                }
            });
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        games = createNewGame(GameCount);
        playGame(gameIndex);
    }

    private void buttonSelected(int ind) {

        if (buttonBlocked)
            return;
        buttonBlocked = true;
        int timeout = 500;

        if (games.get(gameIndex).picture.author == games.get(gameIndex).authors_variant.get(ind).id) {
            // Right )
            trueAnswerCount++;
            buttons.get(ind).setTextColor(0xFF09AD1F);
        } else {
            // Fail (
            timeout = 1500;
            buttons.get(ind).setTextColor(
                    ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark));

            for (int i = 0; i < buttons.size(); i++) {
                if(((Author)buttons.get(i).getTag()).id == games.get(gameIndex).picture.author) {
                    buttons.get(i).setTextColor(0xFF09AD1F);
                }
            }
        }

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {

                ++gameIndex;
                showButtonBlock(true);
                if (gameIndex < games.size()) {
                    showButtonBlock(false);
                    playGame(gameIndex);
                } else {
                    dialog = new GameSetFinishedDialog();
                    dialog.init(trueAnswerCount, GameCount, 0, false);
                    dialog.show(getFragmentManager(), "dialog");
                    dialog.setEventListener(new GameSetFinishedDialog.DialogEventListener() {
                        @Override
                        public void moreButtonPressed() {
                            games = createNewGame(GameCount);
                            playGame(gameIndex);
                        }

                        @Override
                        public void finishButtonPressed() {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    });
                }
            }
        }, timeout);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public void setServerResources(ArrayList<Picture> pictures, ArrayList<Author> authors) {
        this.pictures = pictures;
        this.authors = authors;
    }

    private void playGame(int gameIndex) {

        Log.d(TAG, "play game " + gameIndex);
        ChooseAuthorGame game = games.get(gameIndex);
        game.loadPicture();
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setText(game.authors_variant.get(i).name_ru);
            buttons.get(i).setTag(game.authors_variant.get(i));
            buttons.get(i).setTextColor(ContextCompat.getColor(getActivity(), android.R.color.tab_indicator_text));
        }

        if (gameIndex + 1 < games.size()) {
            games.get(gameIndex + 1).loadPicture();
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        init();
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    ArrayList<ChooseAuthorGame> createNewGame(int count)
    {
        Log.d(TAG, "create new " + count + "games");
        gameIndex = 0;
        trueAnswerCount = 0;
        ArrayList<ChooseAuthorGame> games = new ArrayList<>();

        ArrayList<Picture> tmp_pic = new ArrayList<>();
        tmp_pic.addAll(pictures);
        Collections.shuffle(tmp_pic);

        for (int i = 0; i < count; i++) {
            ChooseAuthorGame game = new ChooseAuthorGame(i);
            if (tmp_pic.size() > 0) {
                game.picture = tmp_pic.remove(rnd.nextInt(tmp_pic.size()));
                game.authors_variant.add(getAuthorById(game.picture.author));
                int author_count = 3;
                while (author_count > 0) {
                    Author a = authors.get(rnd.nextInt(authors.size()));
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


    private Author getAuthorById (Long id) {
        for (Author a : authors) {
            if (a.id == id) {
                return a;
            }
        }
        return null;
    }

    class ChooseAuthorGame {
        public Picture picture;
        public ArrayList<Author> authors_variant = new ArrayList<>();
        private int index;

        Target imageTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(TAG, "helius: image " + index + "loaded from " + from);
                if (index == gameIndex) {
                    mImageView.setImageBitmap(bitmap);
                    showButtonBlock(true);
                    View v = getView();
                    if (v != null) {
                        getView().findViewById(R.id.progress_view).setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        ChooseAuthorGame(int index) {
            this.index = index;
        }

        public void loadPicture() {
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
                    Picasso.with(a.getApplicationContext())
                            .load(uri.toString())
                            .resize(mImageView.getWidth(),0)
                            .into(imageTarget);
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
