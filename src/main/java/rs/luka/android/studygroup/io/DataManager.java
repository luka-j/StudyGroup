package rs.luka.android.studygroup.io;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.FileIOException;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.misc.TextUtils;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.network.Courses;
import rs.luka.android.studygroup.network.Exams;
import rs.luka.android.studygroup.network.Groups;
import rs.luka.android.studygroup.network.Lessons;
import rs.luka.android.studygroup.network.Notes;
import rs.luka.android.studygroup.network.Questions;
import rs.luka.android.studygroup.network.UserManager;

/**
 * Created by luka on 18.8.15..
 */
public class DataManager {
    //loader ids, could be anything (yes, they can clash, as long as we know they are from different LoaderManagers)
    public static final int LOADER_ID_GROUPS = 0;
    public static final int LOADER_ID_COURSES = 1;
    public static final int LOADER_ID_NOTES = 2;
    public static final int LOADER_ID_QUESTIONS = 3;
    public static final int LOADER_ID_EXAMS = 4;
    public static final int LOADER_ID_LESSONS = 5;

    private static final String TAG                  = "studygroup.DataManager";
    private static final String PREFS_NAME           = "fetchHistory";

    private static final String LAST_FETCH_GROUPS    = "lfGroups";
    private static final String LAST_FETCH_COURSES   = "lfCourses";
    private static final String LAST_FETCH_LESSONS   = "lfLessons";
    private static final String LAST_FETCH_NOTES     = "lfNotes";
    private static final String LAST_FETCH_QUESTIONS = "lfQuestions";
    private static final String LAST_FETCH_EXAMS     = "lfExams";

    private static final String LAST_FETCH_USER_THUMB      = "lfUThumb";
    private static final String LAST_FETCH_GROUP_THUMB     = "lfGThumb";
    private static final String LAST_FETCH_COURSE_THUMB    = "lfCThumb";
    private static final String LAST_FETCH_NOTE_THUMBS     = "lfNThumbs";
    private static final String LAST_FETCH_QUESTION_THUMBS = "lfQThumbs";

    private static final int FETCH_TIMEOUT_GROUPS  = 1000 * 60 * 2; //2min, non-refreshable
    private static final int FETCH_TIMEOUT_THUMBS  = 1000 * 60 * 15; //15min
    private static final int FETCH_TIMEOUT_COURSES = 1000 * 60 * 60 * 3; //3h
    private static final int FETCH_TIMEOUT_LESSONS = 1000 * 60 * 30; //30min
    private static final int FETCH_TIMEOUT_ITEMS   = 1000 * 60 * 5; //5min

    private static final int THUMB_THRESHOLD = 250;

    private static ThreadPoolExecutor executor;
    static {
        executor = new ThreadPoolExecutor(6, 6, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        executor.allowCoreThreadTimeOut(true);
    }

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

    public interface GroupLoaderCallbacks {
        void onGroupsLoaded(Database.GroupCursor groups);
    }
    public static void loadGroups(final Context c, final NetworkExceptionHandler exceptionHandler, final GroupLoaderCallbacks callbacks) {
        final long currentTime = System.currentTimeMillis();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if((currentTime-getLastFetch(c, LAST_FETCH_GROUPS)) > FETCH_TIMEOUT_GROUPS) {
                    try {
                        Groups.getGroups(c, exceptionHandler);
                        exceptionHandler.finished();
                        writeLastFetch(c, LAST_FETCH_GROUPS);
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callbacks.onGroupsLoaded(Database.getInstance(c).queryGroups());
                    }
                });
            }
        });
    }

    /**
     * Queries local database for group
     * @param c context used to obtain database
     * @param id id of the wanted group
     * @return Group, if it exists in the database
     */
    public static Group getGroup(Context c, ID id) {
        return Database.getInstance(c).queryGroup(id);
    }

    public static void refreshGroups(final Context c, final LoaderManager.LoaderCallbacks<Cursor> callbacks,
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
                        if (image != null && image.exists()) {
                            Groups.updateImage(groupId, image, handler);
                            LocalImages.saveGroupImage(id, image);
                        }
                        Database.getInstance(c).insertGroup(id, name, place, image != null, Group.PERM_OWNER);
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
                        if(image != null && !image.equals(LocalImages.generateGroupImageFile(id))) {
                            Groups.updateImage(id.getGroupIdValue(), image, exceptionHandler);
                            LocalImages.saveGroupImage(id, image);
                        }
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

    public static void getCourses(final Context c, final Group group, final android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                  final android.support.v4.app.LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if((currentTime-getLastFetch(c, LAST_FETCH_COURSES)) > FETCH_TIMEOUT_COURSES) {
                    try {
                        Courses.getCourses(c, group,exceptionHandler);
                        exceptionHandler.finished();
                        writeLastFetch(c, LAST_FETCH_COURSES);
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                manager.destroyLoader(LOADER_ID_COURSES);
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
        return Database.getInstance(c).queryCourse(id);
    }

    public static void refreshCourses(final Context c, final Group group,
                                      final android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                      final android.support.v4.app.LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Courses.getCourses(c, group, exceptionHandler);
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
                                 final Integer year, final File image, final boolean isPrivate,
                                 final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String displaySubject = subject;
                    if(isPrivate) {
                        displaySubject += " (" + c.getString(R.string.priv) + ")";
                    }
                    Long courseId = Courses.createCourse(groupId.getGroupIdValue(), displaySubject, teacher, year,
                                                         isPrivate?Group.PERM_WRITE:Group.PERM_READ_PUBLIC, handler);
                    if(courseId != null) {
                        ID id = new ID(groupId, courseId);
                        Database.getInstance(c).insertCourse(id, displaySubject, teacher, year, image!=null);
                        if (image != null) {
                            Courses.updateImage(courseId, image, handler);
                            LocalImages.saveCourseImage(id, image);
                        }
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
                        if(image != null && !image.equals(LocalImages.generateCourseImageFile(id))) {
                            Courses.updateImage(id.getCourseIdValue(), image, handler);
                            LocalImages.saveCourseImage(id, image);
                        }
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

    public static void hideCourse(final Context c, final ID id, final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Courses.hideCourse(id.getCourseIdValue(), exceptionHandler);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                Database.getInstance(c).removeCourse(id);
            }
        });
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

    public static void hideLesson(final Context c, final ID courseId, final String lesson,
                                  final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Lessons.hideLesson(courseId.getCourseIdValue(), lesson, exceptionHandler);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                Database.getInstance(c).removeLesson(courseId, lesson);
            }
        });

    }

    public static void renameLesson(final Context c, final ID courseId, final String oldName, final String newName,
                                    final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String newNameReal = TextUtils.replaceEscapes(newName);
                    boolean success = Lessons.renameLesson(courseId.getCourseIdValue(), oldName, newNameReal, handler);
                    if(success) {
                        Database.getInstance(c).renameLesson(courseId, oldName, newNameReal);
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
                               final String text, final File image, final File audio, final boolean isPrivate,
                               final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String realText = TextUtils.replaceEscapes(text);
                    String realLesson = TextUtils.replaceEscapes(lesson);
                    Long noteId = Notes.createNote(courseId.getCourseIdValue(), realLesson, realText,
                                                   handler, isPrivate?Group.PERM_WRITE:Group.PERM_READ_PUBLIC);
                    if(noteId != null) {
                        ID id = new ID(courseId, noteId);
                        Database.getInstance(c).insertNote(id, realLesson, realText, image!=null, audio!=null, 0);
                        if (image != null) {
                            Notes.updateImage(noteId, image, handler);
                            LocalImages.saveNoteImage(id, courseName, realLesson, image); //erases temp
                        }
                        if (audio != null) {
                            Notes.updateAudio(noteId, audio, handler);
                            LocalAudio.saveNoteAudio(id, courseName, realLesson, audio);
                        }
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
                    String realText = TextUtils.replaceEscapes(text);
                    String realLesson = TextUtils.replaceEscapes(lesson);
                    boolean success = Notes.updateNote(id.getItemIdValue(), realLesson, realText, handler);
                    if(success) {
                        Course course = getCourse(c, id);
                        Database.getInstance(c).updateNote(id, realLesson, realText, imageFile!=null, audioFile!=null);
                        if(imageFile != null
                           && !imageFile.equals(LocalImages.generateNoteImageFile(course.getSubject(), realLesson, id))) {
                            Notes.updateImage(id.getItemIdValue(), imageFile, handler);
                            LocalImages.saveNoteImage(id, course.getSubject(), realLesson, imageFile);
                        }
                        if(audioFile != null &&
                                !audioFile.equals(LocalAudio.generateItemAudioFile(course.getSubject(), realLesson, id)))
                            LocalAudio.saveNoteAudio(id, course.getSubject(), realLesson, audioFile);
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

    public static void reorderNote(final Context c, final ID id, final String lesson, final int newOrder, final int order,
                                   final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Database.getInstance(c).reorderNote(id, lesson, newOrder, order);
                    Notes.reorderNote(id.getItemIdValue(), newOrder, handler);
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void hideNote(final Context c, final ID noteId, final String lesson,
            final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Database.getInstance(c).removeNote(noteId, lesson);
                    boolean success = Notes.hideNote(noteId.getItemIdValue(), exceptionHandler);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
            }
        });
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

    public static void addQuestion(final Context c, final ID courseId, final String courseName, final String lesson,
                                    final String question, final String answer, final File image, final boolean isPrivate,
                                    final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String realQuestion = TextUtils.replaceEscapes(question);
                    String realAnswer = TextUtils.replaceEscapes(answer);
                    String realLesson = TextUtils.replaceEscapes(lesson);
                    Long questionId = Questions.createQuestion(courseId.getCourseIdValue(),
                                                               realLesson,
                                                               realQuestion, realAnswer, handler,
                                                               isPrivate ? Group.PERM_WRITE : Group.PERM_READ_PUBLIC);
                    if(questionId != null) {
                        ID id = new ID(courseId, questionId);
                        Database.getInstance(c).insertQuestion(id, realLesson, realQuestion, realAnswer, image!=null, 0);
                        if (image != null) {
                            Questions.updateImage(questionId, image, handler);
                            LocalImages.saveQuestionImage(id, courseName, realLesson, image);
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

    public static void editQuestion(final Context c, final ID id, final String lesson, final String question,
                                    final String answer, final File imageFile, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String realQuestion = TextUtils.replaceEscapes(question);
                    String realAnswer = TextUtils.replaceEscapes(answer);
                    String realLesson = TextUtils.replaceEscapes(lesson);
                    boolean success = Questions.updateQuestion(id.getItemIdValue(), realLesson, realQuestion, realAnswer, handler);
                    if(success) {
                        Course course = getCourse(c, id);
                        Database.getInstance(c).updateQuestion(id, realLesson, realQuestion, realAnswer, imageFile!=null);
                        if(imageFile != null &&
                                !imageFile.equals(LocalImages.generateQuestionImageFile(course.getSubject(), realLesson, id))) {
                            Questions.updateImage(id.getItemIdValue(), imageFile, handler);
                            LocalImages.saveQuestionImage(id, course.getSubject(), realLesson, imageFile);
                        }
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

    public static void reorderQuestion(final Context context, final ID id, final String lesson, final int newOrder,
                                       final int order, final NetworkExceptionHandler handler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Database.getInstance(context).reorderQuestion(id, lesson, newOrder, order);
                    Questions.reorderQuestion(id.getItemIdValue(), newOrder, handler);
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void hideQuestion(final Context c, final ID questionId, final String lesson,
                                    final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Questions.hideQuestion(questionId.getItemIdValue(), exceptionHandler);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                Database.getInstance(c).removeQuestion(questionId, lesson);
            }
        });
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

    public static void hideExam(final Context c, final ID id, final String lesson,
                                final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Exams.hideExam(id.getItemIdValue(), exceptionHandler);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                Database.getInstance(c).removeExam(id, lesson);
            }
        });
    }

    public static void getGroupImage(final Context c, final ID id, final int scaleTo,
                                     final NetworkExceptionHandler handler, final ImageView insertInto) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long currentTime = System.currentTimeMillis();
                    boolean exists = LocalImages.groupImageExists(id);
                    if(!exists || currentTime - getLastFetch(c, LAST_FETCH_GROUP_THUMB) > FETCH_TIMEOUT_THUMBS) {
                        Groups.loadImage(id.getGroupIdValue(), scaleTo, LocalImages.generateGroupImageFile(id), handler);
                        writeLastFetch(c, LAST_FETCH_GROUP_THUMB);
                    }
                } catch (IOException e) {
                    handler.handleIOException(e);
                }
                try {
                    final Bitmap image;
                    image = LocalImages.getGroupImage(id, scaleTo);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            insertInto.setImageBitmap(image);
                        }
                    });
                } catch (IOException e) {
                    handler.handleIOException(e);
                }
            }
        });
    }

    public static void getCourseImage(final Context c, final ID id, final int scaleTo,
                                      final NetworkExceptionHandler handler, final ImageView insertInto) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long currentTime = System.currentTimeMillis();
                    boolean exists = LocalImages.courseImageExists(id);
                    if(!exists || currentTime - getLastFetch(c, LAST_FETCH_COURSE_THUMB) > FETCH_TIMEOUT_THUMBS) {
                        Courses.loadImage(id.getCourseIdValue(),
                                          scaleTo,
                                          LocalImages.generateCourseImageFile(id),
                                          handler);
                        writeLastFetch(c, LAST_FETCH_COURSE_THUMB);
                    }
                } catch (IOException e) {
                    handler.handleIOException(e);
                }
                try {
                    final Bitmap image = LocalImages.getCourseImage(id, scaleTo);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            insertInto.setImageBitmap(image);
                        }
                    });
                } catch (IOException e) {
                    handler.handleIOException(e);
                }
            }
        });
    }

    public static void getUserImage(final Context c, final long id, final int scaleTo,
                                      final NetworkExceptionHandler handler, final ImageView insertInto) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long currentTime = System.currentTimeMillis();
                    boolean exists = LocalImages.userThumbExists(id);
                    if(!exists || currentTime - getLastFetch(c, LAST_FETCH_USER_THUMB) > FETCH_TIMEOUT_THUMBS) {
                        UserManager.getImage(id, scaleTo, LocalImages.generateUserThumbFile(id), handler);
                        writeLastFetch(c, LAST_FETCH_USER_THUMB);
                    }
                } catch (IOException e) {
                    handler.handleIOException(e);
                }
                try {
                    final Bitmap image = LocalImages.getUserThumb(id, scaleTo);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            insertInto.setImageBitmap(image);
                        }
                    });
                } catch (IOException e) {
                    handler.handleIOException(e);
                }
            }
        });
    }

    public static void getNoteImage(final Context c, final ID id, final String courseName, final String lessonName,
                                    final int scaleTo, final NetworkExceptionHandler handler, final ImageView insertInto) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(scaleTo > THUMB_THRESHOLD)
                    getNoteFullsizeImage(id, courseName, lessonName, scaleTo, handler, insertInto);
                else
                    getNoteThumb(c, id, courseName, lessonName, scaleTo, handler, insertInto);
            }
        });
    }

    private static void getNoteFullsizeImage(final ID id, final String courseName, final String lessonName,
                                             final int scaleTo, final NetworkExceptionHandler handler,
                                             final ImageView insertInto) {
        try {
            boolean exists = LocalImages.noteImageExists(courseName, lessonName, id);
            if(!exists) {
                Notes.loadImage(id.getItemIdValue(),
                                LocalImages.generateNoteImageFile(courseName, lessonName, id),
                                handler);
            }
        } catch (IOException e) {
            handler.handleIOException(e);
        }

        try {
            final Bitmap image = LocalImages.getNoteImage(courseName, lessonName, id, scaleTo);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    insertInto.setImageBitmap(image);
                }
            });
        } catch (IOException e) {
            handler.handleIOException(e);
        }
    }


    public static void getNoteThumb(final Context c, final ID id, final String courseName, final String lessonName,
                                    final int scaleTo, final NetworkExceptionHandler exceptionHandler,
                                    final ImageView insertInto) {
        try {
            long currentTime = System.currentTimeMillis();
            boolean exists = LocalImages.noteThumbExists(courseName, lessonName, id);
            if(!exists) {
                Notes.loadThumb(id.getItemIdValue(),
                                scaleTo,
                                LocalImages.generateNoteThumbFile(courseName, lessonName, id),
                                exceptionHandler);
                writeLastFetch(c, LAST_FETCH_NOTE_THUMBS);
            } else if(currentTime - getLastFetch(c, LAST_FETCH_NOTE_THUMBS) > FETCH_TIMEOUT_THUMBS) {
                File current = LocalImages.generateNoteThumbFile(courseName, lessonName, id);
                File old = LocalImages.invalidateThumb(current);
                Notes.loadThumb(id.getItemIdValue(), scaleTo, current, exceptionHandler);
                boolean same = LocalImages.thumbsEqual(old, current);
                if(old.exists() && !old.delete()) throw new FileIOException(old, "Cannot delete");
                if(!same) {
                    LocalImages.deleteNoteImage(courseName, lessonName, id);
                }
                writeLastFetch(c, LAST_FETCH_NOTE_THUMBS);
            }
        } catch (IOException e) {
            exceptionHandler.handleIOException(e);
        }
        try {
            final Bitmap image;
            image = LocalImages.getNoteThumb(courseName, lessonName, id, scaleTo);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    insertInto.setImageBitmap(image);
                }
            });
        } catch (IOException e) {
            exceptionHandler.handleIOException(e);
        }
    }


    public static void getQuestionImage(final Context c, final ID id, final String courseName, final String lessonName,
                                    final int scaleTo, final NetworkExceptionHandler handler, final ImageView insertInto) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(scaleTo > THUMB_THRESHOLD)
                    getQuestionFullsizeImage(id, courseName, lessonName, scaleTo, handler, insertInto);
                else
                    getQuestionThumb(c, id, courseName, lessonName, scaleTo, handler, insertInto);
            }
        });
    }

    private static void getQuestionFullsizeImage(final ID id, final String courseName, final String lessonName,
                                             final int scaleTo, final NetworkExceptionHandler handler,
                                             final ImageView insertInto) {
        try {
            boolean exists = LocalImages.questionImageExists(courseName, lessonName, id);
            if(!exists) {
                Questions.loadImage(id.getItemIdValue(),
                                LocalImages.generateQuestionImageFile(courseName, lessonName, id),
                                handler);
            }
        } catch (IOException e) {
            handler.handleIOException(e);
        }
        try {
            final Bitmap image = LocalImages.getQuestionImage(courseName, lessonName, id, scaleTo);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    insertInto.setImageBitmap(image);
                }
            });
        } catch (IOException e) {
            handler.handleIOException(e);
        }
    }

    public static void getQuestionThumb(final Context c, final ID id, final String courseName, final String lessonName,
                                    final int scaleTo, final NetworkExceptionHandler exceptionHandler,
                                    final ImageView insertInto) {
        try {
            long currentTime = System.currentTimeMillis();
            boolean exists = LocalImages.questionThumbExists(courseName, lessonName, id);
            if(!exists) {
                Questions.loadThumb(id.getItemIdValue(),
                                    scaleTo,
                                    LocalImages.generateQuestionThumbFile(courseName, lessonName, id),
                                    exceptionHandler);
                writeLastFetch(c, LAST_FETCH_QUESTION_THUMBS);
            } else if(currentTime - getLastFetch(c, LAST_FETCH_QUESTION_THUMBS) > FETCH_TIMEOUT_THUMBS) {
                File current = LocalImages.generateQuestionThumbFile(courseName, lessonName, id);
                File old = LocalImages.invalidateThumb(current);
                Questions.loadThumb(id.getItemIdValue(), scaleTo, current, exceptionHandler);
                boolean same = LocalImages.thumbsEqual(old, current);
                if(!old.delete()) throw new FileIOException(old, "Cannot delete");
                if(!same) {
                    LocalImages.deleteQuestionImage(courseName, lessonName, id);
                }
                writeLastFetch(c, LAST_FETCH_QUESTION_THUMBS);
            }
        } catch (IOException e) {
            exceptionHandler.handleIOException(e);
        }
        try {
            final Bitmap image;
            image = LocalImages.getQuestionThumb(courseName, lessonName, id, scaleTo);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    insertInto.setImageBitmap(image);
                }
            });
        } catch (IOException e) {
            exceptionHandler.handleIOException(e);
        }
    }

    public static void setMyProfile(final String username, final String email, final File image,
                                    final NetworkExceptionHandler exceptionHandler) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = UserManager.updateMyProfile(username, email, exceptionHandler);
                    if(success) {
                        User.setMyDetails(0, username, email, image != null);
                        File thumb = LocalImages.generateUserThumbFile(User.getLoggedInUser().getId());
                        if(image != null && !image.equals(thumb)) {
                            UserManager.updateMyImage(image, exceptionHandler);
                            thumb.delete();
                            Utils.copyFile(image, thumb);
                        }
                        exceptionHandler.finished();
                    }
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
            }
        });
    }

    public interface AudioCallbacks {
        void onAudioReady(int requestId, File audioFile);
    }
    public static void getAudio(final int requestId, final ID noteId, final String courseName, final String lessonName,
                                final NetworkExceptionHandler exceptionHandler, final AudioCallbacks callbacks) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File file = LocalAudio.generateItemAudioFile(courseName, lessonName, noteId);
                try {
                    Notes.loadAudio(noteId.getItemIdValue(), file, exceptionHandler);
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                callbacks.onAudioReady(requestId, file);
            }
        });
    }

}
