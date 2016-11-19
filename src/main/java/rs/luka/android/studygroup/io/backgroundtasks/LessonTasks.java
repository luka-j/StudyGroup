package rs.luka.android.studygroup.io.backgroundtasks;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.util.Log;

import java.io.IOException;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.database.LessonTable;
import rs.luka.android.studygroup.io.network.Lessons;
import rs.luka.android.studygroup.misc.TextUtils;
import rs.luka.android.studygroup.model.ID;

import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.pushToExecutor;

/**
 * Created by luka on 17.10.16..
 */
public class LessonTasks {
    public static final int     LOADER_ID      = 5;
    static final String LAST_FETCH_KEY = "lfLessons";
    private static final int    FETCH_TIMEOUT  = 1000 * 60 * 15; //15min
    private static final String TAG            = "background.LessonTasks";

    public static void getLessons(final Context c, final long courseId,
                                  final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                  final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                if((currentTime - DataManager.getLastFetch(c, LAST_FETCH_KEY)) > FETCH_TIMEOUT) {
                    try {
                        Lessons.getLessons(c, courseId, exceptionHandler);
                        exceptionHandler.finished();
                        DataManager.writeLastFetch(c, LAST_FETCH_KEY);
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                manager.initLoader(LOADER_ID, null, callbacks);
            }
        });
    }

    public static void refreshLessons(final Context c, final long courseId,
                                      final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                      final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Lessons.getLessons(c, courseId, exceptionHandler);
                    exceptionHandler.finished();
                    DataManager.writeLastFetch(c, LAST_FETCH_KEY);
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                manager.restartLoader(LOADER_ID, null, callbacks);
            }
        });
    }

    public static void hideLesson(final Context c, final ID courseId, final String lesson,
                                  final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Lessons.hideLesson(courseId.getCourseIdValue(), lesson, exceptionHandler);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                new LessonTable(c).removeLesson(courseId, lesson);
            }
        });

    }

    public static void renameLesson(final Context c, final ID courseId, final String oldName, final String newName,
                                    final NetworkExceptionHandler handler) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    String newNameReal = TextUtils.replaceEscapes(newName);
                    boolean success = Lessons.renameLesson(courseId.getCourseIdValue(), oldName, newNameReal, handler);
                    if(success) {
                        new LessonTable(c).renameLesson(courseId, oldName, newNameReal);
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
}