package rs.luka.android.studygroup.io;

import android.app.LoaderManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 18.8.15..
 */
public class DataManager {
    private static final String TAG                  = "studygroup.DataManager";
    private static final String PREFS_NAME           = "fetchHistory";
    private static final String LAST_FETCH_GROUPS    = "lfGroups";
    private static final String LAST_FETCH_COURSES   = "lfCourses";
    private static final String LAST_FETCH_LESSONS   = "lfLessons";
    private static final String LAST_FETCH_NOTES     = "lfNotes";
    private static final String LAST_FETCH_QUESTIONS = "lfQuestions";
    private static final String LAST_FETCH_EXAMS     = "lfExams";

    private static final int   FETCH_TIMEOUT_GROUPS  = 1000 * 60 * 60 * 24; //1d
    private static final int   FETCH_TIMEOUT_COURSES = 1000 * 60 * 60 * 12; //6h
    private static final int   FETCH_TIMEOUT_ITEMS   = 1000 * 60 * 60 * 1; //1h
    private static final int   FETCH_TIMEOUT_LESSONS = 1000 * 60 * 60 * 3; //3h
    private static       short randomCounter         = (short) new Random().nextInt(65530);

    private static ExecutorService executor = Executors.newCachedThreadPool();

    private static long getLastFetch(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(key, 0);
    }

    private static void writeLastFetch(Context context, String key) {
        SharedPreferences        prefs       = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor      = prefs.edit();
        long                     currentTime = System.currentTimeMillis();
        editor.putLong(key, currentTime);
        editor.apply();
    }

    public static void getGroups(final Context c, final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                 final LoaderManager manager) {
        long currentTime = System.currentTimeMillis();

        if((currentTime-getLastFetch(c, LAST_FETCH_GROUPS)) > FETCH_TIMEOUT_GROUPS) {
            //todo download groups
            writeLastFetch(c, LAST_FETCH_GROUPS);
        }
        manager.initLoader(0, null, callbacks);
    }

    public static void refreshGroups(final LoaderManager.LoaderCallbacks callbacks, final LoaderManager manager) {
                //todo download groups
                manager.restartLoader(0, null, callbacks);
    }

    public static void addGroup(final Context c, final String name, final String place, final File image) {
                ID id = ID.generateGroupId();
                //todo upload group
                Database.getInstance(c).insertGroup(id, name, place, image!=null);
                if (image != null) { LocalImages.saveGroupImage(id, name, image); }

    }

    public static long getGroupCount(Context c) {
        return Database.getInstance(c).getGroupCount();
    }

    public static void editGroup(final Context c, final ID id, final String name, final String place, final File image) {
                //todo update group
                Database.getInstance(c).updateGroup(id, name, place, image!=null);
                LocalImages.saveGroupImage(id, name, image);
    }

    public static void getCourses(Context c, final LoaderManager.LoaderCallbacks<Cursor> callbacks, final LoaderManager manager) {
        long currentTime = System.currentTimeMillis();

        if((currentTime-getLastFetch(c, LAST_FETCH_COURSES)) > FETCH_TIMEOUT_COURSES) {
            //todo download courses
            writeLastFetch(c, LAST_FETCH_COURSES);
        }
        manager.initLoader(1, null, callbacks);
    }

    public static Course getCourse(Context c, ID id) {
        return Database.getInstance(c).queryCourse(id);
    }

    public static void refreshCourses(final LoaderManager.LoaderCallbacks<Cursor> callbacks, final LoaderManager manager) {

                //todo download courses
                manager.restartLoader(1, null, callbacks);
    }

    public static void addCourse(final Context c, final ID groupId, final String subject, final String teacher,
                                 final Integer year, final File image) {
                ID id = new ID(groupId, shortTime(), true);
                //todo upload group
                Database.getInstance(c).insertCourse(id, subject, teacher, year, image!=null);
                if (image != null) { LocalImages.saveCourseImage(id, subject, image); }
    }

    public static void editCourse(final Context c, final ID id, final String subject, final String teacher,
                                  final Integer year, final File image) {
                //todo update course
                Database.getInstance(c).updateCourse(id, subject, teacher, year, image!=null);
                LocalImages.saveCourseImage(id, subject, image);
    }

    public static void removeCourse(final Context c, final ID id) {
                //todo remove course
                Database.getInstance(c).removeCourse(id);
    }

    private static short shortTime() {
        randomCounter++;
        return randomCounter;
    }

    public static void getLessons(final Context c, final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                  final LoaderManager manager) {
        long currentTime = System.currentTimeMillis();

        if((currentTime-getLastFetch(c, LAST_FETCH_LESSONS)) > FETCH_TIMEOUT_LESSONS) {
                    //todo download courses
            writeLastFetch(c, LAST_FETCH_LESSONS);
        }
        manager.initLoader(5, null, callbacks);
    }

    public static void refreshLessons(final LoaderManager.LoaderCallbacks<Cursor> callbacks, final LoaderManager manager) {

                //todo download lessons
                manager.restartLoader(5, null, callbacks);
    }

    public static void removeLesson(final Context c, final ID courseId, final String lesson) {
                //todo remove lesson
                Database.getInstance(c).removeLesson(courseId, lesson);
    }

    public static void renameLesson(final Context c, final ID courseId, final String oldName, final String newName) {
                //todo edit lesson
                Database.getInstance(c).renameLesson(courseId, oldName, newName);
    }

    public static void getNotes(final Context c, final LoaderManager.LoaderCallbacks callbacks, final LoaderManager manager) {
        long currentTime = System.currentTimeMillis();

        if((currentTime-getLastFetch(c, LAST_FETCH_NOTES)) > FETCH_TIMEOUT_ITEMS) {
            //todo download ns
            writeLastFetch(c, LAST_FETCH_NOTES);
        }
        manager.initLoader(2, null, callbacks);
    }

    public static void refreshNotes(final LoaderManager.LoaderCallbacks callbacks, final LoaderManager manager) {
                //todo download ns
                manager.restartLoader(2, null, callbacks);
    }

    public static void addNote(final Context c, final ID courseId, final String courseName, final String lesson,
                               final String text, final File image, final File audio) {
                //todo add note
                ID id = new ID(courseId, (int) (System.currentTimeMillis() / 1000));
                Database.getInstance(c).insertNote(id, lesson, text, image!=null, audio!=null);

                if (image != null) {
                    LocalImages.saveItemImage(id, courseName, lesson, image);
                }
                if (audio != null) {
                    LocalAudio.saveNoteAudio(id, courseName, lesson, audio);
                }
    }

    public static void editNote(final Context c, final ID id, final String lesson, final String text,
                                final File imageFile, final File audioFile) {
                //todo edit note
                Database.getInstance(c).updateNote(id, lesson, text, imageFile!=null, audioFile!=null);
                LocalImages.saveItemImage(id, getCourse(c, id).getSubject(), lesson, imageFile);
                LocalAudio.saveNoteAudio(id, getCourse(c, id).getSubject(), lesson, audioFile);
    }

    public static void removeNote(final Context c, final ID noteId, final String lesson) {
                //todo remove note
                Database.getInstance(c).removeNote(noteId, lesson);
    }

    public static void getQuestions(final Context c, final LoaderManager.LoaderCallbacks callbacks,
                                    final LoaderManager manager) {
        long currentTime = System.currentTimeMillis();

        if((currentTime-getLastFetch(c, LAST_FETCH_QUESTIONS)) > FETCH_TIMEOUT_ITEMS) {
                    //todo download qs
            writeLastFetch(c, LAST_FETCH_QUESTIONS);
        }
        manager.initLoader(3, null, callbacks);
    }

    public static void refreshQuestions(final LoaderManager.LoaderCallbacks callbacks, final LoaderManager manager) {
                //todo download qs
                manager.restartLoader(3, null, callbacks);
    }

    public static void addQuestion(final Context c, final ID courseId, final String courseName, final String lesson,
                                   final String question, final String answer, final File image) {
                //todo add q
                ID id = new ID(courseId, (int) (System.currentTimeMillis() / 1000));
                Database.getInstance(c).insertQuestion(id, lesson, question, answer, image!=null);

                if (image != null) {
                    LocalImages.saveItemImage(id, courseName, lesson, image);
                }
    }

    public static void editQuestion(final Context c, final ID id, final String lesson, final String question,
                                    final String answer, final File imageFile) {
                //todo edit q
                Database.getInstance(c).updateQuestion(id, lesson, question, answer, imageFile!=null);
                LocalImages.saveItemImage(id,
                                          getCourse(c, id).getSubject(),
                                          lesson,
                                          imageFile); // TODO: 11.9.15. test performance
    }

    public static void removeQuestion(final Context c, final ID questionId, final String lesson) {
                Database.getInstance(c).removeQuestion(questionId, lesson);
    }

    public static void getExams(final Context c, final LoaderManager.LoaderCallbacks callbacks,
                                final LoaderManager manager) {
        long currentTime = System.currentTimeMillis();

        if((currentTime-getLastFetch(c, LAST_FETCH_EXAMS)) > FETCH_TIMEOUT_ITEMS) {
                    //todo download es
            writeLastFetch(c, LAST_FETCH_EXAMS);
        }
        manager.initLoader(4, null, callbacks);

    }

    public static void refreshExams(final LoaderManager.LoaderCallbacks callbacks, final LoaderManager manager) {
                //todo download es
                manager.restartLoader(4, null, callbacks);
    }

    public static void addExam(final Context c, final ID courseId, final String klass, final String lesson,
                               final String type, final Date date) {
                //todo add exam
                ID id = new ID(courseId, (int) (System.currentTimeMillis() / 1000));
                Database.getInstance(c).insertExam(id, klass, lesson, type, date.getTime());
    }

    public static void editExam(final Context c, final ID id, final String klass, final String lesson,
                                final String type, final Date date) {
                //todo edit exam
                Database.getInstance(c).updateExam(id, klass, lesson, type, date.getTime());
    }

    public static void removeExam(final Context c, final ID id, final String lesson) {
                //todo remove exam
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

    public static File getAudio(ID noteId, String courseName, String lessonName) {
        return LocalAudio.getNoteAudio(noteId, courseName, lessonName);
    }

    public static Bitmap getUserImage(long id) {
        return null;
    }

    public static boolean userImageExists(long id) {
        return false;
    }
}
