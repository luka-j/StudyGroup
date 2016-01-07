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
import rs.luka.android.studygroup.io.Network;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.model.User;

/**
 * Created by luka on 4.1.16..
 */
public class Questions {
    private static final String TAG = "net.Questions";

    public static final String QUESTIONS = "questions/";
    private static final String COURSE = "course/";

    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_GROUPID = "groupId";
    private static final String JSON_KEY_COURSEID = "courseId";
    private static final String JSON_KEY_QUESTION = "question";
    private static final String JSON_KEY_ANSWER = "answer";
    private static final String JSON_KEY_LESSON = "lesson";
    private static final String JSON_KEY_HASIMAGE = "hasImage";


    public static void getQuestions(Context c, long courseId, String lesson, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url      = new URL(Network.getDomain(), COURSE + courseId + "/" +
                                                        (lesson.isEmpty()?"%20":lesson) + "/" + QUESTIONS);
            Network.Response response = Network.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray  array     = new JSONArray(response.responseMessage);
                int        len       = array.length();
                Question[] questions = new Question[len];
                for(int i=0; i<len; i++) {
                    JSONObject jsonQuestion = array.getJSONObject(i);
                    questions[i] = new Question(new ID(jsonQuestion.getLong(JSON_KEY_GROUPID), courseId,
                                                       jsonQuestion.getLong(JSON_KEY_ID)),
                                                lesson,
                                                jsonQuestion.getString(JSON_KEY_QUESTION),
                                                jsonQuestion.getString(JSON_KEY_ANSWER),
                                                jsonQuestion.getBoolean(JSON_KEY_HASIMAGE));
                }
                Database.getInstance(c).clearQuestions(courseId, lesson);
                Database.getInstance(c).insertQuestions(questions);
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

    public static Long createQuestion(long courseId, String lesson, String question, String answer,
                                    NetworkExceptionHandler exceptionHandler, boolean isExam) throws IOException {
        try {
            URL                 url    = new URL(Network.getDomain(), (isExam? Exams.EXAMS + "question" : QUESTIONS));
            Map<String, String> params = new HashMap<>(4);
            params.put(JSON_KEY_COURSEID, String.valueOf(courseId));
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_QUESTION, question);
            params.put(JSON_KEY_ANSWER, answer);

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

    public static boolean updateQuestion(long id, String lesson, String question, String answer,
                                     NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), QUESTIONS + id);
            Map<String, String> params   = new HashMap<>(3);
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_QUESTION, question);
            params.put(JSON_KEY_ANSWER, answer);

            Network.Response    response = Network.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;


            Network.Response handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
