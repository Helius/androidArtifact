package com.ghelius.artifacts.artifacts;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

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

    @Test
    public void testUserData() throws Exception {
        UserData userData = new UserData() {
            @Override
            boolean saveUserData(JSONObject data) {
                return false;
            }

            @Override
            JSONObject loadUserData() {
                return new JSONObject();
            }
        };

        assertEquals(userData.getLevel(), 0);
        userData.setLevel(2);
        assertEquals(userData.getLevel(), 2);

        assertEquals(0, userData.getGameStatistic(2).totalAttempt);
        assertEquals(0, userData.getGameStatistic(2).successfullAttempt);

        userData.updateGameStatistic(0, new BaseGameStatistic(15, 6));
        assertEquals(15, userData.getGameStatistic(0).totalAttempt);
        assertEquals(6, userData.getGameStatistic(0).successfullAttempt);

        userData.updateGameStatistic(8, new BaseGameStatistic(150, 60));
        assertEquals(150, userData.getGameStatistic(8).totalAttempt);
        assertEquals(60, userData.getGameStatistic(8).successfullAttempt);

        userData.updateGameStatistic(0, new BaseGameStatistic(3, 3));
        assertEquals(15 + 3, userData.getGameStatistic(0).totalAttempt);
        assertEquals(6 + 3, userData.getGameStatistic(0).successfullAttempt);
    }

}
