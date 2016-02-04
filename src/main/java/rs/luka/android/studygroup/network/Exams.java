package rs.luka.android.studygroup.network;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.model.Exam;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 5.1.16..
 */
public class Exams {
    public static final String  EXAMS = "exams/";
    private static final String TAG = "studygroup.net.Exams";
    private static final String GROUP = "group/";
    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_GROUPID = "groupId";
    private static final String JSON_KEY_COURSEID = "courseId";
    private static final String JSON_KEY_LESSON = "lesson";
    private static final String JSON_KEY_CLASS = "klass";
    private static final String JSON_KEY_TYPE = "type";
    private static final String JSON_KEY_DATE = "date";

    public static void getExams(Context c, long groupId, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url      = new URL(Network.getDomain(), GROUP + groupId + "/" + EXAMS);
            Network.Response<String> response = NetworkRequests.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray array = new JSONArray(response.responseData);
                int       len   = array.length();
                Exam[]    exams = new Exam[len];
                for (int i = 0; i < len; i++) {
                    JSONObject jsonNote = array.getJSONObject(i);
                    exams[i] = new Exam(c,
                                        new ID(jsonNote.getLong(JSON_KEY_GROUPID),
                                               jsonNote.getLong(JSON_KEY_COURSEID),
                                               jsonNote.getLong(JSON_KEY_ID)),
                                        jsonNote.getString(JSON_KEY_CLASS),
                                        jsonNote.getString(JSON_KEY_LESSON),
                                        jsonNote.getString(JSON_KEY_TYPE),
                                        new Date(jsonNote.getLong(JSON_KEY_DATE)));
                }
                Database.getInstance(c).clearExams(groupId);
                Database.getInstance(c).insertExams(exams);
            } else {
                Log.w(TAG, "Something's wrong; server returned code " + response.responseCode);
                response.handleErrorCode(exceptionHandler);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            exceptionHandler.handleJsonException();
        }
    }

    public static Long createExam(long groupId, long courseId, String klass, String lesson, String type, long date,
                                  NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url    = new URL(Network.getDomain(), EXAMS);
            Map<String, String> params = new HashMap<>(6);
            params.put(JSON_KEY_GROUPID, String.valueOf(groupId));
            params.put(JSON_KEY_COURSEID, String.valueOf(courseId));
            params.put(JSON_KEY_CLASS, klass);
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_TYPE, type);
            params.put(JSON_KEY_DATE, String.valueOf(date));

            Network.Response<String>    response = NetworkRequests.requestPostData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_CREATED)
                return Long.parseLong(response.responseData);

            Network.Response handled = response.handleErrorCode(exceptionHandler);
            if(handled.responseCode == Network.Response.RESPONSE_CREATED)
                return Long.parseLong(response.responseData);
            return null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateExam(long id, String lesson, long date,
                                     NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), EXAMS + id);
            Map<String, String> params   = new HashMap<>(2);
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_DATE, String.valueOf(date));

            Network.Response    response = NetworkRequests.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getEdits(int requestId, long examId, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), EXAMS + examId + "/edits");
            NetworkRequests.getDataAsync(requestId, url, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hideExam(long examId, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL              url      = new URL(Network.getDomain(), EXAMS + examId + "/hide");

            Network.Response    response = NetworkRequests.requestPutData(url, NetworkRequests.emptyMap);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAllExams(int requestId, long groupId, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL                 url      = new URL(Network.getDomain(), GROUP + groupId + "/showAllExams");
            NetworkRequests.putDataAsync(requestId, url, NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeExam(int requestId, long examId, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), EXAMS + examId);
            NetworkRequests.deleteDataAsync(requestId, url, callbacks);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
