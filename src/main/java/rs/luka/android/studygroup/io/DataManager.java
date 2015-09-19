package rs.luka.android.studygroup.io;

import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.Random;

import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.History;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 18.8.15..
 */
public class DataManager {
    private static final String TAG = "studygroup.DataManager";
    private static final String PREFS_NAME           = "fetchHistory";
    private static final String LAST_FETCH_GROUPS    = "lfGroups";
    private static final String LAST_FETCH_COURSES   = "lfCourses";
    private static final String LAST_FETCH_NOTES     = "lfNotes";
    private static final String LAST_FETCH_QUESTIONS = "lfQuestions";
    private static final String LAST_FETCH_EXAMS     = "lfExams";

    private static final int   FETCH_TIMEOUT_GROUPS  = 1000 * 60 * 60 * 24; //1d
    private static final int   FETCH_TIMEOUT_COURSES = 1000 * 60 * 60 * 12; //6h
    private static final int   FETCH_TIMEOUT_ITEMS   = 1000 * 60 * 60 * 1; //1h
    private static       short randomCounter         = (short) new Random().nextInt(65530);

    public static void getGroups(LoaderManager.LoaderCallbacks<Cursor> callbacks, LoaderManager manager) {
        manager.initLoader(0, null, callbacks);
    }

    public static void refreshGroups(LoaderManager.LoaderCallbacks callbacks, LoaderManager manager) {
        manager.restartLoader(0, null, callbacks);
    }

    public static void addGroup(Context c, String name, String place, File image) {
        ID id = ID.generateGroupId();
        Database.getInstance(c).insertGroup(id, name, place);

        if (image != null) { LocalImages.saveGroupImage(id, name, image); }
    }

    public static long getGroupCount(Context c) {
        return Database.getInstance(c).getGroupCount();
    }

    public static void editGroup(Context c, ID id, String name, String place, File image) {
        Database.getInstance(c).updateGroup(id, name, place);
        LocalImages.saveGroupImage(id, name, image);
    }

    public static void getCourses(LoaderManager.LoaderCallbacks<Cursor> callbacks, LoaderManager manager) {
        manager.initLoader(1, null, callbacks);
    }

    public static Course getCourse(Context c, ID id) {
        return Database.getInstance(c).queryCourse(id);
    }

    public static void refreshCourses(LoaderManager.LoaderCallbacks<Cursor> callbacks, LoaderManager manager) {
        manager.restartLoader(1, null, callbacks);
    }

    public static void addCourse(Context c, ID groupId, String subject, String teacher, Integer year, File image) {
        ID id = new ID(groupId, shortTime());
        Database.getInstance(c).insertCourse(id, subject, teacher, year);

        if (image != null) { LocalImages.saveCourseImage(id, subject, image); }
    }

    public static void editCourse(Context c, ID id, String subject, String teacher, Integer year, File image) {
        Database.getInstance(c).updateCourse(id, subject, teacher, year);
        LocalImages.saveCourseImage(id, subject, image);
    }

    public static void removeCourse(Context c, ID id) {
        Database.getInstance(c).removeCourse(id);
    }

    private static short shortTime() {
        randomCounter++;
        return randomCounter;
    }

    public static void getLessons(LoaderManager.LoaderCallbacks<Cursor> callbacks, LoaderManager manager) {
        manager.initLoader(5, null, callbacks);
    }

    public static void refreshLessons(LoaderManager.LoaderCallbacks<Cursor> callbacks, LoaderManager manager) {
        manager.restartLoader(5, null, callbacks);
    }

    public static void removeLesson(Context c, ID courseId, String lesson) {
        Database.getInstance(c).removeLesson(courseId, lesson);
    }

    public static void renameLesson(Context c, ID courseId, String oldName, String newName) {
        Database.getInstance(c).renameLesson(courseId, oldName, newName);
    }

    public static void getNotes(LoaderManager.LoaderCallbacks callbacks, LoaderManager manager) {
        manager.initLoader(2, null, callbacks);
    }

    public static void refreshNotes(LoaderManager.LoaderCallbacks callbacks, LoaderManager manager) {
        manager.restartLoader(2, null, callbacks);
    }

    public static void addNote(Context c, ID courseId, String courseName, String lesson, String text, File image,
                               File audio) {
        ID id = new ID(courseId, (int) (System.currentTimeMillis() / 1000));
        Database.getInstance(c).insertNote(id, lesson, text);

        if (image != null) {
            LocalImages.saveItemImage(id, courseName, lesson, image);
            Log.i(TAG, "Note image: " + image.getAbsolutePath());
        }
        if (audio != null) {
            LocalAudio.saveNoteAudio(id, courseName, lesson, audio);
            Log.i(TAG, "Note audio: " + audio.getAbsolutePath());
        }
    }

    public static void editNote(Context c, ID id, String lesson, String text, File imageFile, File audioFile) {
        Database.getInstance(c).updateNote(id, lesson, text);
        LocalImages.saveItemImage(id,
                                  getCourse(c, id).getSubject(),
                                  lesson,
                                  imageFile); // TODO: 11.9.15. test performance
    }

    public static void removeNote(Context c, ID noteId, String lesson) {
        Database.getInstance(c).removeNote(noteId, lesson);
    }

    public static void getQuestions(LoaderManager.LoaderCallbacks callbacks, LoaderManager manager) {
        manager.initLoader(3, null, callbacks);
    }

    public static void refreshQuestions(LoaderManager.LoaderCallbacks callbacks, LoaderManager manager) {
        manager.restartLoader(3, null, callbacks);
    }

    public static void addQuestion(Context c, ID courseId, String courseName, String lesson, String question,
                                   String answer, File image) {
        ID id = new ID(courseId, (int) (System.currentTimeMillis() / 1000));
        Database.getInstance(c).insertQuestion(id, lesson, question, answer);

        if (image != null) {
            LocalImages.saveItemImage(id, courseName, lesson, image);
        }
    }

    public static void editQuestion(Context c, ID id, String lesson, String question, String answer,
                                    File imageFile) {
        Database.getInstance(c).updateQuestion(id, lesson, question, answer);
        LocalImages.saveItemImage(id,
                                  getCourse(c, id).getSubject(),
                                  lesson,
                                  imageFile); // TODO: 11.9.15. test performance
    }

    public static void removeQuestion(Context c, ID questionId, String lesson) {
        Database.getInstance(c).removeQuestion(questionId, lesson);
    }

    public static void getExams(LoaderManager.LoaderCallbacks callbacks, LoaderManager manager) {
        manager.initLoader(4, null, callbacks);
    }

    public static void refreshExams(LoaderManager.LoaderCallbacks callbacks, LoaderManager manager) {
        manager.restartLoader(4, null, callbacks);
    }

    public static void addExam(Context c, ID courseId, String klass, String lesson, String type, Date date) {
        ID id = new ID(courseId, (int) (System.currentTimeMillis() / 1000));
        Database.getInstance(c).insertExam(id, klass, lesson, type, date.getTime());
    }

    public static void editExam(Context c, ID id, String klass, String lesson, String type, Date date) {
        Database.getInstance(c).updateExam(id, klass, lesson, type, date.getTime());
    }

    public static void removeExam(Context c, ID id, String lesson) {
        Database.getInstance(c).removeExam(id, lesson);
    }

    public static Bitmap getImage(ID id, String name, String lessonName, int scaleTo) {
        if (id.isGroupId()) {
            return LocalImages.getGroupImage(id, name, scaleTo);
        } else if (id.isCourseId()) {
            return LocalImages.getCourseImage(id, name, scaleTo);
        } else if (id.isItemId()) {
            return LocalImages.getItemImage(name, lessonName, id, scaleTo);
        }
        throw new IllegalArgumentException("Invalid id: " + id.toString());
    }

    public static boolean imageExists(ID id, String name, String lessonName) {
        if (id.isGroupId()) {
            return LocalImages.groupHasImage(id, name);
        } else if (id.isCourseId()) {
            return LocalImages.courseHasImage(id, name);
        } else if (id.isItemId()) {
            return LocalImages.itemHasImage(name, lessonName, id);
        }
        throw new IllegalArgumentException("Invalid id: " + id.toString());
    }

    public static File getAudio(ID noteId, String courseName, String lessonName) {
        return LocalAudio.getNoteAudio(noteId, courseName, lessonName);
    }

    public static boolean audioExists(ID noteId, String courseName, String lessonName) {
        return LocalAudio.noteHasAudio(noteId, courseName, lessonName);
    }

    public static History getHistory(Context c, ID id) {
        History h = new History();
        h.add(new History.HistoryEntry("Pera", new Date(), ""));
        return h;
    }
}
