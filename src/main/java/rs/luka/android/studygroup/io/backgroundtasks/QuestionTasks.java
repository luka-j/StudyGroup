package rs.luka.android.studygroup.io.backgroundtasks;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.exceptions.FileIOException;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.io.database.QuestionTable;
import rs.luka.android.studygroup.io.network.Questions;
import rs.luka.android.studygroup.misc.TextUtils;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;

import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.pushToExecutor;
import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.resetLastFetch;

/**
 * Created by luka on 17.10.16..
 */
public class QuestionTasks {
    public static final int     LOADER_ID = 3;
    private static final String TAG       = "background.QuestionTask";


    private static final int    FETCH_TIMEOUT         = DataManager.FETCH_TIMEOUT_ITEMS;
    static final String LAST_FETCH_KEY        = "lfQuestions";
    static final String LAST_FETCH_THUMBS_KEY = "lfQThumbs";

    public static void getQuestions(final Context c, final long courseId, final String lesson,
                                    final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                    final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                if((currentTime - DataManager.getLastFetch(c, LAST_FETCH_KEY)) > FETCH_TIMEOUT) {
                    try {
                        Questions.getQuestions(c, courseId, lesson, exceptionHandler);
                        DataManager.writeLastFetch(c, LAST_FETCH_KEY);
                        exceptionHandler.finished();
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                manager.initLoader(LOADER_ID, null, callbacks);
            }
        });
    }

    public static void refreshQuestions(final Context c, final long courseId, final String lesson,
                                        final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                        final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Questions.getQuestions(c, courseId, lesson, exceptionHandler);
                    DataManager.writeLastFetch(c, LAST_FETCH_KEY);
                    manager.restartLoader(LOADER_ID, null, callbacks);
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
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    String realQuestion = TextUtils.replaceEscapes(question);
                    String realAnswer = TextUtils.replaceEscapes(answer);
                    String realLesson = TextUtils.replaceEscapes(lesson);
                    Long questionId = Questions.createQuestion(courseId.getCourseIdValue(),
                                                               realLesson,
                                                               realQuestion, realAnswer, handler,
                                                               isPrivate ? Group.PERM_WRITE : Group.PERM_READ_CAN_REQUEST_WRITE);
                    if(questionId != null) {
                        ID id = new ID(courseId, questionId);
                        new QuestionTable(c).insertQuestion(id, realLesson, realQuestion, realAnswer, image != null, 0);
                        if (image != null) {
                            Questions.updateImage(questionId, image, handler);
                            LocalImages.saveQuestionImage(id, courseName, realLesson, image);
                        }
                        resetLastFetch(c, LessonTasks.LAST_FETCH_KEY);
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
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    String realQuestion = TextUtils.replaceEscapes(question);
                    String realAnswer = TextUtils.replaceEscapes(answer);
                    String realLesson = TextUtils.replaceEscapes(lesson);
                    boolean success = Questions.updateQuestion(id.getItemIdValue(), realLesson, realQuestion, realAnswer, handler);
                    if(success) {
                        Course course = CourseTasks.getCourse(c, id);
                        new QuestionTable(c).updateQuestion(id, realLesson, realQuestion, realAnswer, imageFile!=null);
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
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    new QuestionTable(context).reorderQuestion(id, lesson, newOrder, order);
                    Questions.reorderQuestion(id.getItemIdValue(), newOrder, handler);
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void hideQuestion(final Context c, final ID questionId, final String lesson,
                                    final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean success = Questions.hideQuestion(questionId.getItemIdValue(), exceptionHandler);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                new QuestionTable(c).removeQuestion(questionId, lesson);
            }
        });
    }

    public static void getQuestionImage(final Context c, final ID id, final String courseName, final String lessonName,
                                        final int scaleTo, final NetworkExceptionHandler handler, final ImageView insertInto) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                if(scaleTo > DataManager.THUMB_THRESHOLD)
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
                DataManager.writeLastFetch(c, LAST_FETCH_THUMBS_KEY);
            } else if(currentTime - DataManager.getLastFetch(c, LAST_FETCH_THUMBS_KEY) > DataManager.FETCH_TIMEOUT_THUMBS) {
                File current = LocalImages.generateQuestionThumbFile(courseName, lessonName, id);
                File old = LocalImages.invalidateThumb(current);
                Questions.loadThumb(id.getItemIdValue(), scaleTo, current, exceptionHandler);
                boolean same = LocalImages.thumbsEqual(old, current);
                if(!old.delete()) throw new FileIOException(old, "Cannot delete");
                if(!same) {
                    LocalImages.deleteQuestionImage(courseName, lessonName, id);
                }
                DataManager.writeLastFetch(c, LAST_FETCH_THUMBS_KEY);
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
}
