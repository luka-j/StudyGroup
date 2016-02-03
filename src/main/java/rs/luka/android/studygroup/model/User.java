package rs.luka.android.studygroup.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.DataManager;

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
    private int permission;
    private static SharedPreferences prefs; //todo fix mess

    public User(String token, SharedPreferences prefs) {
        this.token = token;
        User.prefs = prefs; //todo really?
    }

    public User(long id, String name, int permission) {
        this.name = name;
        this.id = id;
        this.permission = permission;
    }

    public static boolean hasSavedToken(SharedPreferences prefs) {
        return prefs.contains(PREFS_KEY_TOKEN);
    }

    public static void recoverUser() {
        instance = new User(prefs.getString(PREFS_KEY_TOKEN, null), prefs); //todo I've said it once, and I'll say it again: fix mess
    }

    public static void clearToken() {
        if(prefs != null)
            prefs.edit().remove(PREFS_KEY_TOKEN).apply();
        instance.token=null;
    }
    public static void instantiateUser(String token, SharedPreferences prefs) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(PREFS_KEY_TOKEN, token);
        prefsEditor.apply();
        instance = new User(token, prefs);
    }
    public static void setOfflineUser(SharedPreferences prefs) {
        instance = new User(prefs.getString(PREFS_KEY_TOKEN, "0"), prefs);
    }

    public static String getToken() {
        if(instance != null)
            return instance.token;
        if(prefs != null && hasSavedToken(prefs)) {
            instantiateUser(prefs.getString(PREFS_KEY_TOKEN, null), prefs);
            return instance.token;
        }
        return null;
    }

    public long getId() {
        return id;
    }
    public static void setMyId(long id) {
        instance.id = id;
    }

    public String getRoleDescription(Context c) {
        if(permission >= Group.PERM_OWNER)
            return c.getString(R.string.role_owner);
        if(permission >= Group.PERM_MODIFY)
            return c.getString(R.string.role_mod);
        if(permission >= Group.PERM_WRITE)
            return c.getString(R.string.role_member);
        if(permission >= Group.PERM_REQUEST_WRITE)
            return c.getString(R.string.role_request);
        return c.getString(R.string.role_stranger);
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

    public int getPermission() {
        return permission;
    }
    public void setPermission(int permission) {this.permission = permission;}
}
