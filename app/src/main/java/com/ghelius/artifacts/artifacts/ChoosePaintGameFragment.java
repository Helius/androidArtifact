package com.ghelius.artifacts.artifacts;


import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class ChoosePaintGameFragment extends BaseGameFragment{

    public static final String TAG = "ChoosePaint";

    private TextView author_view;
    private Random rnd;
    private boolean buttonBlocked;
    private ArrayList<ChooseButton> mButtons;
    private ButtonAdapter mButtonAdapter;
    private ArrayList<ChoosePaintGame> games = null;
    GameSetFinishedDialog dialog;
    ImageView fullImage;
    private boolean fullShowed = false;
    private Drawable background;

    enum ButtonState {Normal, True, Hide, False}

    private class ChooseButton {
        Picture picture;
        ButtonState state;
        int author_id;
        public boolean loaded = false;

        ChooseButton(Picture picture, int author_id) {
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
            final ImageView failMark = (ImageView) view.findViewById(R.id.fail_mark);
            pic.setImageResource(R.drawable.picture_dashed_placeholder);
                    Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(mStorageRef.child(button.picture.path))
                    .listener(new RequestListener<StorageReference, GlideDrawable>() {

                        @Override
                        public boolean onException(Exception e, StorageReference model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, StorageReference model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            button.loaded = true;
                            if(checkButtonsReady()) {
                                showProgressView(false);
                            }
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .override(300, 300)
                    .into(pic);
//                pic.setImageBitmap(mButtons.get(i).cachedBitmap);


            switch (button.state) {
                case True:
                    view.setVisibility(View.VISIBLE);
                    failMark.setVisibility(View.VISIBLE);
                    failMark.setImageResource(R.drawable.tick);
                    break;
                case False:
                    view.setVisibility(View.VISIBLE);
                    failMark.setVisibility(View.VISIBLE);
                    failMark.setImageResource(R.drawable.cross);
                    break;
                case Hide:
                    view.setVisibility(View.INVISIBLE);
                    failMark.setVisibility(View.INVISIBLE);
                    break;
                case Normal:
                default:
                    view.setVisibility(View.VISIBLE);
                    failMark.setVisibility(View.INVISIBLE);
                    break;
            }
            return view;
        }

        void update(int i) {
//            Log.d(TAG, "update grid");
            notifyDataSetChanged();
        }
    }

    private void showProgressView(boolean show) {
        View v = getView();
        if (v != null) {
            View progress_view = v.findViewById(R.id.progress_view);
            if (progress_view != null) {
                progress_view.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }


    private void init () {
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
        rnd = new Random(System.currentTimeMillis());
        mButtons = new ArrayList<>();
        buttonBlocked = true;
    }

    public ChoosePaintGameFragment() {
        // Required empty public constructor
        init();
    }


    private void showButtonBlock(boolean show) {
        View v = getView();
        if (v == null)
            return;
//        if (show) {
//            v.findViewById(R.id.choose_button_grid_view).setVisibility(View.VISIBLE);
//        } else {
//            v.findViewById(R.id.choose_button_grid_view).setVisibility(View.INVISIBLE);
//        }
//        buttonBlocked = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment

        Glide.get(getContext()).setMemoryCategory(MemoryCategory.HIGH);
        GlideBuilder b = new GlideBuilder(getContext());
        b.setMemoryCache(new LruResourceCache(100000000));


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

        fullImage = (ImageView) view.findViewById(R.id.full_image);

        View backgroundView = view.findViewById(R.id.full_image_fade_background);
        background = backgroundView.getBackground();
        background.setAlpha(0);
//        Log.d(TAG,"onCreateView");

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
        fullImage.setVisibility(View.INVISIBLE);

//        int width  = fullImage.getMeasuredWidth();
//        int height  = fullImage.getMeasuredHeight();
//        SizeChangeAnimation anim = new SizeChangeAnimation(fullImage);
//        anim.setHeights(width, 0);
//        anim.setWidths(height, 0);
//        anim.setDuration(100);
//        fullImage.startAnimation(anim);

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
        View layout = getView().findViewById(R.id.choose_pict_root_layout);
        int width  = layout.getMeasuredWidth();
        int height  = layout.getMeasuredHeight();

        fullShowed = true;
        fullImage.setVisibility(View.VISIBLE);
        Glide.with(getContext())
                .using(new FirebaseImageLoader())
                .load(mStorageRef.child(mButtons.get(i).picture.path))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .override(width, height)
                .into(fullImage);

        SizeChangeAnimation anim = new SizeChangeAnimation(fullImage);
        anim.setHeights(10, width);
        anim.setWidths(10, height);
        anim.setDuration(150);
//        fullImage.startAnimation(anim);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(background,
                PropertyValuesHolder.ofInt("alpha", 0, 160));
        animator.setTarget(background);
        animator.setDuration(1);
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
    public void onResume() {
        super.onResume();
        playGame(gameIndex);
        getActivity().setTitle(R.string.game_2_title);
    }

    private void buttonSelected(int ind) {

        if (!checkButtonsReady())
            return;

        buttonBlocked = true;
        int timeout = 800;
        boolean result = false;
        if (games.get(gameIndex).author.id == games.get(gameIndex).picture_variant.get(ind).author) {
            // Right )
            result = true;
//            author_view.setBackgroundResource(R.drawable.choose_button_true_background_shape);
            for(ChooseButton button : mButtons) {
                if(button.author_id == games.get(gameIndex).author.id) {
                    button.state = ButtonState.True;
                }
                else {
                    button.state = ButtonState.Hide;
                }
            }
        } else {
            // Fail (
//            author_view.setBackgroundResource(R.drawable.choose_button_false_background_shape);
            for(ChooseButton button : mButtons) {
                if(button.author_id == games.get(gameIndex).author.id) {
                    button.state = ButtonState.True;
                }
                else {
                    if (mButtons.indexOf(button) == ind) {
                        button.state = ButtonState.False;
                    } else {
                        button.state = ButtonState.Hide;
                    }
                }
            }
            timeout = 1500;
        }
        for(ChooseButton button: mButtons) {
            addToHistory(new GameHistory.GameHistoryItem(button.picture, result));
        }
        userData.updateGameStatistic(getContext(), games.get(gameIndex).picture_variant.get(ind), result, TAG);

        sessionStatistic.addAttempt(result);
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

    private boolean checkButtonsReady() {
        for (ChooseButton b: mButtons) {
            if (!b.loaded) {
                return false;
            }
        }
        return true;
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
        games = createNewGame(gameCount);
        init();
//        Log.d(TAG,"onCreate");
    }

    private void playGame(int gameIndex) {
        if (gameIndex < games.size()) {
//            Log.d(TAG, "play game " + gameIndex);
            ChoosePaintGame game = games.get(gameIndex);
            mButtons.clear();
            for (Picture pic : game.picture_variant) {
                mButtons.add(new ChooseButton(pic, pic.author));
            }
            author_view.setText(game.author.getName());
            author_view.setBackgroundResource(0);
            showProgressView(true);
        } else { // we played all game and now just show last one
        }
        loadAllImages();
    }

    void loadAllImages() {
//        View v = getView();
//        if (v != null) {
//            View p = v.findViewById(R.id.progress_view);
//            if (p != null) {
//                p.setVisibility(View.INVISIBLE);
//            }
//        }
//        View v = getView().findViewById(R.id.progress_view);
//        if (v != null) {
//            v.setVisibility(View.VISIBLE);
//        }
//        for (final ChooseButton b : mButtons) {
//            b.cachedBitmap = null;
//
//            Glide.with(getActivity())
//                    .using(new FirebaseImageLoader())
//                    .load(mStorageRef.child(b.picture.path))
//                    .asBitmap()
//                    .override(100,100)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(b.cachedBitmap)
////                    .into(new SimpleTarget<Bitmap>() {
////                        @Override
////                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
////                            b.cachedBitmap = resource;
////                            mButtonAdapter.update(0);
////                            int i = 0;
////                            for (final ChooseButton b: mButtons) {
////                                if (b.cachedBitmap != null) {
////                                    i++;
////                                    if (i == 4) {
////                                        View v = getView();
////                                        if (v != null) {
////                                            v = v.findViewById(R.id.progress_view);
////                                        }
////                                        if (v != null) {
////                                            v.setVisibility(View.INVISIBLE);
////                                        }
////                                    }
////                                }
////                            }
////                        }
////                    }
//                    );
//        }
        mButtonAdapter.update(0);
    }
    ArrayList<ChoosePaintGame> createNewGame(int count)
    {
//        Log.d(TAG, "create new " + count + "games");
        gameIndex = 0;
        sessionStatistic = new BaseGameStatistic();
        ArrayList<ChoosePaintGame> games = new ArrayList<>();

        ArrayList<Picture> tmp_pic = new ArrayList<>();
        tmp_pic.addAll(dataProvider.getPictures());
        Collections.shuffle(tmp_pic);

        for (int i = 0; i < count; i++) {
            ChoosePaintGame game = new ChoosePaintGame(i);
            if (tmp_pic.size() > 0) {
                game.picture_variant.add(tmp_pic.remove(rnd.nextInt(tmp_pic.size())));
                game.author = dataProvider.getAuthorById(game.picture_variant.get(0).author);


                int pic_count = 3;
                while (pic_count > 0) {
                    Picture pic = dataProvider.getPictures().get(rnd.nextInt(dataProvider.getPictures().size()));
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
