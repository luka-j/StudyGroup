package rs.luka.android.studygroup.io.network;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.MediaCleanup;
import rs.luka.android.studygroup.io.database.LessonTable;

/**
 * Created by luka on 4.1.16..
 */
public class Lessons {
    public static final String LESSONS = "/lessons";
    private static final String TAG = "studygroup.net.Lessons";
    private static final String COURSE = "course/";
    private static final String LESSON = "/lesson/";

    private static final String JSON_KEY_NAME       = "name";
    private static final String JSON_KEY_NOTENO     = "noteNo";
    private static final String JSON_KEY_QUESTIONNO = "questionNo";
    private static final String JSON_KEY_PERMISSION = "requiredPermission";

    public static class Lesson implements Comparable<Lesson> {
        public final String name;
        public final int noteNo;
        public final int questionNo;
        public final int permission;

        public Lesson(String name, int noteNo, int questionNo, int permission) {
            this.name = name;
            this.noteNo = noteNo;
            this.questionNo = questionNo;
            this.permission = permission;
        }

        @Override
        @NonNull
        public int compareTo(@NonNull Lesson another) {
            if(permission < another.permission) return -1;
            if(permission > another.permission) return 1;
            return 0;
        }
    }

    public static void getLessons(Context c, long courseId, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL              url      = new URL(Network.getDomain(), COURSE + courseId + LESSONS);
            Network.Response<String> response = NetworkRequests.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray array   = new JSONArray(response.responseData);
                int       len     = array.length();
                Lesson[] lessons = new Lesson[len];
                for(int i=0; i<len; i++) {
                    JSONObject jsonLesson = array.getJSONObject(i);
                    lessons[i] = new Lesson(jsonLesson.getString(JSON_KEY_NAME), jsonLesson.getInt(JSON_KEY_NOTENO),
                                            jsonLesson.getInt(JSON_KEY_QUESTIONNO), jsonLesson.getInt(JSON_KEY_PERMISSION));
                }
                Arrays.sort(lessons);
                LessonTable db = new LessonTable(c);
                db.clearLessons(courseId);
                db.insertLessons(courseId, lessons);
                MediaCleanup.cleanupLessons(c, courseId, lessons);
            } else {
                Log.w(TAG, "Something's wrong; server returned code " + response.responseCode);

                Network.Response handled = response.handleErrorCode(exceptionHandler);
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
            oldName = URLEncoder.encode(oldName, "UTF-8").replace("+", "%20");
            newName = URLEncoder.encode(newName, "UTF-8").replace("+", "%20");
            URL              url      = new URL(Network.getDomain(), COURSE + courseId + LESSON + oldName);
            Map<String, String> params = new HashMap<>(1);
            params.put(JSON_KEY_NAME, newName);

            Network.Response    response = NetworkRequests.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hideLesson(long courseId, String lesson, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            lesson = URLEncoder.encode(lesson, "UTF-8").replace("+", "%20");
            URL              url      = new URL(Network.getDomain(), COURSE + courseId + "/" + lesson + "/hide");

            Network.Response    response = NetworkRequests.requestPutData(url, NetworkRequests.emptyMap);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleErrorCode(exceptionHandler);
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
