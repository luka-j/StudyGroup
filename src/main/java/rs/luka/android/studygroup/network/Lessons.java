package rs.luka.android.studygroup.network;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Database;

/**
 * Created by luka on 4.1.16..
 */
public class Lessons {
    public static final String LESSONS = "/lessons";
    private static final String TAG = "studygroup.net.Lessons";
    private static final String COURSE = "course/";
    private static final String LESSON = "/lesson/";

    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_NOTENO = "noteNo";
    private static final String JSON_KEY_QUESTIONNO = "questionNo";

    public static void getLessons(Context c, long courseId, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL              url      = new URL(Network.getDomain(), COURSE + courseId + LESSONS);
            Network.Response<String> response = NetworkRequests.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray array   = new JSONArray(response.responseData);
                int       len     = array.length();
                String[] names = new String[len]; int[] noteNo = new int[len]; int[] questionNo = new int[len];
                for(int i=0; i<len; i++) {
                    JSONObject jsonLesson = array.getJSONObject(i);
                    names[i] = jsonLesson.getString(JSON_KEY_NAME);
                    noteNo[i] = jsonLesson.getInt(JSON_KEY_NOTENO);
                    questionNo[i] = jsonLesson.getInt(JSON_KEY_QUESTIONNO);
                }
                Database.getInstance(c).clearLessons(courseId);
                Database.getInstance(c).insertLessons(courseId, names, noteNo, questionNo);
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

    public static boolean renameLesson(long courseId, String oldName, String newName,
                                    NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            oldName = URLEncoder.encode(oldName, "UTF-8"); newName = URLEncoder.encode(newName, "UTF-8");
            URL              url      = new URL(Network.getDomain(), COURSE + courseId + LESSON + oldName);
            Map<String, String> params = new HashMap<>(1);
            params.put(JSON_KEY_NAME, newName);

            Network.Response    response = NetworkRequests.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hideLesson(long courseId, String lesson, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            lesson = URLEncoder.encode(lesson, "UTF-8");
            URL              url      = new URL(Network.getDomain(), COURSE + courseId + "/" +
                                                                     URLEncoder.encode(lesson, "UTF-8") + "/hide");

            Network.Response    response = NetworkRequests.requestPutData(url, NetworkRequests.emptyMap);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAllLessons(int requestId, long courseId, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL                 url      = new URL(Network.getDomain(), COURSE + courseId + "/showAllLessons");
            NetworkRequests.putDataAsync(requestId, url, NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeLesson(int requestId, long courseId, String name,
                                    Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), COURSE + courseId + LESSON + URLEncoder.encode(name, "UTF-8"));
            NetworkRequests.deleteDataAsync(requestId, url, callbacks);
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
