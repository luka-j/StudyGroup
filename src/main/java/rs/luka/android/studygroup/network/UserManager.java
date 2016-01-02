package rs.luka.android.studygroup.network;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import rs.luka.android.studygroup.io.Network;

/**
 * Created by luka on 25.7.15..
 */
public class UserManager {
    private static final String KEY_REGISTER_EMAIL = "email";
    private static final String KEY_REGISTER_USERNAME = "username";
    private static final String KEY_REGISTER_PASSWORD = "password";

    private static final String KEY_LOGIN_EMAIL = "email";
    private static final String KEY_LOGIN_PASSWORD = "pass";

    protected static final String USERS = "users/";
    protected static final String REGISTER = Network.DOMAIN + USERS + "register";
    protected static final String LOGIN = Network.DOMAIN + USERS + "login";

    public static void register(int requestId, String email, String username, String password, Network.NetworkCallback callback) {
        Map<String, String> params = new HashMap<>(3);
        params.put(KEY_REGISTER_USERNAME, username);
        params.put(KEY_REGISTER_PASSWORD, password);
        params.put(KEY_REGISTER_EMAIL, email);
        try {
            URL registerUrl = new URL(REGISTER);
            Network.postDataAsync(requestId, registerUrl, params, callback);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex); //should NEVER happen
        }
    }

    public static void login(int requestId, String email, String password, Network.NetworkCallback callback) {
        Map<String, String> params = new HashMap<>(2);
        params.put(KEY_LOGIN_EMAIL, email);
        params.put(KEY_LOGIN_PASSWORD, password);
        try {
            URL loginUrl = new URL(LOGIN);
            Network.postDataAsync(requestId, loginUrl, params, callback);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex); //should NEVER happen
        }
    }

    public static void refreshToken(int requestId, String token, Network.NetworkCallback callback) {
        try {
            URL refreshUrl = new URL(Network.DOMAIN + USERS + token + "/refresh");
            Network.postDataAsync(requestId, refreshUrl, Network.emptyMap, callback);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex); //should NOT happen
        }
    }
}
