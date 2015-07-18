package rs.luka.android.studygroup.networkcontroller;

import android.util.Log;

import java.io.File;

/**
 * Created by luka on 14.7.15..
 */
public class Adder {
    private static final String TAG = "studygroup.network.Add";

    public static void addCourse(String name, String teacher, String year, File image) {
        Log.i(TAG, "Add course: " + name + " by " + teacher + ", y" + year + " with image " + getNullableFilePath(image));
    }

    public static void addNote(String lesson, String note, File imageFile, File audioFile) {
        Log.i(TAG, "Add note: lesson " + lesson + "\nNote: " + note + "\nFiles: image "
                + getNullableFilePath(imageFile) + ", audio: " + getNullableFilePath(audioFile));
    }

    public static void addQuestion(String lesson, String question, String answer, File imageFile) {
        Log.i(TAG, "Add question: lesson " + lesson + "\nQuestion: " + question + "\nAnswer: " + answer
                + "\nFiles: image " + getNullableFilePath(imageFile));
    }

    private static String getNullableFilePath(File f) {
        if (f == null)
            return "null";
        else return f.getAbsolutePath();
    }
}
