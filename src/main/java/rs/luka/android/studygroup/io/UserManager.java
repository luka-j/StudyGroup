package rs.luka.android.studygroup.io;

import java.util.UUID;

/**
 * Created by luka on 25.7.15..
 */
public class UserManager {
    public static UUID login(String username, String password) {

        return UUID.randomUUID();
    }

    public static String requestName(UUID token) {
        return "random";
    }
}
