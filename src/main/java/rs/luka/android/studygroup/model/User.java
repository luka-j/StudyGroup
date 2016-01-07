package rs.luka.android.studygroup.model;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Network;
import rs.luka.android.studygroup.network.UserManager;

/**
 * Created by luka on 25.7.15..
 */
public class User {
    public static final String PREFS_NAME = "userdata";
    public static final String PREFS_KEY_TOKEN = "User.token";

    private static User   instance;
    private String name;
    private String   token;
    private long id;
    private SharedPreferences prefs;

    public User(String token, SharedPreferences prefs) {
        this.token = token;
        this.prefs = prefs;
    }

    public User(long id, String name) {
        this.name = name;
        this.id = id;
    }

    public static boolean hasSavedToken(SharedPreferences prefs) {
        return prefs.contains(PREFS_KEY_TOKEN);
    }

    public static void clearToken() {
        instance.prefs.edit().remove(PREFS_KEY_TOKEN).apply();
        instance.token=null;
    }
    public static void instantiateUser(String token, SharedPreferences prefs) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(PREFS_KEY_TOKEN, token);
        prefsEditor.apply();
        instance = new User(token, prefs);
    }

    public static String getToken() {
        if(instance != null)
            return instance.token;
        return null;
    }

    public static User getLoggedInUser() {
        return instance;
    }

    public static boolean isLoggedIn() {
        return instance != null;
    }

    public void refreshToken(String token) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(PREFS_KEY_TOKEN, token);
        prefsEditor.apply();
        this.token = token;
    }

    public String getName() {return name;}

    public Bitmap getImage() {
        return DataManager.getUserImage(id);
    }

    public boolean hasImage() {
        return DataManager.userImageExists(id);
    }
}
