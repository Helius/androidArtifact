package com.ghelius.artifacts.artifacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


class TextButtonAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private ArrayList<TextButton> mButtons;

    TextButtonAdapter(Context context) {
        mButtons = new ArrayList<>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public TextButton getButton(int i) {
        return mButtons.get(i);
    }

    public void addNewButton(TextButton button) {
        mButtons.add(button);
        notifyDataSetChanged();
    }

    public ArrayList<TextButton> getButtons() {
        return mButtons;
    }

    public void clearButton() {
        mButtons.clear();
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

    void update() {
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(R.layout.choose_button_item, viewGroup, false);
        }
        TextView text = (TextView) view.findViewById(R.id.text);
        TextButton button = mButtons.get(i);
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
}
