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
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class ChooseAuthorGameFragment extends Fragment {

    private static final String TAG = "ChooseAuthor";

    private StorageReference mStorageRef;
    private ArrayList<Picture> pictures;
    private ArrayList<Author> authors;
    private ImageView mImageView;
    private int trueButtonIndex;
    private Random rnd = new Random(System.currentTimeMillis());
    private ArrayList<TextView> buttons = new ArrayList<>();
    FirebaseDatabase mDatabase;
    private boolean picturesReady = false;
    private boolean authorReady = false;
    private boolean buttonBlocked = false;

    public ChooseAuthorGameFragment() {
        // Required empty public constructor
    }

    public void loadPicture(Picture picture) {
        mStorageRef.child(picture.path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG,"got uri");
                Activity a = getActivity();
                if (a != null) {
                    Picasso.with(a.getApplicationContext()).load(uri.toString()).into(mImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            showButtonBlock(true);
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(getActivity(), "Sorry, can't load image...", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    private void showButtonBlock(boolean b) {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setVisibility(b ? View.VISIBLE : View.INVISIBLE);
        }
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
                    choose(index);
                }
            });

        }

        return view;
    }

    private void choose(int ind) {

        if (buttonBlocked)
            return;

        // check user choosing
        if (ind == trueButtonIndex) {
            Toast.makeText(getActivity(), "Yep!", Toast.LENGTH_SHORT).show();
            createNewGame();
        } else {
            for(int i = 0; i < buttons.size(); i++) {
                if (i != trueButtonIndex) {
                    buttons.get(i).setText("");
                }
            }
            buttonBlocked = true;
            final Handler h = new Handler ();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttonBlocked = false;
                    createNewGame();
                }
            }, 2000);
        }
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
        if (picturesReady && authorReady) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createNewGame();
                    }
                });
            } else {
                // WTF?
            }
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

    void createNewGame()
    {
        showButtonBlock(false);
        // select and load pic
        Picture pic = pictures.get(rnd.nextInt(pictures.size()-1));
        loadPicture(pic);


        // select button
        trueButtonIndex = rnd.nextInt(buttons.size());
        buttons.get(trueButtonIndex).setText(getAuthorById(pic.author).name_ru);

        ArrayList<Author> tmp_authors = new ArrayList<>();
        for (Author a: authors) {
            if (a.id != pic.author)
                tmp_authors.add(a);
        }
        long seed = System.nanoTime();
        Collections.shuffle(tmp_authors, new Random(seed));

        // arrange other
        for(int i = 0; i < buttons.size(); i++) {
            if (trueButtonIndex != i) {
                Author a = tmp_authors.get(i % tmp_authors.size());
                buttons.get(i).setText(a.name_ru);
            }
        }
    }

    private Author getAuthorById (Long id) {
        for (Author a : authors) {
            if (a.id == id) {
                return a;
            }
        }
        return null;
    }

}
