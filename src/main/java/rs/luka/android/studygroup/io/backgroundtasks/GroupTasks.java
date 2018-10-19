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

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.io.database.GroupTable;
import rs.luka.android.studygroup.io.network.Groups;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;

import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.onUIThread;
import static rs.luka.android.studygroup.io.backgroundtasks.DataManager.pushToExecutor;

/**
 * Created by luka on 17.10.16..
 */
public class GroupTasks {
    public static final int     LOADER_ID            = 0;
    static final String LAST_FETCH_KEY       = "lfGroups";
    static final String LAST_FETCH_THUMB_KEY = "lfGThumb";
    private static final int    FETCH_TIMEOUT        = 1000 * 60 * 1; //1min, non-refreshable
    private static final String TAG                  = "background.GroupTasks";

    public static void loadGroups(final Context c, final NetworkExceptionHandler exceptionHandler, final GroupLoaderCallbacks callbacks) {
        final long currentTime = System.currentTimeMillis();
        pushToExecutor(() -> {
            if((currentTime - DataManager.getLastFetch(c, LAST_FETCH_KEY)) > FETCH_TIMEOUT ||
               new GroupTable(c).getGroupCount() == 0) {
                try {
                    Groups.getGroups(c, exceptionHandler);
                    exceptionHandler.finished();
                    DataManager.writeLastFetch(c, LAST_FETCH_KEY);
                } catch (IOException e) {
                    exceptionHandler.handleIOException(e);
                }
            }
            onUIThread(() -> callbacks.onGroupsLoaded(new GroupTable(c).queryGroups()));
        });
    }

    public static void refreshGroups(final Context c, final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                     final LoaderManager manager, final NetworkExceptionHandler handler) {
        pushToExecutor(() -> {
            try {
                Groups.getGroups(c, handler);
                handler.finished();
                DataManager.writeLastFetch(c, LAST_FETCH_KEY);
            } catch (IOException e) {
                handler.handleIOException(e);
            }
            onUIThread(() -> manager.restartLoader(LOADER_ID, null, callbacks));
        });
    }

    public static void addGroup(final Context c, final String name, final String place, final boolean inviteOnly,
                                final File image, final NetworkExceptionHandler handler) {
        pushToExecutor(() -> {
            try {
                Long groupId = Groups.createGroup(name, place, inviteOnly, handler);
                if(groupId != null) {
                    ID id = new ID(groupId);
                    if (image != null && image.exists()) {
                        Groups.updateImage(groupId, image, handler);
                        LocalImages.saveGroupImage(id, image);
                    }
                    new GroupTable(c).insertGroup(id, name, place, image != null, Group.PERM_OWNER);
                    handler.finished();
                } else {
                    Log.w(TAG, "network.Groups#createGroup returned null; exception should have been handled");
                }
            } catch (IOException ex) {
                handler.handleIOException(ex);
            }
        });
    }

    public static long getGroupCount(Context c) {
        return new GroupTable(c).getGroupCount();
    }

    public static void editGroup(final Context c, final ID id, final String name, final String place,
                                 final boolean inviteOnly, final File image,
                                 final NetworkExceptionHandler exceptionHandler) {
        pushToExecutor(() -> {
            try {
                boolean success = Groups.updateGroup(id.getGroupIdValue(), name, place, inviteOnly, exceptionHandler);
                if(success) {
                    new GroupTable(c).updateGroup(id, name, place, image != null);
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
        });
    }

    public static void getGroupImage(final Context c, final ID id, final int scaleTo,
                                     final NetworkExceptionHandler handler, final ImageView insertInto) {
        DataManager.executor.execute(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                boolean exists = LocalImages.groupImageExists(id);
                if(!exists || currentTime - DataManager.getLastFetch(c, LAST_FETCH_THUMB_KEY) > DataManager.FETCH_TIMEOUT_THUMBS) {
                    Groups.loadImage(id.getGroupIdValue(), scaleTo, LocalImages.generateGroupImageFile(id), handler);
                    DataManager.writeLastFetch(c, LAST_FETCH_THUMB_KEY);
                }
            } catch (IOException e) {
                handler.handleIOException(e);
            }
            try {
                final Bitmap image;
                image = LocalImages.getGroupImage(id, scaleTo);
                new Handler(Looper.getMainLooper()).post(() -> insertInto.setImageBitmap(image));
            } catch (IOException e) {
                handler.handleIOException(e);
            }
        });
    }

    public interface GroupLoaderCallbacks {
        void onGroupsLoaded(GroupTable.GroupCursor groups);
    }
}
