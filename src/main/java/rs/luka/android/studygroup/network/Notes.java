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
import java.util.Set;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.Note;

/**
 * Created by luka on 4.1.16..
 */
public class Notes {
    public static final String NOTES = "notes/";
    private static final String TAG = "studygroup.net.Notes";
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
            Network.Response<String> response = NetworkRequests.requestGetData(url);
            if(response.responseCode == Network.Response.RESPONSE_OK) {
                JSONArray array = new JSONArray(response.responseData);
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

    public static boolean updateNote(long id, String lesson, String text,
                                       NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), NOTES + id);
            Map<String, String> params   = new HashMap<>(2);
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_TEXT, text);

            Network.Response    response = NetworkRequests.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;


            Network.Response handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hideNote(long noteId, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), NOTES + noteId + "/hide");

            Network.Response    response = NetworkRequests.requestPutData(url, NetworkRequests.emptyMap);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAllNotes(int requestId, long courseId, String lesson,
                                    Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), COURSE + courseId + "/" + lesson + "/showAllNotes");
            NetworkRequests.putDataAsync(requestId, url, NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getEdits(int requestId, long noteId, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), NOTES + noteId + "/edits");
            NetworkRequests.getDataAsync(requestId, url, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeNotes(int requestId, Set<Long> noteIds, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url;
            for(Long id : noteIds) {
                url = new URL(Network.getDomain(), NOTES + id);
                NetworkRequests.deleteDataAsync(requestId, url, callbacks);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean loadImage(long id, File loadInto, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), NOTES + id + "/image");
            Network.Response<File> response = NetworkRequests.requestGetFile(url, loadInto);

            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;
            Network.Response<File> handled = response.handleException(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_OK;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean loadThumb(long id, int size, File loadInto, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), NOTES + id + "/image?size=" + size);
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
            URL url = new URL(Network.getDomain(), NOTES + id + "/image");
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
