package rs.luka.android.studygroup.model;

import android.graphics.Bitmap;

import java.util.UUID;

import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.UserManager;

/**
 * Created by luka on 25.7.15..
 */
public class User {
    private static User   instance;
    private final  String name;
    private        UUID   token;
    private long id;

    public User(long id, String name) {
        this.name = name;
        this.id = id;
    }
    private User(String username, String password) {
        token = UserManager.login(username, password);
        name = UserManager.requestName(token);
    }

    public static User login(String username, String password) {
        instance = new User(username, password);
        return instance;
    }

    public static User getLoggedInUser() {
        return instance;
    }

    public static boolean isLoggedIn() {
        return instance != null;
    }

    public void refreshToken(UUID token) {
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
