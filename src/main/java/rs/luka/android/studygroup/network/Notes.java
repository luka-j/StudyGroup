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
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.model.User;

/**
 * Created by luka on 4.1.16..
 */
public class Notes {
    private static final String TAG = "studygroup.net.Notes";

    public static final String NOTES = "notes/";
    private static final String COURSE = "course/";

    private static final String JSON_KEY_ID = "id";
    private static final String JSON_KEY_GROUPID = "groupId";
    private static final String JSON_KEY_COURSEID = "courseId";
    private static final String JSON_KEY_TEXT = "text";
    private static final String JSON_KEY_LESSON = "lesson";
    private static final String JSON_KEY_HASIMAGE = "hasImage";
    private static final String JSON_KEY_HASAUDIO = "hasAudio";


    public static void getNotes(Context c, long courseId, String lesson, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL              url      = new URL(Network.getDomain(), COURSE + courseId + "/" +
                                                                     (lesson.isEmpty()?"%20":lesson) + "/" + NOTES);
            Network.Response response = Network.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray array = new JSONArray(response.responseMessage);
                int       len   = array.length();
                Note[]    notes = new Note[len];
                for (int i = 0; i < len; i++) {
                    JSONObject jsonNote = array.getJSONObject(i);
                    notes[i] = new Note(new ID(jsonNote.getLong(JSON_KEY_GROUPID),
                                               courseId,
                                               jsonNote.getLong(JSON_KEY_ID)),
                                        lesson,
                                        jsonNote.getString(JSON_KEY_TEXT),
                                        jsonNote.getBoolean(JSON_KEY_HASIMAGE),
                                        jsonNote.getBoolean(JSON_KEY_HASAUDIO));
                }
                Database.getInstance(c).clearNotes(courseId, lesson);
                Database.getInstance(c).insertNotes(notes);
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

    public static Long createNote(long courseId, String lesson, String text,
                                    NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url    = new URL(Network.getDomain(), NOTES);
            Map<String, String> params = new HashMap<>(3);
            params.put(JSON_KEY_COURSEID, String.valueOf(courseId));
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_TEXT, text);

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

    public static boolean updateNote(long id, String lesson, String text,
                                       NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), NOTES + id);
            Map<String, String> params   = new HashMap<>(2);
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_TEXT, text);

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
