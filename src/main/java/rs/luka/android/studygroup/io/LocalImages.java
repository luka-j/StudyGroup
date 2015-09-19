package rs.luka.android.studygroup.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 12.9.15..
 */
public class LocalImages {
    public static final  File   APP_IMAGE_DIR = new File(Environment.getExternalStorageDirectory(),
                                                         "/DCIM/StudyGroup/");
    private static final String TAG           = "studygroup.Images";

    protected static void saveGroupImage(ID id, String name, File image) {
        saveImage(image, generateGroupImageFile(name, id));
    }

    protected static Bitmap getGroupImage(ID id, String name, int scaleTo) {
        return loadImage(generateGroupImageFile(name, id), scaleTo);
    }

    protected static boolean groupHasImage(ID id, String name) {
        return generateGroupImageFile(name, id).exists();
    }

    protected static void saveCourseImage(ID id, String name, File image) {
        saveImage(image, generateCourseImageFile(name, id));
    }

    protected static Bitmap getCourseImage(ID id, String name, int scaleTo) {
        return loadImage(generateCourseImageFile(name, id), scaleTo);
    }

    protected static boolean courseHasImage(ID id, String name) {
        return generateCourseImageFile(name, id).exists();
    }

    protected static void saveItemImage(ID id, String courseName, String lessonName, File image) {
        saveImage(image, generateItemImageFile(courseName, lessonName, id));
    }

    protected static Bitmap getItemImage(String courseName, String lessonName, ID itemId, int scaleTo) {
        return loadImage(generateItemImageFile(courseName, lessonName, itemId), scaleTo);
    }

    public static String getItemImagePath(String courseName, String lessonName, ID itemId) {
        return generateItemImageFile(courseName, lessonName, itemId).getAbsolutePath();
    }

    protected static boolean itemHasImage(String courseName, String lessonName, ID itemId) {
        return generateItemImageFile(courseName, lessonName, itemId).exists();
    }

    private static File generateGroupImageFile(String groupName, ID id) {
        if (!APP_IMAGE_DIR.isDirectory()) APP_IMAGE_DIR.mkdir();
        return new File(APP_IMAGE_DIR,
                        groupName + "-IMG" + id.toString()
                        + ".jpg"); // TODO: 19.9.15. support i za druge formate sem jpg
    }

    private static File generateCourseImageFile(String courseName, ID id) {
        if (!APP_IMAGE_DIR.isDirectory()) APP_IMAGE_DIR.mkdir();
        return new File(APP_IMAGE_DIR, courseName + "-IMG" + id.toString() + ".jpg");
    }

    private static File generateItemImageFile(String courseName, String lessonName, ID itemId) {
        if (!APP_IMAGE_DIR.isDirectory()) APP_IMAGE_DIR.mkdir();
        File courseDir = new File(APP_IMAGE_DIR, courseName);
        if (!courseDir.isDirectory()) courseDir.mkdir();
        return new File(courseDir, lessonName + "-IMG_" + itemId.toString() + ".jpg");
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
