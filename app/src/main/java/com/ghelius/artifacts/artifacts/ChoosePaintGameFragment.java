package com.ghelius.artifacts.artifacts;


import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import static com.ghelius.artifacts.artifacts.ChoosePaintGameFragment.ButtonState.False;
import static com.ghelius.artifacts.artifacts.ChoosePaintGameFragment.ButtonState.True;


public class ChoosePaintGameFragment extends Fragment implements GameSetFinishedDialog.DialogEventListener {

    private static final String TAG = "ChoosePaint";

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
    ImageView fullImage;
    private int gameCount = 10;
    BaseGameStatistic sessionStatistic;
    private String locale;
    private boolean fullShowed = false;
    private Drawable background;
    private UserData userData;

    enum ButtonState {Normal, True, Hide, False}



    private class ChooseButton {
        Picture picture;
        ButtonState state;
        Long author_id;
        String url;
        Bitmap cachedBitmap;

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
        public View getView(int i, View view, final ViewGroup viewGroup) {
            if (view == null) {
                view = mInflater.inflate(R.layout.choose_paint_item, viewGroup, false);
            }
//            Log.d(TAG, "getView " + i);
            final ChooseButton button = mButtons.get(i);
            final ImageView pic = (ImageView) view.findViewById(R.id.picture);
            pic.setImageBitmap(mButtons.get(i).cachedBitmap);
            switch (button.state) {
                case True:
                    break;
                case False:
                    pic.setAlpha(50);
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
            Log.d(TAG, "update grid");
            for (ChooseButton button : mButtons) {
                button.url = null;
            }
            notifyDataSetChanged();
        }
    }




    private void init () {
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
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
        final GridView mGridView = (GridView) view.findViewById(R.id.paint_grid);
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
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showFullImageDialog(i);
                return true;
            }
        });
        mGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
//                    case MotionEvent.ACTION_CANCEL:
                        hideFullImageDialog();
//                        mGridView.clearChoices();
                        return false;
//                    case MotionEvent.ACTION_DOWN:
                    default:
                        break;

                }
                return false;
            }
        });

        if (games == null) {
            games = createNewGame(gameCount);
        }
        dialog = (GameSetFinishedDialog) getActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (dialog == null) {
            dialog = new GameSetFinishedDialog();
        }
        dialog.init(userData.getGameStatistic(TAG), sessionStatistic, userData.getLevel());
        dialog.setEventListener(this);

        fullImage = (ImageView) view.findViewById(R.id.full_image);

        View backgroundView = view.findViewById(R.id.full_image_fade_background);
        background = backgroundView.getBackground();
        background.setAlpha(0);

        return view;
    }

    private void hideFullImageDialog() {

        Activity a = getActivity();
        if (a == null)
            return;

        if (!fullShowed) {
            return;
        }
        fullShowed = false;

        int width  = fullImage.getMeasuredWidth();
        int height  = fullImage.getMeasuredHeight();
        SizeChangeAnimation anim = new SizeChangeAnimation(fullImage);
        anim.setHeights(width, 0);
        anim.setWidths(height, 0);
        anim.setDuration(100);
        fullImage.startAnimation(anim);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(background,
                PropertyValuesHolder.ofInt("alpha", 160, 0));
        animator.setTarget(background);
        animator.setDuration(100);
        animator.start();
    }

    private void showFullImageDialog(final int i) {
        Activity a = getActivity();
        if (a == null)
            return;

        fullShowed = true;
        fullImage.setVisibility(View.VISIBLE);
        fullImage.setImageBitmap(mButtons.get(i).cachedBitmap);
        View layout = getView().findViewById(R.id.choose_pict_root_layout);
        int width  = layout.getMeasuredWidth();
        int height  = layout.getMeasuredHeight();
        SizeChangeAnimation anim = new SizeChangeAnimation(fullImage);
        anim.setHeights(10, width);
        anim.setWidths(10, height);
        anim.setDuration(150);
        fullImage.startAnimation(anim);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(background,
                PropertyValuesHolder.ofInt("alpha", 0, 160));
        animator.setTarget(background);
        animator.setDuration(200);
        animator.start();
    }
public class SizeChangeAnimation extends Animation {

    private int startHeight;
    private int deltaHeight;

    private int startWidth;
    private int deltaWidth;

    private View view;

    public SizeChangeAnimation(View view) {
        this.view = view;
    }

    public void setHeights(int start, int end) {
        this.startHeight = start;
        this.deltaHeight = end - this.startHeight;
    }

    public void setWidths(int start, int end) {
        this.startWidth = start;
        this.deltaWidth = end - this.startWidth;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (startHeight != 0) {
            if (deltaHeight > 0) {
                view.getLayoutParams().height = (int) (startHeight + deltaHeight * interpolatedTime);
            } else {
                view.getLayoutParams().height = (int) (startHeight - Math.abs(deltaHeight) * interpolatedTime);
            }
        }

        if (startWidth != 0) {
            if (deltaWidth > 0) {
                view.getLayoutParams().width = (int) (startWidth + deltaWidth * interpolatedTime);
            } else {
                view.getLayoutParams().width = (int) (startWidth - Math.abs(deltaWidth) * interpolatedTime);
            }
        }

        view.requestLayout();
    }
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

        sessionStatistic.addAttempt();
        // Right )
        if (games.get(gameIndex).author.id == games.get(gameIndex).picture_variant.get(ind).author) {
            sessionStatistic.addRight();
        // Fail (
        } else {
            timeout = 1500;
        }
        for(ChooseButton button : mButtons) {
            if(button.author_id == games.get(gameIndex).author.id) {
                button.state = True;
            }
            else {
                button.state = False;
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
        init();
    }

    public void setServerResources(UserData userData, ArrayList<Picture> pictures, ArrayList<Author> authors) {
        this.userData = userData;
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
        loadAllImages();
    }

    void loadAllImages() {
        for (final ChooseButton b : mButtons) {
            Glide.with(getActivity())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(b.picture.path))
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                            b.cachedBitmap = resource;
                            mButtonAdapter.update(0);
                        }
                    });
        }
    }

    ArrayList<ChoosePaintGame> createNewGame(int count)
    {
        Log.d(TAG, "create new " + count + "games");
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
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
        public HashMap<String, Bitmap> cachedPicture;

        ChoosePaintGame(int id) {
            this.id = id;
            this.picture_variant = new ArrayList<>();
            this.cachedPicture = new HashMap<>();
        }
    }
}
