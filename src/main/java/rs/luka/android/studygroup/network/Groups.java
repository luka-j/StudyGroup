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
import rs.luka.android.studygroup.io.Network;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 2.1.16..
 */
public class Groups  {
    private static final String TAG = "studygroup.net.Groups";

    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_PLACE = "place";
    private static final String JSON_KEY_HASIMAGE = "hasImage";
    private static final String JSON_KEY_PERMISSION = "permission";
    public static final String GROUPS = "groups/";

    /**
     * Retrieves groups from server and overwrites those in the database
     * @param c Context used to access database
     * @param exceptionHandler object responsible for handling various networking unexpected behaviors
     * @throws IOException
     */
    public static void getGroups(Context c, NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL              url      = new URL(Network.getDomain(), GROUPS);
            Network.Response response = Network.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray array = new JSONArray(response.responseMessage);
                int len = array.length();
                Group[] groups = new Group[len];
                for(int i=0; i<len; i++) {
                    JSONObject jsonGroup = array.getJSONObject(i).getJSONObject("group");
                    groups[i] = new Group(new ID(jsonGroup.getLong(JSON_KEY_ID)),
                                          jsonGroup.getString(JSON_KEY_NAME),
                                          jsonGroup.getString(JSON_KEY_PLACE),
                                          jsonGroup.getBoolean(JSON_KEY_HASIMAGE),
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
            Map<String, String> params   = new HashMap<>(3);
            params.put(JSON_KEY_NAME, name);
            params.put(JSON_KEY_PLACE, place);

            Network.Response    response = Network.requestPostData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_CREATED)
                return Long.parseLong(response.responseMessage);


            Network.Response handled = response.handleException(exceptionHandler);
            if(handled.responseCode == Network.Response.RESPONSE_CREATED)
                return Long.parseLong(response.responseMessage);
            return null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateGroup(long id, String name, String place, NetworkExceptionHandler exceptionHandler)
        throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), GROUPS + id);
            Map<String, String> params   = new HashMap<>(3);
            params.put(JSON_KEY_NAME, name);
            params.put(JSON_KEY_PLACE, place);

            Network.Response    response = Network.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateImage(long id, File image) throws IOException {

        return false;
    }

    public static void getUsers(int requestId, long id, Network.NetworkCallbacks callbacks) {
        try {
            Network.getDataAsync(requestId, new URL(Network.getDomain(), GROUPS + id + "/members"), callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
