package rs.luka.android.studygroup.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.exceptions.FileIOException;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 12.9.15..
 */
public class LocalImages {
    public static final  File   APP_IMAGE_DIR = new File(Environment.getExternalStorageDirectory(),
                                                         "DCIM/StudyGroup/");
    public static final File APP_THUMBS_DIR = new File(APP_IMAGE_DIR, "thumbs/");
    private static final String TAG           = "studygroup.Images";

    private static final int THUMB_THRESHOLD = 350;

    protected static void saveGroupImage(ID id, String name, File image) throws IOException {
        saveImage(image, generateGroupImageFile(name, id));
    }

    protected static Bitmap getGroupImage(ID id, String name, int scaleTo) throws IOException {
        return loadImage(generateGroupImageFile(name, id), scaleTo);
    }

    protected static void saveCourseImage(ID id, String name, File image) throws IOException {
        saveImage(image, generateCourseImageFile(name, id));
    }

    protected static Bitmap getCourseImage(ID id, String name, int scaleTo) throws IOException {
        return loadImage(generateCourseImageFile(name, id), scaleTo);
    }
    protected static void saveItemImage(ID id, String courseName, String lessonName, File image)
            throws FileIOException {
        saveImage(image, generateItemImageFile(courseName, lessonName, id));
    }

    protected static Bitmap getItemImage(String courseName, String lessonName, ID itemId, int scaleTo)
            throws FileIOException {
        return loadImage(scaleTo > THUMB_THRESHOLD ?
                         generateItemImageFile(courseName, lessonName, itemId) :
                         generateItemThumbFile(courseName, lessonName, itemId), scaleTo);
    }

    public static String getItemImagePath(String courseName, String lessonName, ID itemId) throws FileIOException {
        return generateItemImageFile(courseName, lessonName, itemId).getAbsolutePath();
    }

    protected static File generateGroupImageFile(String groupName, ID id) throws IOException {
        if (!APP_THUMBS_DIR.isDirectory()) {
            if(!APP_THUMBS_DIR.mkdirs()) throw new FileIOException(APP_THUMBS_DIR, "Cannot create directory");
            new File(APP_THUMBS_DIR, ".nomedia").createNewFile();
        }
        return new File(APP_THUMBS_DIR,
                        groupName + "-IMG_" + id.toString()
                        + ".jpg"); // TODO: 19.9.15. support i za druge formate sem jpg
    }

    protected static File generateCourseImageFile(String courseName, ID id) throws IOException {
        if (!APP_THUMBS_DIR.isDirectory()) {
            if(!APP_THUMBS_DIR.mkdirs()) throw new IOException("Cannot create thumbs dir " + APP_THUMBS_DIR.getAbsolutePath());
            new File(APP_THUMBS_DIR, ".nomedia").createNewFile();
        }
        return new File(APP_THUMBS_DIR, courseName + "-IMG_" + id.toString() + ".jpg");
    }

    protected static File generateItemImageFile(String courseName, String lessonName, ID itemId)
            throws FileIOException {
        File courseDir = new File(APP_IMAGE_DIR, courseName);
        if (!courseDir.isDirectory()) {
            if(!courseDir.mkdirs()) throw new FileIOException(APP_THUMBS_DIR, "Cannot create directory");
        }
        return new File(courseDir, lessonName + "-IMG_" + itemId.toString() + ".jpg");
    }

    protected static File generateItemThumbFile(String courseName, String lessonName, ID itemId)
            throws FileIOException {
        File courseDir = new File(APP_THUMBS_DIR, courseName);
        if (!courseDir.isDirectory()) {
            if(!APP_THUMBS_DIR.mkdirs()) throw new FileIOException(APP_THUMBS_DIR, "Cannot create directory");
        }
        return new File(courseDir, lessonName + "-thumb_" + itemId.toString() + ".jpg");
    }

    public static Bitmap loadImage(File imageFile, int scaleTo) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        if (scaleTo > 0) {
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opts);
            opts.inJustDecodeBounds = false;
            int larger = opts.outHeight > opts.outWidth ? opts.outHeight : opts.outWidth;
            opts.inSampleSize = larger / scaleTo;
        }
        opts.inPreferQualityOverSpeed = true;
        return BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opts);
    }

    private static void saveImage(File oldImage, File newImage) {
        if (oldImage.getName()
                    .endsWith("temp")) { // TODO: 10.9.15. minor fix (i drugi fajlovi mogu da se završavaju na temp)
            boolean renameSuccess = oldImage.renameTo(newImage);
            if (!renameSuccess) {
                Log.e(TAG,
                      "Rename failed. Planned img name: " + newImage.getAbsolutePath());
            }
        } else {
            try {
                Utils.copyFile(oldImage, newImage);
            } catch (IOException e) {
                Log.e(TAG, "File copy failed.", e);
            }
        }
    }
}
