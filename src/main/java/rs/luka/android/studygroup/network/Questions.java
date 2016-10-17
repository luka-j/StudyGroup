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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.MediaCleanup;
import rs.luka.android.studygroup.io.database.QuestionTable;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.Question;

/**
 * Created by luka on 4.1.16..
 */
public class Questions {
    public static final String QUESTIONS = "questions/";
    private static final String TAG = "net.Questions";
    private static final String COURSE = "course/";

    private static final String JSON_KEY_ID         = "id";
    private static final String JSON_KEY_GROUPID    = "groupId";
    private static final String JSON_KEY_COURSEID   = "courseId";
    private static final String JSON_KEY_QUESTION   = "question";
    private static final String JSON_KEY_ANSWER     = "answer";
    private static final String JSON_KEY_LESSON     = "lesson";
    private static final String JSON_KEY_HASIMAGE   = "hasImage";
    private static final String JSON_KEY_PERMISSION = "requiredPermission";
    private static final String JSON_KEY_ORDER      = "order";

    public static void getQuestions(Context c, long courseId, String lesson, NetworkExceptionHandler handler)
            throws IOException {
        try {
            URL url      = new URL(Network.getDomain(), COURSE + courseId + "/" +
                                                        (lesson.isEmpty() ? "%20":URLEncoder.encode(lesson, "UTF-8")).replace("+", "%20")
                                                        + "/" + QUESTIONS);
            Network.Response<String> response = NetworkRequests.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray  array     = new JSONArray(response.responseData);
                int        len       = array.length();
                Question[] questions = new Question[len];
                for(int i=0; i<len; i++) {
                    JSONObject jsonQuestion = array.getJSONObject(i);
                    questions[i] = new Question(new ID(jsonQuestion.getLong(JSON_KEY_GROUPID), courseId,
                                                       jsonQuestion.getLong(JSON_KEY_ID)),
                                                lesson,
                                                jsonQuestion.getString(JSON_KEY_QUESTION),
                                                jsonQuestion.getString(JSON_KEY_ANSWER),
                                                jsonQuestion.getBoolean(JSON_KEY_HASIMAGE),
                                                jsonQuestion.getInt(JSON_KEY_ORDER));
                }
                QuestionTable db = new QuestionTable(c);
                db.clearQuestions(courseId, lesson);
                db.insertQuestions(questions);
                MediaCleanup.cleanupQuestions(c, courseId, questions);
            } else {
                Log.w(TAG, "Something's wrong; server returned code " + response.responseCode);

                Network.Response handled = response.handleErrorCode(handler);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            handler.handleJsonException();
        }
    }

    public static Long createQuestion(long courseId, String lesson, String question, String answer,
                                    NetworkExceptionHandler exceptionHandler, int permission) throws IOException {
        try {
            URL                 url    = new URL(Network.getDomain(), QUESTIONS);
            Map<String, String> params = new HashMap<>(5);
            params.put(JSON_KEY_COURSEID, String.valueOf(courseId));
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_QUESTION, question);
            params.put(JSON_KEY_ANSWER, answer);
            params.put(JSON_KEY_PERMISSION, String.valueOf(permission));

            Network.Response<String> response = NetworkRequests.requestPostData(url, params);
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

    public static boolean updateQuestion(long id, String lesson, String question, String answer,
                                     NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), QUESTIONS + id);
            Map<String, String> params   = new HashMap<>(3);
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_QUESTION, question);
            params.put(JSON_KEY_ANSWER, answer);

            Network.Response    response = NetworkRequests.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;


            Network.Response handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hideQuestion(long questionId, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL              url      = new URL(Network.getDomain(), QUESTIONS + questionId + "/hide");

            Network.Response    response = NetworkRequests.requestPutData(url, NetworkRequests.emptyMap);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAllQuestions(int requestId, long courseId, String lesson,
                                        Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), COURSE + courseId + "/" + lesson + "/showAllQuestions");
            NetworkRequests.putDataAsync(requestId, url, NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getEdits(int requestId, long questionId, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), QUESTIONS + questionId + "/edits");
            NetworkRequests.getDataAsync(requestId, url, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeQuestions(int requestId, Set<Long> questionIds,
                                       Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url;
            for(Long id : questionIds) {
                url = new URL(Network.getDomain(), QUESTIONS + id);
                NetworkRequests.deleteDataAsync(requestId, url, callbacks);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean loadImage(long id, File loadInto, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), QUESTIONS + id + "/image");
            Network.Response<File> response = NetworkRequests.requestGetFile(url, loadInto);

            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;
            Network.Response<File> handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_OK;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean loadThumb(long id, int size, File loadInto, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), QUESTIONS + id + "/image?size=" + size);
            Network.Response<File> response = NetworkRequests.requestGetFile(url, loadInto);

            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;
            Network.Response<File> handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_OK;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateImage(long id, File image, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), QUESTIONS + id + "/image");
            Network.Response<File> response = NetworkRequests.requestPutFile(url, image);

            if(response.responseCode == Network.Response.RESPONSE_CREATED)
                return true;
            Network.Response<File> handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean reorderQuestion(long id, int newOrder, NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL url = new URL(Network.getDomain(), QUESTIONS + id + "/reorder/" + newOrder);

            Network.Response    response = NetworkRequests.requestPutData(url, NetworkRequests.emptyMap);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_OK;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
