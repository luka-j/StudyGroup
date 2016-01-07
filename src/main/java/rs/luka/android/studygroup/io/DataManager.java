package rs.luka.android.studygroup.io;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.network.Courses;
import rs.luka.android.studygroup.network.Exams;
import rs.luka.android.studygroup.network.Groups;
import rs.luka.android.studygroup.network.Lessons;
import rs.luka.android.studygroup.network.Notes;
import rs.luka.android.studygroup.network.Questions;

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

    private static final int   FETCH_TIMEOUT_GROUPS  = 1000 * 60 * 60 * 12; //1d
    private static final int   FETCH_TIMEOUT_COURSES = 1000 * 60 * 60 * 6; //6h
    private static final int   FETCH_TIMEOUT_LESSONS = 1000 * 60 * 60 * 2; //2h
    private static final int   FETCH_TIMEOUT_ITEMS   = 1000 * 60 * 20; //20min

    //loader ids, could be anything (yes, they can clash, as long as we know they are from different LoaderManagers)
    private static final int LOADER_ID_GROUPS = 0;
    private static final int LOADER_ID_COURSES = 1;
    private static final int LOADER_ID_NOTES = 2;
    private static final int LOADER_ID_QUESTIONS = 3;
    private static final int LOADER_ID_EXAMS = 4;
    private static final int LOADER_ID_LESSONS = 5;

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

    public static void getGroups(final Activity c, final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                 final LoaderManager manager, final NetworkExceptionHandler handler) {
        final long currentTime = System.currentTimeMillis();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if((currentTime-getLastFetch(c, LAST_FETCH_GROUPS)) > FETCH_TIMEOUT_GROUPS) {
                    try {
                        Groups.getGroups(c, handler);
                        handler.finished();
                        writeLastFetch(c, LAST_FETCH_GROUPS);
                    } catch (IOException e) {
                        handler.handleIOException(e);
                    }
                }
                manager.initLoader(LOADER_ID_GROUPS, null, callbacks);
            }
        });
    }

    public static void refreshGroups(final Context c, final LoaderManager.LoaderCallbacks callbacks,
                                     final LoaderManager manager, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Groups.getGroups(c, handler);
                    handler.finished();
                    writeLastFetch(c, LAST_FETCH_GROUPS);
                } catch (IOException e) {
                    handler.handleIOException(e);
                }
                manager.restartLoader(LOADER_ID_GROUPS, null, callbacks);
            }
        });
    }

    public static void addGroup(final Context c, final String name, final String place, final File image,
                                final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Long groupId = Groups.createGroup(name, place, handler);
                    if(groupId != null) {
                        ID id = new ID(groupId);
                        Database.getInstance(c).insertGroup(id, name, place, image != null);
                        if (image != null) { LocalImages.saveGroupImage(id, name, image); }
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Groups#createGroup returned null; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static long getGroupCount(Context c) {
        return Database.getInstance(c).getGroupCount();
    }

    public static void editGroup(final Context c, final ID id, final String name, final String place, final File image,
                                 final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Groups.updateGroup(id.getGroupIdValue(), name, place, exceptionHandler);
                    if(success) {
                        Database.getInstance(c).updateGroup(id, name, place, image != null);
                        if(image != null)
                            LocalImages.saveGroupImage(id, name, image);
                        exceptionHandler.finished();
                    } else {
                        Log.w(TAG, "network.groups#updateGroup returned false; exception should have been handled");
                    }
                } catch (IOException ex) {
                    exceptionHandler.handleIOException(ex);
                }
            }
        });
    }

    public static void getCourses(final Context c, final long groupId, final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                  final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if((currentTime-getLastFetch(c, LAST_FETCH_COURSES)) > FETCH_TIMEOUT_COURSES) {
                    try {
                        Courses.getCourses(c, groupId,exceptionHandler);
                        exceptionHandler.finished();
                        writeLastFetch(c, LAST_FETCH_COURSES);
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                manager.initLoader(LOADER_ID_COURSES, null, callbacks);
            }
        });
    }

    /**
     * Queries local database for course
     * @param c context used to obtain database
     * @param id id of the wanted course
     * @return Course, if it exists in the database
     */
    public static Course getCourse(Context c, ID id) {
        return Database.getInstance(c).queryCourse(id); /// retrieve if doesn't exist (on current thread)
                                                    // (probably unnecessary, courses are loaded right after groups)
    }

    public static void refreshCourses(final Context c, final long groupId,
                                      final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                      final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Courses.getCourses(c, groupId, exceptionHandler);
                    exceptionHandler.finished();
                    writeLastFetch(c, LAST_FETCH_COURSES);
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                manager.restartLoader(LOADER_ID_COURSES, null, callbacks);
            }
        });
    }

    public static void addCourse(final Context c, final ID groupId, final String subject, final String teacher,
                                 final Integer year, final File image, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Long courseId = Courses.createCourse(groupId.getGroupIdValue(), subject, teacher, year, handler);
                    if(courseId != null) {
                        ID id = new ID(groupId, courseId);
                        Database.getInstance(c).insertCourse(id, subject, teacher, year, image!=null);
                        if (image != null) { LocalImages.saveCourseImage(id, subject, image); }
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Courses#createCourse returned null; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void editCourse(final Context c, final ID id, final String subject, final String teacher,
                                  final Integer year, final File image, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Courses.updateCourse(id.getCourseIdValue(), subject, teacher, year, handler);
                    if(success) {
                        Database.getInstance(c).updateCourse(id, subject, teacher, year, image!=null);
                        if(image != null) LocalImages.saveCourseImage(id, subject, image);
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Courses#updateCourse returned false; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void removeCourse(final Context c, final ID id) {
                //todo remove course
                Database.getInstance(c).removeCourse(id);
    }

    public static void getLessons(final Context c, final long courseId,
                                  final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                  final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if((currentTime-getLastFetch(c, LAST_FETCH_LESSONS)) > FETCH_TIMEOUT_LESSONS) {
                    try {
                        Lessons.getLessons(c, courseId, exceptionHandler);
                        exceptionHandler.finished();
                        writeLastFetch(c, LAST_FETCH_LESSONS);
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                manager.initLoader(LOADER_ID_LESSONS, null, callbacks);
            }
        });
    }

    public static void refreshLessons(final Context c, final long courseId,
                                      final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                      final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Lessons.getLessons(c, courseId, exceptionHandler);
                    exceptionHandler.finished();
                    writeLastFetch(c, LAST_FETCH_LESSONS);
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                manager.restartLoader(LOADER_ID_LESSONS, null, callbacks);
            }
        });
    }

    public static void removeLesson(final Context c, final ID courseId, final String lesson) {
                //todo remove lesson
                Database.getInstance(c).removeLesson(courseId, lesson);
    }

    public static void renameLesson(final Context c, final ID courseId, final String oldName, final String newName,
                                    final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Lessons.renameLesson(courseId.getCourseIdValue(), oldName, newName, handler);
                    if(success) {
                        Database.getInstance(c).renameLesson(courseId, oldName, newName);
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Lessons#renameLesson returned false; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void getNotes(final Context c, final long courseId, final String lesson,
                                final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if((currentTime-getLastFetch(c, LAST_FETCH_NOTES)) > FETCH_TIMEOUT_ITEMS) {
                    try {
                        Notes.getNotes(c, courseId, lesson, exceptionHandler);
                        writeLastFetch(c, LAST_FETCH_NOTES);
                        exceptionHandler.finished();
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                manager.initLoader(LOADER_ID_NOTES, null, callbacks);
            }
        });
    }

    public static void refreshNotes(final Context c, final long courseId, final String lesson,
                                    final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                    final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Notes.getNotes(c, courseId, lesson, exceptionHandler);
                    writeLastFetch(c, LAST_FETCH_NOTES);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                manager.restartLoader(LOADER_ID_NOTES, null, callbacks);
            }
        });
    }

    public static void addNote(final Context c, final ID courseId, final String courseName, final String lesson,
                               final String text, final File image, final File audio, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Long noteId = Notes.createNote(courseId.getCourseIdValue(), lesson, text, handler);
                    if(noteId != null) {
                        ID id = new ID(courseId, noteId);
                        Database.getInstance(c).insertNote(id, lesson, text, image!=null, audio!=null);
                        if (image != null)
                            LocalImages.saveItemImage(id, courseName, lesson, image);
                        if (audio != null)
                            LocalAudio.saveNoteAudio(id, courseName, lesson, audio);
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Notes#createNote returned null; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void editNote(final Context c, final ID id, final String lesson, final String text,
                                final File imageFile, final File audioFile, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Notes.updateNote(id.getItemIdValue(), lesson, text, handler);
                    if(success) {
                        Database.getInstance(c).updateNote(id, lesson, text, imageFile!=null, audioFile!=null);
                        if(imageFile != null)
                            LocalImages.saveItemImage(id, getCourse(c, id).getSubject(), lesson, imageFile);
                        if(audioFile != null)
                            LocalAudio.saveNoteAudio(id, getCourse(c, id).getSubject(), lesson, audioFile);
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Notes#updateNote returned false; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void removeNote(final Context c, final ID noteId, final String lesson) {
                //todo remove note
                Database.getInstance(c).removeNote(noteId, lesson);
    }

    public static void getQuestions(final Context c, final long courseId, final String lesson,
                                    final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                    final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if((currentTime-getLastFetch(c, LAST_FETCH_QUESTIONS)) > FETCH_TIMEOUT_ITEMS) {
                    try {
                        Questions.getQuestions(c, courseId, lesson, exceptionHandler);
                        writeLastFetch(c, LAST_FETCH_QUESTIONS);
                        exceptionHandler.finished();
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                manager.initLoader(LOADER_ID_QUESTIONS, null, callbacks);
            }
        });
    }

    public static void refreshQuestions(final Context c, final long courseId, final String lesson,
                                        final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                        final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Questions.getQuestions(c, courseId, lesson, exceptionHandler);
                    writeLastFetch(c, LAST_FETCH_NOTES);
                    manager.restartLoader(LOADER_ID_QUESTIONS, null, callbacks);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
            }
        });
    }

    private static void addQuestion(final Context c, final ID courseId, final String courseName, final String lesson,
                                    final String question, final String answer, final File image,
                                    final NetworkExceptionHandler handler, final boolean isExam) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Long questionId = Questions.createQuestion(courseId.getCourseIdValue(),
                                                               (isExam?lesson.substring(Question.EXAM_PREFIX.length()):lesson),
                                                               question, answer, handler, isExam);
                    if(questionId != null) {
                        ID id = new ID(courseId, questionId);
                        Database.getInstance(c).insertQuestion(id, lesson, question, answer, image!=null);
                        if (image != null) {
                            LocalImages.saveItemImage(id, courseName, lesson, image);
                        }
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Questions#createQuestion returned null; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void addRegularQuestion(final Context c, final ID courseId, final String courseName, final String lesson,
                                          final String question, final String answer, final File image,
                                          final NetworkExceptionHandler handler) {
        addQuestion(c, courseId, courseName, lesson, question, answer, image, handler, false);
    }

    public static void editQuestion(final Context c, final ID id, final String lesson, final String question,
                                    final String answer, final File imageFile, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Questions.updateQuestion(id.getItemIdValue(), lesson, question, answer, handler);
                    if(success) {
                        Database.getInstance(c).updateQuestion(id, lesson, question, answer, imageFile!=null);
                        if(imageFile != null)
                            LocalImages.saveItemImage(id, getCourse(c, id).getSubject(), lesson, imageFile);
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Questions#updateQuestion returned false; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void removeQuestion(final Context c, final ID questionId, final String lesson) {
        //todo remove question
                Database.getInstance(c).removeQuestion(questionId, lesson);
    }

    public static void getExams(final Context c, final long groupId,
                                final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if((currentTime-getLastFetch(c, LAST_FETCH_EXAMS)) > FETCH_TIMEOUT_ITEMS) {
                    try {
                        Exams.getExams(c, groupId, exceptionHandler);
                        writeLastFetch(c, LAST_FETCH_EXAMS);
                        exceptionHandler.finished();
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                manager.initLoader(LOADER_ID_EXAMS, null, callbacks);
            }
        });
    }

    public static void refreshExams(final Context c, final long groupId,
                                    final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                    final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Exams.getExams(c, groupId, exceptionHandler);
                    writeLastFetch(c, LAST_FETCH_EXAMS);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                manager.restartLoader(LOADER_ID_EXAMS, null, callbacks);
            }
        });
    }

    public static void addExam(final Context c, final ID courseId, final String klass, final String lesson,
                               final String type, final Date date, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Long examId = Exams.createExam(courseId.getGroupIdValue(), courseId.getCourseIdValue(),
                                                       klass, lesson, type, date.getTime(), handler);
                    if(examId != null) {
                        ID id = new ID(courseId, examId);
                        Database.getInstance(c).insertExam(id, klass, lesson, type, date.getTime());
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Exams#createExam returned null; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void editExam(final Context c, final ID id, final String klass, final String lesson,
                                final String type, final Date date, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Exams.updateExam(id.getItemIdValue(), lesson, date.getTime(), handler);
                    if(success) {
                        Database.getInstance(c).updateExam(id, klass, lesson, type, date.getTime());
                        handler.finished();
                    } else {
                        Log.w(TAG, "network.Exams#updateExam returned false; exception should have been handled");
                    }
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void removeExam(final Context c, final ID id, final String lesson) {
                //todo remove exam
                Database.getInstance(c).removeExam(id, lesson);
    }

    public static void addExamQuestion(final Context c, final ID courseId, final String courseName,
                                       final String realLesson, final String question, final String answer,
                                       final File image, final NetworkExceptionHandler handler) {
        addQuestion(c, courseId, courseName, realLesson, question, answer, image, handler, false);
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
