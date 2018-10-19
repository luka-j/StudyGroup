package rs.luka.android.studygroup.io.backgroundtasks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.io.database.CourseTable;
import rs.luka.android.studygroup.io.network.Courses;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;

import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.getLastFetchTagged;
import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.onUIThread;
import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.pushToExecutor;
import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.writeLastFetchTagged;

/**
 * Created by luka on 17.10.16..
 */
public class CourseTasks {
    public static final int     LOADER_ID            = 1;
    static final String LAST_FETCH_KEY       = "lfCourses";
    private static final int    FETCH_TIMEOUT        = 1000 * 60 * 40; //40min
    private static final String TAG                  = "background.CourseTasks";
    static final String LAST_FETCH_THUMB_KEY = "lfCThumb";

    public static void getCourses(final Context c, final Group group, final android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                  final android.support.v4.app.LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        pushToExecutor(() -> {
            if((currentTime - getLastFetchTagged(c, LAST_FETCH_KEY, group.getIdValue())) > FETCH_TIMEOUT) {
                try {
                    Courses.getCourses(c, group, exceptionHandler);
                    exceptionHandler.finished();
                    writeLastFetchTagged(c, LAST_FETCH_KEY, group.getIdValue());
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
            }
            onUIThread(() -> manager.restartLoader(LOADER_ID, null, callbacks));
        });
    }

    /**
     * Queries local database for course
     * @param c context used to obtain database
     * @param id id of the wanted course
     * @return Course, if it exists in the database
     */
    public static Course getCourse(Context c, ID id) {
        return new CourseTable(c).queryCourse(id);
    }

    public static void refreshCourses(final Context c, final Group group,
                                      final android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                      final android.support.v4.app.LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(() -> {
            try {
                Courses.getCourses(c, group, exceptionHandler);
                exceptionHandler.finished();
                writeLastFetchTagged(c, LAST_FETCH_KEY, group.getIdValue());
            } catch (IOException e) {
                exceptionHandler.handleIOException(e);
            }
            onUIThread(() -> manager.restartLoader(LOADER_ID, null, callbacks));
        });

    }

    public static void addCourse(final Context c, final ID groupId, final String subject, final String teacher,
                                 final Integer year, final File image, final boolean isPrivate,
                                 final NetworkExceptionHandler handler) {
        pushToExecutor(() -> {
            try {
                String displaySubject = subject;
                if(isPrivate) {
                    displaySubject = "{"+c.getString(R.string.private_prefix)+"} " + displaySubject;
                }
                Long courseId = Courses.createCourse(groupId.getGroupIdValue(), displaySubject, teacher, year,
                                                     isPrivate?Group.PERM_WRITE:Group.PERM_READ_CAN_REQUEST_WRITE, handler);
                if(courseId != null) {
                    ID id = new ID(groupId, courseId);
                    new CourseTable(c).insertCourse(id, displaySubject, teacher, year, image != null);
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
        });
    }

    public static void editCourse(final Context c, final ID id, final String subject, final String teacher,
                                  final Integer year, final File image, final NetworkExceptionHandler handler) {
        pushToExecutor(() -> {
            try {
                boolean success = Courses.updateCourse(id.getCourseIdValue(), subject, teacher, year, handler);
                if(success) {
                    new CourseTable(c).updateCourse(id, subject, teacher, year, image != null);
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
        });
    }

    public static void hideCourse(final Context c, final ID id, final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(() -> {
            try {
                boolean success = Courses.hideCourse(id.getCourseIdValue(), exceptionHandler);
                exceptionHandler.finished();
            } catch (IOException e) {
                exceptionHandler.handleIOException(e);
            }
            new CourseTable(c).removeCourse(id);
        });
    }

    public static void getCourseImage(final Context c, final ID id, final int scaleTo,
                                      final NetworkExceptionHandler handler, final ImageView insertInto) {
        DataManager.executor.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                boolean exists = LocalImages.courseImageExists(id);
                if(!exists || currentTime - getLastFetchTagged(c, LAST_FETCH_THUMB_KEY, id.getCourseIdValue())
                              > DataManager.FETCH_TIMEOUT_THUMBS) {
                    Courses.loadImage(id.getCourseIdValue(),
                                      scaleTo,
                                      LocalImages.generateCourseImageFile(id),
                                      handler);
                    writeLastFetchTagged(c, LAST_FETCH_THUMB_KEY, id.getCourseIdValue());
                }
            } catch (IOException e) {
                handler.handleIOException(e);
            }
            try {
                final Bitmap image = LocalImages.getCourseImage(id, scaleTo);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if(insertInto.getContext() != null)
                        insertInto.setImageBitmap(image);
                });
            } catch (IOException e) {
                handler.handleIOException(e);
            }
        });
    }
}
