package com.ghelius.artifacts.artifacts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;

import java.lang.ref.WeakReference;

public class SplashActivity extends AppCompatActivity {
    private boolean closed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("splash", "onCreate " + this.toString());
        Handler h = new Handler();
        final WeakReference<Context> ctx = new WeakReference<Context>(getApplicationContext());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("splash", "timeout, alive: " + (ctx.get() != null));
                Context context = ctx.get();
                if (context != null && !closed) {
                    goToApp(context);
                }
            }
        }, 2000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!closed) {
            closed = true;
            goToApp(getApplicationContext());
        }
        return super.onTouchEvent(event);
    }

    void goToApp(Context context) {
        Log.d("splash", "go to app with " + this.toString());
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("splash", "onDestroy " + this.toString());
        closed = true;
    }
}
