package rs.luka.android.studygroup.io.network;

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
import rs.luka.android.studygroup.io.database.NoteTable;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.Note;

/**
 * Created by luka on 4.1.16..
 */
public class Notes {
    private static final String V = Network.API_VERSION;
    public static final String NOTES = "notes/";
    private static final String TAG = "studygroup.net.Notes";
    private static final String COURSE = "course/";

    private static final String JSON_KEY_ID         = "id";
    private static final String JSON_KEY_GROUPID    = "groupId";
    private static final String JSON_KEY_COURSEID   = "courseId";
    private static final String JSON_KEY_TEXT       = "text";
    private static final String JSON_KEY_LESSON     = "lesson";
    private static final String JSON_KEY_HASIMAGE   = "hasImage";
    private static final String JSON_KEY_HASAUDIO   = "hasAudio";
    private static final String JSON_KEY_PERMISSION = "requiredPermission";
    private static final String JSON_KEY_ORDER      = "order";


    public static void getNotes(Context c, long courseId, String lesson, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL              url      = new URL(Network.getDomain(), V + COURSE + courseId + "/" +
                                                                     (lesson.isEmpty()? "%20": URLEncoder.encode(lesson, "UTF-8")).replace("+", "%20")
                                                                     + "/" + NOTES);
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
                                        jsonNote.getBoolean(JSON_KEY_HASAUDIO),
                                        jsonNote.getInt(JSON_KEY_ORDER));
                }
                NoteTable db = new NoteTable(c);
                db.clearNotes(courseId, lesson);
                db.insertNotes(notes);
                MediaCleanup.cleanupNotes(c, courseId, notes);
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

    public static Long createNote(long courseId, String lesson, String text,
                                    NetworkExceptionHandler exceptionHandler, int permission) throws IOException {
        try {
            URL                 url    = new URL(Network.getDomain(), V + NOTES);
            Map<String, String> params = new HashMap<>(4);
            params.put(JSON_KEY_COURSEID, String.valueOf(courseId));
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_TEXT, text);
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

    public static boolean updateNote(long id, String lesson, String text,
                                       NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL                 url      = new URL(Network.getDomain(), V + NOTES + id);
            Map<String, String> params   = new HashMap<>(2);
            params.put(JSON_KEY_LESSON, lesson);
            params.put(JSON_KEY_TEXT, text);

            Network.Response    response = NetworkRequests.requestPutData(url, params);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;


            Network.Response handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_OK;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hideNote(long noteId, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), V + NOTES + noteId + "/hide");

            Network.Response    response = NetworkRequests.requestPutData(url, NetworkRequests.emptyMap);
            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;

            Network.Response handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_OK;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void showAllNotes(int requestId, long courseId, String lesson,
                                    Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), V + COURSE + courseId + "/" + lesson + "/showAllNotes");
            NetworkRequests.putDataAsync(requestId, url, NetworkRequests.emptyMap, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getEdits(int requestId, long noteId, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url = new URL(Network.getDomain(), V + NOTES + noteId + "/edits");
            NetworkRequests.getDataAsync(requestId, url, callbacks);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeNotes(int requestId, Set<Long> noteIds, Network.NetworkCallbacks<String> callbacks) {
        try {
            URL url;
            for(Long id : noteIds) {
                url = new URL(Network.getDomain(), V + NOTES + id);
                NetworkRequests.deleteDataAsync(requestId, url, callbacks);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean loadImage(long id, File loadInto, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), V + NOTES + id + "/image");
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
            URL url = new URL(Network.getDomain(), V + NOTES + id + "/image?size=" + size);
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
            System.out.println("updating note image");
            URL url = new URL(Network.getDomain(), V + NOTES + id + "/image");
            Network.Response<File> response = NetworkRequests.requestPutFile(url, image);
            System.out.println("request done");

            if(response.responseCode == Network.Response.RESPONSE_CREATED)
                return true;
            Network.Response<File> handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean loadAudio(long id, File loadInto, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), V + NOTES + id + "/audio");
            Network.Response<File> response = NetworkRequests.requestGetFile(url, loadInto);

            if(response.responseCode == Network.Response.RESPONSE_OK)
                return true;
            Network.Response<File> handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_OK;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean updateAudio(long id, File audio, NetworkExceptionHandler exceptionHandler)
            throws IOException {
        try {
            URL url = new URL(Network.getDomain(), V + NOTES + id + "/audio");
            Network.Response<File> response = NetworkRequests.requestPutFile(url, audio);

            if(response.responseCode == Network.Response.RESPONSE_CREATED)
                return true;
            Network.Response<File> handled = response.handleErrorCode(exceptionHandler);
            return handled.responseCode == Network.Response.RESPONSE_CREATED;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean reorderNote(long id, int newOrder, NetworkExceptionHandler exceptionHandler) throws IOException {
        try {
            URL url = new URL(Network.getDomain(), V + NOTES + id + "/reorder/" + newOrder);

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
