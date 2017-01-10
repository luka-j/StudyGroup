package rs.luka.android.studygroup.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.ImageView;

import rs.luka.android.studygroup.Notekeeper;
import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.backgroundtasks.DataManager;
import rs.luka.android.studygroup.io.backgroundtasks.UserTasks;
import rs.luka.android.studygroup.io.database.Database;

/**
 * Created by luka on 25.7.15..
 * 9.1.'17.: definitely needs rewrite
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
    private long id;
    private int permission;
    private boolean hasImage;
    private SharedPreferences prefs;

    private User() {
        prefs = Notekeeper.getAppContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE); //better than the last version
        loadDetailsFromPrefs();
    }
    private User(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public User(long id, String name, int permission, boolean hasImage) {
        this.name = name;
        this.id = id;
        this.permission = permission;
        this.hasImage = hasImage;
    }

    public String getToken() {
        return prefs.getString(PREFS_KEY_TOKEN, "");
    }

    public static boolean hasSavedToken() {
        if(instance == null) instance = new User();
        return instance.prefs.contains(PREFS_KEY_TOKEN);
    }

    public static void clearToken() {
        if(instance == null) instance = new User();
        instance.prefs.edit().remove(PREFS_KEY_TOKEN).apply();
    }
    public static void instantiateUser(String token, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(PREFS_KEY_TOKEN, token);
        prefsEditor.apply();
        instance = new User(prefs);
    }
    public static void setOfflineUser(SharedPreferences prefs) {
        instance = new User(prefs);
    }

    public static String getInstanceToken() {
        if(instance == null) instance = new User();
        return instance.getToken();
    }

    public static void setMyDetails(long id, String name, String email, boolean hasImage) {
        if(id > 0) instance.id = id;
        if(name != null) instance.name = name;
        if(email != null) instance.email = email;
        instance.hasImage = hasImage;
        SharedPreferences.Editor editor = instance.prefs.edit();
        editor.putLong(PREFS_KEY_ID, id);
        editor.putString(PREFS_KEY_NAME, name);
        editor.putString(PREFS_KEY_EMAIL, email);
        editor.putBoolean(PREFS_KEY_HASIMAGE, hasImage);
        editor.apply();
    }

    public void loadDetailsFromPrefs() {
        id = prefs.getLong(PREFS_KEY_ID, 0);
        name = prefs.getString(PREFS_KEY_NAME, "");
        email = prefs.getString(PREFS_KEY_EMAIL, "");
        hasImage = prefs.getBoolean(PREFS_KEY_HASIMAGE, false);
    }
    public static void loadMyDetailsFromPrefs() {
        if(instance == null) instance = new User();
        instance.loadDetailsFromPrefs();
    }

    public static String getMyEmail() {
        return instance.email;
    }

    public long getId() {
        return id;
    }

    public String getRoleDescription(Context c) {
        if(permission >= Group.PERM_CREATOR)
            return c.getString(R.string.role_creator);
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
        if(instance == null) instance = new User();
        return instance;
    }

    public static boolean isLoggedIn() {
        return instance != null;
    }

    public void refreshToken(String token) {
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putString(PREFS_KEY_TOKEN, token);
        prefsEditor.apply();
    }

    public void logOut(Context c) {
        prefs.edit().clear().apply();
        DataManager.clearFetchHistory(c);
        prefs = null;
        instance = null;
        SQLiteOpenHelper dbHelper = Database.getInstance(c);
        dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 0, 1);
    }

    public String getName() {return name;}

    public void getImage(Context c, int scaleTo, NetworkExceptionHandler handler, ImageView insertInto) {
        UserTasks.getUserImage(c, id, scaleTo, handler, insertInto);
    }

    public boolean hasImage() {
        return hasImage;
    }

    public int getPermission() {
        return permission;
    }
    public void setPermission(int permission) {this.permission = permission;}
}
