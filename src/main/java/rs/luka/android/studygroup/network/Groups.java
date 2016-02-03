package rs.luka.android.studygroup.network;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 2.1.16..
 */
public class Groups  {
    public static final String GROUPS = "groups/";
    private static final String TAG = "studygroup.net.Groups";
    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_PLACE = "place";
    private static final String JSON_KEY_HASIMAGE = "hasImage";
    private static final String JSON_KEY_YEARS = "courseYears";
    private static final String JSON_KEY_PERMISSION = "permission";

    /**
     * Retrieves groups from server and overwrites those in the database
     * @param c Context used to access database
     * @param exceptionHandler object responsible for handling various networking unexpected behaviors
     * @throws IOException
     */
    public static void getGroups(Context c, NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL              url      = new URL(Network.getDomain(), GROUPS);
            Network.Response<String> response = NetworkRequests.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray array = new JSONArray(response.responseData);
                int len = array.length();
                Group[] groups = new Group[len];
                for(int i=0; i<len; i++) {
                    JSONObject jsonGroup = array.getJSONObject(i).getJSONObject("group");
                    groups[i] = new Group(new ID(jsonGroup.getLong(JSON_KEY_ID)),
                                          jsonGroup.getString(JSON_KEY_NAME),
                                          jsonGroup.getString(JSON_KEY_PLACE),
                                          jsonGroup.getBoolean(JSON_KEY_HASIMAGE),
                                          Utils.stringToList(jsonGroup.getString(JSON_KEY_YEARS)),
                                          array.getJSONObject(i).getInt(JSON_KEY_PERMISSION));
                }
                Database.getInstance(c).clearGroups();
                Database.getInstance(c).insertGroups(groups);
            } else {
                Log.w(TAG, "Something's wrong; server returned code " + response.responseCode);

                Network.Response handled = response.handleException(exceptionHandler);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            exceptionHandler.handleJsonException();
        }
    }


    public static Long createGroup(String name, String place, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), GROUPS);
            Map<String, String> params   = new HashMap<>(2);
            params.put(JSON_KEY_NAME, name);
            params.put(JSON_KEY_PLACE, place);

            Network.Response<String>    response = NetworkRequests.requestPostData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_CREATED)
                return Long.parseLong(response.responseData);


            Network.Response handled = response.handleException(exceptionHandler);
            if(handled.responseCode == Network.Response.RESPONSE_CREATED)
                return Long.parseLong(response.responseData);
            return null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateGroup(long id, String name, String place, NetworkExceptionHandler exceptionHandler)
        throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), GROUPS + id);
            Map<String, String> params   = new HashMap<>(2);
            params.put(JSON_KEY_NAME, name);
            params.put(JSON_KEY_PLACE, place);

            Network.Response    response = NetworkRequests.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getUsers(int requestId, long id, Network.NetworkCallbacks<String> callbacks) {
        try {
            NetworkRequests.getDataAsync(requestId, new URL(Network.getDomain(), GROUPS + id + "/members"), callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void requestJoin(int requestId, long id, Network.NetworkCallbacks<String> callbacks) {
        try {
            NetworkRequests.postDataAsync(requestId, new URL(Network.getDomain(), "group/" + id + "/requestWrite"),
                                          NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void grantOwner(int requestId, long groupId, long userId, Network.NetworkCallbacks<String> callbacks) {
        try {
            NetworkRequests.putDataAsync(requestId, new URL(Network.getDomain(), GROUPS + groupId + "/grantOwner/" + userId),
                                         NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void grantMod(int requestId, long groupId, long userId, Network.NetworkCallbacks<String> callbacks) {
        try {
            NetworkRequests.putDataAsync(requestId, new URL(Network.getDomain(), GROUPS + groupId + "/grantModify/" + userId),
                                         NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void grantMember(int requestId, long groupId, long userId, Network.NetworkCallbacks<String> callbacks) {
        try {
            NetworkRequests.putDataAsync(requestId, new URL(Network.getDomain(), GROUPS + groupId + "/grantWrite/" + userId),
                                         NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void revokeMember(int requestId, long groupId, long userId, Network.NetworkCallbacks<String> callbacks) {
        try {
            NetworkRequests.putDataAsync(requestId, new URL(Network.getDomain(), GROUPS + groupId + "/revokeWrite/" + userId),
                                         NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static boolean loadImage(long id, int size, File loadInto, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), GROUPS + id + "/image?size=" + size);
            Network.Response<File> response = NetworkRequests.requestGetFile(url, loadInto);

            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;
            Network.Response<File> handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_OK;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateImage(long id, File image, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), GROUPS + id + "/image");
            Network.Response<File> response = NetworkRequests.requestPutFile(url, image);

            if(response.responseCode == Network.Response.RESPONSE_CREATED)
                return true;
            Network.Response<File> handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
