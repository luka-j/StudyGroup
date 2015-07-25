package rs.luka.android.studygroup.io;

import android.util.Log;

import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 19.7.15..
 */
public class Hider {
    private static final String TAG = "studygroup.network.Hide";

    public static void hideCourse(ID id) {
        Log.i(TAG, "Hiding course id: " + id.toString());
    }

    public static void showCourse(ID id) {
        Log.i(TAG, "Showing course id: " + id.toString());
    }

    public static void hideLesson(ID courseId, String lesson) {
        Log.i(TAG, "Hiding lesson: courseId " + courseId.toString() + ", lesson " + lesson);
    }

    public static void showLesson(ID courseId, String lesson) {
        Log.i(TAG, "Showing lesson: courseId " + courseId.toString() + ", lesson " + lesson);
    }

    public static void hideNote(ID noteId) {
        Log.i(TAG, "Hiding note: " + noteId.toString());
    }

    public static void showNote(ID noteId) {
        Log.i(TAG, "Showing note: " + noteId.toString());
    }
}
