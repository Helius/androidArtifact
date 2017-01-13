package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

import static com.ghelius.artifacts.artifacts.ChoosePaintGameFragment.ButtonState.False;
import static com.ghelius.artifacts.artifacts.ChoosePaintGameFragment.ButtonState.Hide;
import static com.ghelius.artifacts.artifacts.ChoosePaintGameFragment.ButtonState.Normal;
import static com.ghelius.artifacts.artifacts.ChoosePaintGameFragment.ButtonState.True;


public class ChoosePaintGameFragment extends Fragment implements GameSetFinishedDialog.DialogEventListener {

    private static final String TAG = "ChooseGameGame";

    private ArrayList<Picture> pictures;
    private ArrayList<Author> authors;

    private StorageReference mStorageRef;
    private TextView author_view;
    private Random rnd;
    private boolean buttonBlocked;
    private ArrayList<ChooseButton> mButtons;
    private ButtonAdapter mButtonAdapter;
    private ArrayList<ChoosePaintGame> games = null;
    private int gameIndex;
    GameSetFinishedDialog dialog;
    private int GameCount = 10;
    private int trueAnswerCount;
    private String locale;

    enum ButtonState {Normal, True, Hide, False}



    private class ChooseButton {
        Picture picture;
        ButtonState state;
        Long author_id;

        ChooseButton(Picture picture, Long author_id) {
            this.picture = picture;
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
                view = mInflater.inflate(R.layout.choose_paint_item, viewGroup, false);
            }
            ChooseButton button = mButtons.get(i);
            final ImageView pic = (ImageView) view.findViewById(R.id.picture);
            mStorageRef.child(button.picture.path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Activity a = getActivity();
                    if (a == null)
                        return;

                    Glide.with(a.getApplicationContext())
                            .load(uri.toString())
                            .asBitmap()
                            .centerCrop()
                            .into(pic);

                }
            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {

                                        }
                                    }
            );

            switch (button.state) {
                case True:
                    break;
                case False:
                    pic.setAlpha(127);
                    break;
                case Hide:
                    pic.setAlpha(0);
                    break;
                default:
                    pic.setAlpha(255);
                    break;
            }
            return view;
        }

        void update(int i) {
            notifyDataSetChanged();
        }
    }




    private void init () {
        gameIndex = 0;
        trueAnswerCount = 0;
        rnd = new Random(System.currentTimeMillis());
        mButtons = new ArrayList<>();
        buttonBlocked = false;
    }

    public ChoosePaintGameFragment() {
        // Required empty public constructor
        init();
    }


    private void showButtonBlock(boolean show) {
        View v = getView();
        if (v == null)
            return;
        if (show) {
//            v.findViewById(R.id.choose_button_grid_view).setVisibility(View.VISIBLE);
        } else {
//            v.findViewById(R.id.choose_button_grid_view).setVisibility(View.INVISIBLE);
        }
        buttonBlocked = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        locale = Locale.getDefault().getLanguage();
        View view = inflater.inflate(R.layout.fragment_choose_paint_game, container, false);
        GridView mGridView = (GridView) view.findViewById(R.id.paint_grid);
        author_view = (TextView) view.findViewById(R.id.author);
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
            games = createNewGame(GameCount);
        }
        dialog = (GameSetFinishedDialog) getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (dialog == null) {
            dialog = new GameSetFinishedDialog();
        }
        dialog.init(trueAnswerCount, GameCount, 0, false);
        dialog.setEventListener(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        playGame(gameIndex);
    }

    private void buttonSelected(int ind) {

        if (buttonBlocked)
            return;
        buttonBlocked = true;
        int timeout = 500;

        if (games.get(gameIndex).author.id == games.get(gameIndex).picture_variant.get(ind).author) {
            // Right )
            trueAnswerCount++;
            for(int i = 0; i < mButtons.size(); ++i) {
                if (i != ind) {
                    mButtons.get(i).state = Hide;
                }
            }
        } else {
            // Fail (
            timeout = 1500;
            mButtons.get(ind).state = False;

            for(ChooseButton button : mButtons) {
                if(button.author_id == games.get(gameIndex).author.id) {
                    button.state = True;
                }
            }

            for(int i = 0; i < mButtons.size(); ++i) {
                if (i != ind && mButtons.get(i).state != True) {
                    mButtons.get(i).state = False;
                }
            }
//            mButtons.get(ind).state = False;
//
        }
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
                    dialog.init(trueAnswerCount, GameCount, 0, false);
                    dialog.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            }
        }, timeout);

    }

    @Override
    public void moreButtonPressed() {
        games = createNewGame(GameCount);
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
        setRetainInstance(true);
        init();
    }

    public void setServerResources(ArrayList<Picture> pictures, ArrayList<Author> authors) {
        this.pictures = pictures;
        this.authors = authors;
    }

    private void playGame(int gameIndex) {
        if (gameIndex < games.size()) {
            Log.d(TAG, "play game " + gameIndex);
            ChoosePaintGame game = games.get(gameIndex);
            mButtons.clear();
            for (Picture pic : game.picture_variant) {
                mButtons.add(new ChooseButton(pic, pic.author));
            }
            if (locale.equals("ru")) {
                author_view.setText(game.author.name_ru);
            } else {
                author_view.setText(game.author.name_en);
            }
        } else { // we played all game and now just show last one
        }
        mButtonAdapter.update(0);
    }



    ArrayList<ChoosePaintGame> createNewGame(int count)
    {
        Log.d(TAG, "create new " + count + "games");
        gameIndex = 0;
        trueAnswerCount = 0;
        ArrayList<ChoosePaintGame> games = new ArrayList<>();

        ArrayList<Picture> tmp_pic = new ArrayList<>();
        tmp_pic.addAll(pictures);
        Collections.shuffle(tmp_pic);

        for (int i = 0; i < count; i++) {
            ChoosePaintGame game = new ChoosePaintGame(i);
            if (tmp_pic.size() > 0) {
                game.picture_variant.add(tmp_pic.remove(rnd.nextInt(tmp_pic.size())));
                game.author = getAuthorById(game.picture_variant.get(0).author);


                int pic_count = 3;
                while (pic_count > 0) {
                    Picture pic = pictures.get(rnd.nextInt(pictures.size()));
                    if (!game.picture_variant.contains(pic) && game.author.id != pic.author) {
                        game.picture_variant.add(pic);
                        pic_count--;
                    }
                }
                Collections.shuffle(game.picture_variant, rnd);
            }
            games.add(game);
        }
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

    class ChoosePaintGame {
        Author author;
        ArrayList<Picture> picture_variant;
        private int id;


//        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>(300,300) {
//            @Override
//            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
//                Log.d(TAG, "helius: image " + id + "loaded from ??");
//                if (id == gameIndex) {
//                    mImageView.setImageBitmap(bitmap);
//                    showButtonBlock(true);
//                    View v = getView();
//                    if (v != null) {
//                        getView().findViewById(R.id.progress_view).setVisibility(View.GONE);
//                    }
//                }
//            }
//        };

        ChoosePaintGame(int id) {
            this.id = id;
            this.picture_variant = new ArrayList<>();
        }

//        void loadPicture(int index, final ImageView view) {
//            mStorageRef.child(picture_variant.get(index).path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                @Override
//                public void onSuccess(Uri uri) {
//                    Activity a = getActivity();
//                    if (a == null)
//                        return;
//
//                    Glide.with(a.getApplicationContext())
//                            .load(uri.toString())
//                            .asBitmap()
//                            .into(view);
//
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // Handle any errors
//                }
//            });
//        }
    }

}
