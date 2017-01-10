package rs.luka.android.studygroup.io.network;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.exceptions.NotLoggedInException;
import rs.luka.android.studygroup.model.User;

/**
 * Created by luka on 25.7.15..
 */
public class UserManager {

    private static final String V = Network.API_VERSION;
    protected static final String USERS = "users/";
    private static final String TAG = "net.UserManager";
    private static final String KEY_REGISTER_EMAIL = "email";
    private static final String KEY_REGISTER_USERNAME = "username";
    private static final String KEY_REGISTER_PASSWORD = "password";
    private static final String KEY_LOGIN_EMAIL = "email";
    private static final String KEY_LOGIN_PASSWORD = "pass";

    public static void register(int requestId, String email, String username, String password,
                                Network.NetworkCallbacks<String> callback) {
        Map<String, String> params = new HashMap<>(3);
        params.put(KEY_REGISTER_USERNAME, username);
        params.put(KEY_REGISTER_PASSWORD, password);
        params.put(KEY_REGISTER_EMAIL, email);
        try {
            URL registerUrl = new URL(Network.getDomain(), V + USERS + "register");
            NetworkRequests.postDataAsync(requestId, registerUrl, params, callback);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex); //should NEVER happen
        }
    }

    public static void login(int requestId, String email, String password, Network.NetworkCallbacks<String> callback) {
        Map<String, String> params = new HashMap<>(2);
        params.put(KEY_LOGIN_EMAIL, email);
        params.put(KEY_LOGIN_PASSWORD, password);
        try {
            URL loginUrl = new URL(Network.getDomain(), V + USERS + "login");
            NetworkRequests.postDataAsync(requestId, loginUrl, params, callback);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex); //should NEVER happen
        }
    }

    public static URL getRefreshTokenUrl(String token) {
        try {
            return new URL(Network.getDomain(), V + USERS + token + "/refresh");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void refreshToken(int requestId, String token, Network.NetworkCallbacks<String> callback) {
        URL refreshUrl = getRefreshTokenUrl(token);
        NetworkRequests.postDataAsync(requestId, refreshUrl, NetworkRequests.emptyMap, callback);
    }

    /**
     * Do NOT call on UI thread. Blocks.
     * @param response
     * @throws NotLoggedInException
     */
    public static void handleTokenError(Network.Response response, NetworkExceptionHandler exceptionHandler)
            throws NotLoggedInException {
        if ("Expired".equals(response.errorMessage)) {
            try {
                URL refreshUrl = new URL(Network.getDomain(), V + USERS + User.getInstanceToken() + "/refresh");
                User.getLoggedInUser()
                    .refreshToken(NetworkRequests.requestPostData(refreshUrl,
                                                                  NetworkRequests.emptyMap).responseData);
                Log.i(TAG, "token refreshed");
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex); //should NOT happen
            } catch (IOException ex) {
                exceptionHandler.handleIOException(ex);
            }
        } else
            throw new NotLoggedInException(UserManager.class, "handleTokenError(Response): server token error");
    }

    public static void getMyDetails(int requestId, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), V + USERS + "me");
            NetworkRequests.getDataAsync(requestId, url, callbacks);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void checkPassword(int requestId, String pass, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), V + USERS + "checkpw?pwd=" + pass);
            NetworkRequests.getDataAsync(requestId, url, callbacks);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void changePassword(int requestId, String old, String newPass, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), V + USERS + "changepw");
            Map<String, String> params = new HashMap<>(2);
            params.put("old", old);
            params.put("pwd", newPass);
            NetworkRequests.putDataAsync(requestId, url, params, callbacks);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean getImage(long userId, int size, File loadInto, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), V + USERS + userId + "/image?size=" + size);
            Network.Response<File> response = NetworkRequests.requestGetFile(url, loadInto);

            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;
            Network.Response<File> handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_OK;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateMyImage(File image, NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL url = new URL(Network.getDomain(), V + USERS + "myImage");
            Network.Response<File> response = NetworkRequests.requestPutFile(url, image);

            if(response.responseCode == Network.Response.RESPONSE_CREATED)
                return true;
            Network.Response<File> handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateMyProfile(String username, String email, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), V + USERS + "myProfile");
            Map<String, String> params   = new HashMap<>(2);
            params.put("username", username);
            params.put("email", email);

            Network.Response    response = NetworkRequests.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
