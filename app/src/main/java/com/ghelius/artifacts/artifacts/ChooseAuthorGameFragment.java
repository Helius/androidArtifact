package com.ghelius.artifacts.artifacts;


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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class ChooseAuthorGameFragment extends Fragment {

    private static final String TAG = "ChooseAuthor";

    private StorageReference mStorageRef;
    private ArrayList<Picture> pictures;
    private ArrayList<Author> authors;
    private ImageView mImageView;
    private Random rnd = new Random(System.currentTimeMillis());
    private ArrayList<TextView> buttons = new ArrayList<>();
    FirebaseDatabase mDatabase;
    private boolean picturesReady = false;
    private boolean authorReady = false;
    private boolean buttonBlocked = false;

    private ArrayList<ChooseAuthorGame> games;
    private int gameIndex = 0;

    public ChooseAuthorGameFragment() {
        // Required empty public constructor
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

    private void buttonSelected(int ind) {

        if (buttonBlocked)
            return;
        buttonBlocked = true;

        if (games.get(gameIndex).picture.author == games.get(gameIndex).authors_variant.get(ind).id) {
            buttons.get(ind).setTextColor(0xFF09AD1F);
        } else {
            buttons.get(ind).setTextColor(ContextCompat.getColor(getActivity(), android.R.color.holo_red_dark));

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
                    Toast.makeText(getActivity().getApplicationContext(), "You play 10 times!", Toast.LENGTH_SHORT).show();
                }
            }
        }, 2000);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();
        loadPictData();
        loadAuthorData();
    }

    private void loadPictData () {
        DatabaseReference dbRef = mDatabase.getReference("content").child("pictures");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                GenericTypeIndicator<ArrayList<Picture>> t = new GenericTypeIndicator<ArrayList<Picture>>(){};
                pictures = dataSnapshot.getValue(t);
                picturesReady = true;
                checkAllDataReady();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    private void checkAllDataReady() {
        Log.d(TAG, "checkAllDataReady" + (picturesReady && authorReady));
        if (picturesReady && authorReady) {
            if (getActivity() != null) {
                games = createNewGame(10);
                playGame(gameIndex);
            } else {
                // WTF?
            }
        }
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

    private void loadAuthorData () {
        DatabaseReference dbRef1 = mDatabase.getReference("content").child("authors");

        dbRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                GenericTypeIndicator<ArrayList<Author>> t = new GenericTypeIndicator<ArrayList<Author>>(){};
                authors = dataSnapshot.getValue(t);
                authorReady = true;
                checkAllDataReady();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    ArrayList<ChooseAuthorGame> createNewGame(int count)
    {
        Log.d(TAG, "create new " + count + "games");
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
            mStorageRef.child(picture.path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(getActivity().getApplicationContext()).setIndicatorsEnabled(true);
                    Picasso.with(getActivity().getApplicationContext())
                            .load(uri.toString())
                            .resize(mImageView.getWidth(), mImageView.getHeight())
                            .centerCrop()
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
