package rs.luka.android.studygroup.io;

import android.util.Log;

import java.util.UUID;

/**
 * Created by luka on 19.7.15..
 */
public class Hider {
    private static final String TAG = "studygroup.network.Hide";

    public static void hideCourse(UUID id) {
        Log.i(TAG, "Hiding course id: " + id.toString());
    }

    public static void showCourse(UUID id) {
        Log.i(TAG, "Showing course id: " + id.toString());
    }

    public static void hideLesson(UUID courseId, String lesson) {
        Log.i(TAG, "Hiding lesson: courseId " + courseId.toString() + ", lesson " + lesson);
    }

    public static void showLesson(UUID courseId, String lesson) {
        Log.i(TAG, "Showing lesson: courseId " + courseId.toString() + ", lesson " + lesson);
    }

    public static void hideNote(UUID courseId, UUID noteId) {
        Log.i(TAG, "Hiding note: courseId " + courseId.toString() + ", note " + noteId.toString());
    }

    public static void showNote(UUID courseId, UUID noteId) {
        Log.i(TAG, "Showing note: courseId " + courseId.toString() + ", note " + noteId.toString());
    }
}
