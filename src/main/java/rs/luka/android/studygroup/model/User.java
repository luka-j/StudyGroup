package rs.luka.android.studygroup.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.DataManager;

/**
 * Created by luka on 25.7.15..
 */
public class User {
    public static final String PREFS_NAME       = "userdata";
    public static final String PREFS_KEY_TOKEN  = "token";
    public static final String PREFS_KEY_ID     = "id";
    public static final String PREFS_KEY_NAME   = "name";
    public static final String PREFS_KEY_EMAIL  = "email";
    private static final String PREFS_KEY_HASIMAGE = "hasImage";

    private static User   instance;
    private String email;
    private String name;
    private String token;
    private long id;
    private int permission;
    private boolean hasImage;
    private static SharedPreferences prefs; //todo fix mess

    public User(String token, SharedPreferences prefs) {
        this.token = token;
        User.prefs = prefs; //todo really?
    }

    public User(long id, String name, int permission, boolean hasImage) {
        this.name = name;
        this.id = id;
        this.permission = permission;
        this.hasImage = hasImage;
    }

    public static boolean hasSavedToken(SharedPreferences prefs) {
        User.prefs = prefs;
        return prefs.contains(PREFS_KEY_TOKEN);
    }

    public static boolean recoverUser() {
        if(prefs != null) {
            instance = new User(prefs.getString(PREFS_KEY_TOKEN, null), prefs);
            //todo I've said it once, and I'll say it again: fix mess
            loadMyDetailsFromPrefs();
            return true;
        }
        return false;
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

    public static void setMyDetails(long id, String name, String email, boolean hasImage) {
        if(id > 0) instance.id = id;
        if(name != null) instance.name = name;
        if(email != null) instance.email = email;
        instance.hasImage = hasImage;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PREFS_KEY_ID, id);
        editor.putString(PREFS_KEY_NAME, name);
        editor.putString(PREFS_KEY_EMAIL, email);
        editor.putBoolean(PREFS_KEY_HASIMAGE, hasImage);
        editor.apply();
    }

    public static void loadMyDetailsFromPrefs() {
        instance.id = prefs.getLong(PREFS_KEY_ID, 0);
        instance.name = prefs.getString(PREFS_KEY_NAME, "");
        instance.email = prefs.getString(PREFS_KEY_EMAIL, "");
        instance.hasImage = prefs.getBoolean(PREFS_KEY_HASIMAGE, false);
    }

    public static String getMyEmail() {
        return instance.email;
    }

    public long getId() {
        return id;
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
        if(instance == null)
            if(!recoverUser())
                return null;
        return instance;
    }

    public static void injectPrefs(Context ctx) { //todo fix tangled mess
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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

    public void getImage(Context c, int scaleTo, NetworkExceptionHandler handler, ImageView insertInto) {
        DataManager.getUserImage(c, id, scaleTo, handler, insertInto);
    }

    public boolean hasImage() {
        return hasImage;
    }

    public int getPermission() {
        return permission;
    }
    public void setPermission(int permission) {this.permission = permission;}
}
