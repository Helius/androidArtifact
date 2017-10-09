package com.ghelius.artifacts.artifacts;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public abstract class NewAuthorsDetector {

    private WeakReference<Context> context_weak;

    abstract void result(ArrayList<Author> a);

    private static class Params {
        JSONArray authors_json;
        ArrayList<Author> authors_arr;
    }


    public void detectNewAuthors(Context context, ArrayList<Author> loaded_authors) throws JSONException {
        this.context_weak = new WeakReference<>(context);
        Params p = new Params();
        p.authors_json = new JSONArray(PreferenceManager.getDefaultSharedPreferences(context).getString("saved_authors", "[]"));
        p.authors_arr = loaded_authors;
        //Log.d("helius:", "saved " + p.authors_json.length() + ", " + p.authors_json.toString());
        //Log.d("helius:", "checked " + p.authors_arr.size() + ", " + p.authors_arr.toString());

        task.execute(new Params[] {p});
    }
    private AsyncTask task = new AsyncTask<Params, Void, Params>() {
        @Override
        protected Params doInBackground(Params... paramses) {
            ArrayList<Author> res = new ArrayList<>();
            Params input = paramses[0];
            Params output = new Params();
            JSONArray out_arr = new JSONArray();

            for (final Author a : input.authors_arr) {
                boolean found = false;
                for (int i = 0; i < input.authors_json.length(); ++i) {
                    try {
                        if(input.authors_json.getInt(i) == a.id) {
                            found = true;
                            break;
                        }
                    } catch (JSONException e) {
                        break;
                    }
                }
                // if we have empty saved list, just don't add all authors as new
                if (!found && input.authors_json.length() > 0) {
                    res.add(a);
                }
                out_arr.put(a.id);
            }


            output.authors_arr = res;
            output.authors_json = out_arr;
            return output;
        }

        @Override
        protected void onPostExecute(Params params) {
            super.onPostExecute(params);
            Context context = context_weak.get();
            if (context != null) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("saved_authors", params.authors_json.toString()).apply();
            }
            // show only if > 2 new author
            if (params.authors_arr.size() > 1) {
                result(params.authors_arr);
            }
        }
    };
}
