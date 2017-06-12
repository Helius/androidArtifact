package com.ghelius.artifacts.artifacts;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;


public class ChooseMovementGameFragment extends Fragment implements GameSetFinishedDialog.DialogEventListener{

    public static final String TAG = "ChooseMovements";

    private ArrayList<Picture> pictures;
    private ArrayList<Movement> movements;

    private StorageReference mStorageRef;
    private ImageView mImageView;
    private Random rnd;
    private boolean buttonBlocked;
    private ArrayList<ChooseButton> mButtons;
    private ButtonAdapter mButtonAdapter;
    private ArrayList<ChooseMovementGame> games = null;
    private int gameIndex;
    GameSetFinishedDialog dialog;
    private int gameCount = 10;
    private BaseGameStatistic sessionStatistic;
    private String locale;
    private UserData userData;
    private GameDataProvider gameDataProvider;

    enum ButtonState {Normal, True, False};

    private class ChooseButton {
        String text;
        ButtonState state;
        int movement_id;

        ChooseButton(String text, int movement_id) {
            this.text = text;
            this.state = ButtonState.Normal;
            this.movement_id = movement_id;
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
        gameIndex = 0;
        rnd = new Random(System.currentTimeMillis());
        mButtons = new ArrayList<>();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_movement_game, container, false);
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
        dialog = (GameSetFinishedDialog) getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (dialog == null) {
            dialog = new GameSetFinishedDialog();
        }
        dialog.init(sessionStatistic, userData.getGameStatistic(TAG), userData.getLevel());
        dialog.setEventListener(this);
        locale = Locale.getDefault().getLanguage();

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
        getActivity().setTitle(R.string.game_3_title);
    }

    private void buttonSelected(int ind) {

        if (buttonBlocked)
            return;
        buttonBlocked = true;
        int timeout = 500;
        sessionStatistic.addAttempt();
        if (games.get(gameIndex).picture.movement_id == games.get(gameIndex).movement_variant.get(ind).id) {
            // Right )
            sessionStatistic.addRight();
            mButtons.get(ind).state = ButtonState.True;
        } else {
            // Fail (
            timeout = 1500;
            mButtons.get(ind).state = ButtonState.False;

            for(ChooseButton button : mButtons) {
                if(button.movement_id == games.get(gameIndex).picture.movement_id) {
                    button.state = ButtonState.True;
                }
            }
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
                    dialog.init(sessionStatistic, userData.getGameStatistic(TAG), userData.getLevel());
                    userData.updateGameStatistic(TAG, sessionStatistic);
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
        setRetainInstance(true);
        games = createNewGame(gameCount);
        init();
    }

    public void setServerResources(UserData userData, GameDataProvider gameDataProvider) {
        this.gameDataProvider = gameDataProvider;
        this.userData = userData;
        this.pictures = gameDataProvider.getPictures();
        this.movements = gameDataProvider.getMovements();
    }

    private void playGame(int gameIndex) {
        if (gameIndex < games.size()) {
            Log.d(TAG, "play game " + gameIndex);
            ChooseMovementGame game = games.get(gameIndex);
            game.loadPicture();
            mButtons.clear();
            for (Movement movement : game.movement_variant) {
                if (locale.equals("ru")) {
                    mButtons.add(new ChooseButton(movement.name_ru, movement.id));
                } else {
                    mButtons.add(new ChooseButton(movement.name_en, movement.id));
                }
            }
            mButtonAdapter.update(0);

            if (gameIndex + 1 < games.size()) {
                games.get(gameIndex + 1).loadPicture();
            }
        } else { // we played all game and now just show last one
            ChooseMovementGame game = games.get(gameIndex-1);
            game.loadPicture();
            mButtonAdapter.update(0);
        }
    }



    ArrayList<ChooseMovementGame> createNewGame(int count)
    {
        Log.d(TAG, "create new " + count + "games");
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
        ArrayList<ChooseMovementGame> games = new ArrayList<>();

        ArrayList<Picture> tmp_pic = new ArrayList<>();
        for (Picture pic: pictures) {
            if (pic.movement_id != 0) {
                tmp_pic.add(pic);
            }
        }
        Collections.shuffle(tmp_pic);


        for (int i = 0; i < count; i++) {
            ChooseMovementGame game = new ChooseMovementGame(i);
            if (tmp_pic.size() > 0) {
                game.picture = tmp_pic.remove(rnd.nextInt(tmp_pic.size()));
                game.movement_variant.add(gameDataProvider.getMovementById(game.picture.movement_id));
                int movement_count = 3;
                while (movement_count > 0) {
                    Movement a = movements.get(rnd.nextInt(movements.size()));
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
                Log.d(TAG, "helius: image " + id + "loaded from ??");
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
