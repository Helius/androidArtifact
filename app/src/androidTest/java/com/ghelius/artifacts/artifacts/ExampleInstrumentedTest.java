package com.ghelius.artifacts.artifacts;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.ref.WeakReference;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.ghelius.artifacts.artifacts", appContext.getPackageName());
    }

    boolean userDataSaved = false;
    @Test
    public void testUserData() throws Exception {
        UserData userData = new UserData() {
            @Override
            boolean saveUserData(JSONObject data) {
                userDataSaved = true;
                return false;
            }

            @Override
            JSONObject loadUserData() {
                return new JSONObject();
            }
        };

        assertEquals(userData.getLevel(), 0);
        assertEquals(false, userDataSaved);
        userData.setLevel(2);
        assertEquals(userData.getLevel(), 2);
        assertEquals(true, userDataSaved);

        assertEquals(0, userData.getGameStatistic(2).totalAttempt);
        assertEquals(0, userData.getGameStatistic(2).successfullAttempt);

        userData.updateGameStatistic(0, new BaseGameStatistic(15, 6));
        assertEquals(15, userData.getGameStatistic(0).totalAttempt);
        assertEquals(6, userData.getGameStatistic(0).successfullAttempt);
        assertEquals(true, userDataSaved);

        userDataSaved = false;
        userData.updateGameStatistic(8, new BaseGameStatistic(150, 60));
        assertEquals(150, userData.getGameStatistic(8).totalAttempt);
        assertEquals(60, userData.getGameStatistic(8).successfullAttempt);
        assertEquals(true, userDataSaved);

        userDataSaved = false;
        userData.updateGameStatistic(0, new BaseGameStatistic(3, 3));
        assertEquals(15 + 3, userData.getGameStatistic(0).totalAttempt);
        assertEquals(6 + 3, userData.getGameStatistic(0).successfullAttempt);
        assertEquals(true, userDataSaved);
    }

}
