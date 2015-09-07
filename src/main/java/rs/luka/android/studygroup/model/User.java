package rs.luka.android.studygroup.model;

import java.util.UUID;

import rs.luka.android.studygroup.io.UserManager;

/**
 * Created by luka on 25.7.15..
 */
public class User {
    private static User   instance;
    private final  String name;
    private        UUID   token;

    private User(String username, String password) {
        token = UserManager.login(username, password);
        name = UserManager.requestName(token);
    }

    public static User login(String username, String password) {
        instance = new User(username, password);
        return instance;
    }

    public static User getInstance() {
        return instance;
    }

    public static boolean isLoggedIn() {
        return instance != null;
    }

    public void refreshToken(UUID token) {
        this.token = token;
    }

    public String getName() {return name;}

}
