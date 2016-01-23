package rs.luka.android.studygroup.network;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 4.1.16..
 */
public class Courses {
    public static final String COURSES   = "courses/";
    private static final String TAG = "studygroup.net.Courses";
    private static final String GROUP    = "group/";

    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_GROUP = "groupId";
    private static final String JSON_KEY_SUBJECT = "subject";
    private static final String JSON_KEY_TEACHER = "teacher";
    private static final String JSON_KEY_YEAR = "year";
    private static final String JSON_KEY_HASIMAGE = "hasImage";

    public static void getCourses(Context c, long groupId, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL              url      = new URL(Network.getDomain(), GROUP + groupId + "/" + COURSES);
            Network.Response<String> response = NetworkRequests.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray array   = new JSONArray(response.responseData);
                int       len     = array.length();
                Course[]  courses = new Course[len];
                for (int i = 0; i < len; i++) {
                    JSONObject jsonCourse = array.getJSONObject(i);
                    courses[i] = new Course(new ID(jsonCourse.getLong(JSON_KEY_GROUP),
                                                   jsonCourse.getLong(JSON_KEY_ID)),
                                            jsonCourse.getString(JSON_KEY_SUBJECT),
                                            jsonCourse.getString(JSON_KEY_TEACHER),
                                            (Integer) jsonCourse.get(JSON_KEY_YEAR), //nullable
                                            jsonCourse.getBoolean(JSON_KEY_HASIMAGE));
                }
                Database.getInstance(c).clearCourses(groupId);
                Database.getInstance(c).insertCourses(courses);
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


    public static Long createCourse(long groupId, String subject, String teacher, Integer year,
                                    NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url    = new URL(Network.getDomain(), COURSES);
            Map<String, String> params = new HashMap<>(4);
            params.put(JSON_KEY_GROUP, String.valueOf(groupId));
            params.put(JSON_KEY_SUBJECT, subject);
            params.put(JSON_KEY_TEACHER, teacher);
            params.put(JSON_KEY_YEAR, String.valueOf(year));

            Network.Response<String> response = NetworkRequests.requestPostData(url, params);
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

    public static boolean updateCourse(long id, String subject, String teacher, Integer year,
                                      NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), COURSES + id);
            Map<String, String> params   = new HashMap<>(3);
            params.put(JSON_KEY_SUBJECT, subject);
            params.put(JSON_KEY_TEACHER, teacher);
            params.put(JSON_KEY_YEAR, String.valueOf(year));

            Network.Response    response = NetworkRequests.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;


            Network.Response handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hideCourse(long id, NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), COURSES + id + "/hide");

            Network.Response    response = NetworkRequests.requestPutData(url, NetworkRequests.emptyMap);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void showAllCourses(int requestId, long groupId, NetworkRequests.NetworkCallbacks<String> callbacks) {
        try {
            URL                 url      = new URL(Network.getDomain(), GROUP + groupId + "/showAllCourses");
            NetworkRequests.putDataAsync(requestId, url, NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeCourse(int requestId, long courseId, NetworkRequests.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), COURSES + courseId);
            NetworkRequests.deleteDataAsync(requestId, url, callbacks);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
