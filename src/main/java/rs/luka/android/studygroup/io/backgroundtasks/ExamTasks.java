package rs.luka.android.studygroup.io.backgroundtasks;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.util.Log;

import java.io.IOException;
import java.util.Date;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.database.ExamTable;
import rs.luka.android.studygroup.io.network.Exams;
import rs.luka.android.studygroup.model.ID;

import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.onUIThread;
import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.pushToExecutor;

/**
 * Created by luka on 17.10.16..
 */
public class ExamTasks {
    public static final int     LOADER_ID      = 4;
    private static final String TAG            = "background.ExamTasks";
    static final String LAST_FETCH_KEY = "lfExams";
    private static final int    FETCH_TIMEOUT  = DataManager.FETCH_TIMEOUT_ITEMS;

    public static void getExams(final Context c, final long groupId,
                                final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        pushToExecutor(() -> {
            if((currentTime - DataManager.getLastFetchTagged(c, LAST_FETCH_KEY, groupId)) > FETCH_TIMEOUT) {
                try {
                    Exams.getExams(c, groupId, exceptionHandler);
                    DataManager.writeLastFetchTagged(c, LAST_FETCH_KEY, groupId);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
            }
            onUIThread(() -> manager.initLoader(LOADER_ID, null, callbacks));
        });
    }

    public static void refreshExams(final Context c, final long groupId,
                                    final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                    final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(() -> {
            try {
                Exams.getExams(c, groupId, exceptionHandler);
                DataManager.writeLastFetchTagged(c, LAST_FETCH_KEY, groupId);
                exceptionHandler.finished();
            } catch (IOException e) {
                exceptionHandler.handleIOException(e);
            }
            onUIThread(() -> manager.restartLoader(LOADER_ID, null, callbacks));
        });

    }

    public static void addExam(final Context c, final ID courseId, final String klass, final String lesson,
                               final String type, final Date date, final NetworkExceptionHandler handler) {
        pushToExecutor(() -> {
            try {
                Long examId = Exams.createExam(courseId.getGroupIdValue(), courseId.getCourseIdValue(),
                                                   klass, lesson, type, date.getTime(), handler);
                if(examId != null) {
                    ID id = new ID(courseId, examId);
                    new ExamTable(c).insertExam(id, klass, lesson, type, date.getTime());
                    handler.finished();
                } else {
                    Log.w(TAG, "network.Exams#createExam returned null; exception should have been handled");
                }
            } catch (IOException ex) {
                handler.handleIOException(ex);
            }
        });
    }

    public static void editExam(final Context c, final ID id, final String klass, final String lesson,
                                final String type, final Date date, final NetworkExceptionHandler handler) {
        pushToExecutor(() -> {
            try {
                boolean success = Exams.updateExam(id.getItemIdValue(), lesson, date.getTime(), handler);
                if(success) {
                    new ExamTable(c).updateExam(id, klass, lesson, type, date.getTime());
                    handler.finished();
                } else {
                    Log.w(TAG, "network.Exams#updateExam returned false; exception should have been handled");
                }
            } catch (IOException ex) {
                handler.handleIOException(ex);
            }
        });
    }

    public static void hideExam(final Context c, final ID id, final String lesson,
                                final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(() -> {
            try {
                boolean success = Exams.hideExam(id.getItemIdValue(), exceptionHandler);
                exceptionHandler.finished();
            } catch (IOException e) {
                exceptionHandler.handleIOException(e);
            }
            new ExamTable(c).removeExam(id, lesson);
        });
    }
}
