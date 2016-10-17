package rs.luka.android.studygroup.io.backgroundtasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.network.UserManager;

/**
 * Created by luka on 17.10.16..
 */
public class UserTasks {
    private static final String LAST_FETCH_THUMB_KEY = "lfUThumb";

    public static void getUserImage(final Context c, final long id, final int scaleTo,
                                    final NetworkExceptionHandler handler, final ImageView insertInto) {
        DataManager.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    long currentTime = System.currentTimeMillis();
                    boolean exists = LocalImages.userThumbExists(id);
                    if(!exists || currentTime - DataManager.getLastFetch(c, LAST_FETCH_THUMB_KEY) > DataManager.FETCH_TIMEOUT_THUMBS) {
                        UserManager.getImage(id, scaleTo, LocalImages.generateUserThumbFile(id), handler);
                        DataManager.writeLastFetch(c, LAST_FETCH_THUMB_KEY);
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

    public static void setMyProfile(final String username, final String email, final File image,
                                    final NetworkExceptionHandler exceptionHandler) {
        DataManager.executor.execute(new Runnable() {
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
}
