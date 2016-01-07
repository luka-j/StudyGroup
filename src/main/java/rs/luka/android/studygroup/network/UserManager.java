package rs.luka.android.studygroup.network;

import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import rs.luka.android.studygroup.exceptions.NotLoggedInException;
import rs.luka.android.studygroup.io.Network;
import rs.luka.android.studygroup.model.User;

/**
 * Created by luka on 25.7.15..
 */
public class UserManager {
    private static final String TAG = "net.UserManager";

    private static final String KEY_REGISTER_EMAIL = "email";
    private static final String KEY_REGISTER_USERNAME = "username";
    private static final String KEY_REGISTER_PASSWORD = "password";

    private static final String KEY_LOGIN_EMAIL = "email";
    private static final String KEY_LOGIN_PASSWORD = "pass";

    protected static final String USERS = "users/";

    public static void register(int requestId, String email, String username, String password, Network.NetworkCallbacks callback) {
        Map<String, String> params = new HashMap<>(3);
        params.put(KEY_REGISTER_USERNAME, username);
        params.put(KEY_REGISTER_PASSWORD, password);
        params.put(KEY_REGISTER_EMAIL, email);
        try {
            URL registerUrl = new URL(Network.getDomain(), USERS + "register");
            Network.postDataAsync(requestId, registerUrl, params, callback);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex); //should NEVER happen
        }
    }

    public static void login(int requestId, String email, String password, Network.NetworkCallbacks callback) {
        Map<String, String> params = new HashMap<>(2);
        params.put(KEY_LOGIN_EMAIL, email);
        params.put(KEY_LOGIN_PASSWORD, password);
        try {
            URL loginUrl = new URL(Network.getDomain(), USERS + "login");
            Network.postDataAsync(requestId, loginUrl, params, callback);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex); //should NEVER happen
        }
    }

    public static void refreshToken(int requestId, String token, Network.NetworkCallbacks callback) {
        try {
            URL refreshUrl = new URL(Network.getDomain(), USERS + token + "/refresh");
            Network.postDataAsync(requestId, refreshUrl, Network.emptyMap, callback);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex); //should NOT happen
        }
    }

    /**
     * Do NOT call on UI thread. Blocks.
     * @param response
     * @throws NotLoggedInException
     */
    public static void handleTokenError(Network.Response response)
            throws NotLoggedInException {
        if(response.responseMessage.equals("Expired")) {
            try {
                URL refreshUrl = new URL(Network.getDomain(), USERS + User.getToken() + "/refresh");
                User.getLoggedInUser().refreshToken(Network.requestPostData(refreshUrl, Network.emptyMap).responseMessage);
                Log.i(TAG, "token refreshed");
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex); //should NOT happen
            } catch (IOException ex) {
                ex.printStackTrace(); //todo
            }
        } else
            throw new NotLoggedInException(UserManager.class, "handleTokenError(Response): server token error");
    }
}
