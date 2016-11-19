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
import rs.luka.android.studygroup.io.LocalAudio;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.io.database.NoteTable;
import rs.luka.android.studygroup.io.network.Notes;
import rs.luka.android.studygroup.misc.TextUtils;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;

import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.pushToExecutor;
import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.resetLastFetch;

/**
 * Created by luka on 17.10.16..
 */
public class NoteTasks {
    public static final int     LOADER_ID            = 2;
    private static final String TAG                  = "background.NoteTasks";
    static final String LAST_FETCH           = "lfNotes";
    private static final int    FETCH_TIMEOUT        = DataManager.FETCH_TIMEOUT_ITEMS; //5min
    static final String LAST_FETCH_THUMB_KEY = "lfNThumbs";

    public static void getNotes(final Context c, final long courseId, final String lesson,
                                final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        final long currentTime = System.currentTimeMillis();
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                if((currentTime - DataManager.getLastFetch(c, LAST_FETCH)) > FETCH_TIMEOUT) {
                    try {
                        Notes.getNotes(c, courseId, lesson, exceptionHandler);
                        DataManager.writeLastFetch(c, LAST_FETCH);
                        exceptionHandler.finished();
                    } catch (IOException e) {
                        exceptionHandler.handleIOException(e);
                    }
                }
                manager.initLoader(LOADER_ID, null, callbacks);
            }
        });
    }

    public static void refreshNotes(final Context c, final long courseId, final String lesson,
                                    final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                    final LoaderManager manager, final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Notes.getNotes(c, courseId, lesson, exceptionHandler);
                    DataManager.writeLastFetch(c, LAST_FETCH);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
                manager.restartLoader(LOADER_ID, null, callbacks);
            }
        });
    }

    public static void addNote(final Context c, final ID courseId, final String courseName, final String lesson,
                               final String text, final File image, final File audio, final boolean isPrivate,
                               final NetworkExceptionHandler handler) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    String realText = TextUtils.replaceEscapes(text);
                    String realLesson = TextUtils.replaceEscapes(lesson);
                    Long noteId = Notes.createNote(courseId.getCourseIdValue(), realLesson, realText,
                                                   handler, isPrivate ? Group.PERM_WRITE : Group.PERM_READ_CAN_REQUEST_WRITE);
                    if(noteId != null) {
                        ID id = new ID(courseId, noteId);
                        new NoteTable(c).insertNote(id, realLesson, realText, image != null, audio != null, 0);
                        if (image != null) {
                            Notes.updateImage(noteId, image, handler);
                            LocalImages.saveNoteImage(id, courseName, realLesson, image); //erases temp
                        }
                        if (audio != null) {
                            Notes.updateAudio(noteId, audio, handler);
                            LocalAudio.saveNoteAudio(id, courseName, realLesson, audio);
                        }
                        resetLastFetch(c, LessonTasks.LAST_FETCH_KEY);
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
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    String realText = TextUtils.replaceEscapes(text);
                    String realLesson = TextUtils.replaceEscapes(lesson);
                    boolean success = Notes.updateNote(id.getItemIdValue(), realLesson, realText, handler);
                    if(success) {
                        Course course = CourseTasks.getCourse(c, id);
                        new NoteTable(c).updateNote(id, realLesson, realText, imageFile!=null, audioFile!=null);
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
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    new NoteTable(c).reorderNote(id, lesson, newOrder, order);
                    Notes.reorderNote(id.getItemIdValue(), newOrder, handler);
                } catch (IOException ex) {
                    handler.handleIOException(ex);
                }
            }
        });
    }

    public static void hideNote(final Context c, final ID noteId, final String lesson,
                                final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    new NoteTable(c).removeNote(noteId, lesson);
                    boolean success = Notes.hideNote(noteId.getItemIdValue(), exceptionHandler);
                    exceptionHandler.finished();
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
            }
        });
    }

    public static void getNoteImage(final Context c, final ID id, final String courseName, final String lessonName,
                                    final int scaleTo, final NetworkExceptionHandler handler, final ImageView insertInto) {
        pushToExecutor(new Runnable() {
            @Override
            public void run() {
                if(scaleTo > DataManager.THUMB_THRESHOLD)
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
                DataManager.writeLastFetch(c, LAST_FETCH_THUMB_KEY);
            } else if(currentTime - DataManager.getLastFetch(c, LAST_FETCH_THUMB_KEY) > DataManager.FETCH_TIMEOUT_THUMBS) {
                File current = LocalImages.generateNoteThumbFile(courseName, lessonName, id);
                File old = LocalImages.invalidateThumb(current);
                Notes.loadThumb(id.getItemIdValue(), scaleTo, current, exceptionHandler);
                boolean same = LocalImages.thumbsEqual(old, current);
                if(old.exists() && !old.delete()) throw new FileIOException(old, "Cannot delete");
                if(!same) {
                    LocalImages.deleteNoteImage(courseName, lessonName, id);
                }
                DataManager.writeLastFetch(c, LAST_FETCH_THUMB_KEY);
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

    public static void getAudio(final int requestId, final ID noteId, final String courseName, final String lessonName,
                                final NetworkExceptionHandler exceptionHandler, final AudioCallbacks callbacks) {
        pushToExecutor(new Runnable() {
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

    public interface AudioCallbacks {
        void onAudioReady(int requestId, File audioFile);
    }
}
