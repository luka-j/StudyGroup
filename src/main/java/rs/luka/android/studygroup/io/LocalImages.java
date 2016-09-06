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
    public static final File   APP_IMAGE_DIR = new File(Environment.getExternalStorageDirectory(),
                                                         "DCIM/StudyGroup/");
    public static final File APP_THUMBS_DIR = new File(APP_IMAGE_DIR, "thumbs/");
    private static final String TEMP_THUMB_NAME = "temp";
    private static final String TAG           = "studygroup.Images";

    protected static void saveGroupImage(ID id, File image) throws IOException {
        saveImage(image, generateGroupImageFile(id));
    }

    protected static Bitmap getGroupImage(ID id, int scaleTo) throws IOException {
        return loadImage(generateGroupImageFile(id), scaleTo);
    }

    protected static boolean groupImageExists(ID id) throws IOException {
        return generateGroupImageFile(id).exists();
    }

    protected static void deleteGroupImage(ID id) throws IOException {
        if(!generateGroupImageFile(id).delete()) throw new FileIOException(generateGroupImageFile(id), "Cannot delete");
    }

    protected static void saveCourseImage(ID id, File image) throws IOException {
        saveImage(image, generateCourseImageFile(id));
    }

    protected static Bitmap getCourseImage(ID id, int scaleTo) throws IOException {
        return loadImage(generateCourseImageFile(id), scaleTo);
    }

    protected static boolean courseImageExists(ID id) throws IOException {
        return generateCourseImageFile(id).exists();
    }

    protected static void deleteCourseImage(ID id) throws IOException {
        if(!generateCourseImageFile(id).delete()) throw new FileIOException(generateCourseImageFile(id), "Cannot delete");
    }

    protected static void saveNoteImage(ID id, String courseName, String lessonName, File image)
            throws FileIOException {
        saveImage(image, generateNoteImageFile(courseName, lessonName, id));
    }

    protected static Bitmap getNoteImage(String courseName, String lessonName, ID itemId, int scaleTo)
            throws IOException {
        return loadImage(generateNoteImageFile(courseName, lessonName, itemId), scaleTo);
    }

    protected static void deleteNoteImage(String courseName, String lessonName, ID id) throws IOException {
        File img = generateNoteImageFile(courseName, lessonName, id);
        if(img.exists() && !img.delete()) throw new FileIOException(img, "Cannot delete");
    }

    protected static Bitmap getNoteThumb(String courseName, String lessonName, ID itemId, int scaleTo)
        throws IOException {
        return loadImage(generateNoteThumbFile(courseName, lessonName, itemId), scaleTo);
    }

    public static String getNoteImagePath(String courseName, String lessonName, ID itemId) throws FileIOException {
        return generateNoteImageFile(courseName, lessonName, itemId).getAbsolutePath();
    }

    public static boolean noteImageExists(String courseName, String lessonName, ID itemId) throws FileIOException {
        return generateNoteImageFile(courseName, lessonName, itemId).exists();
    }

    public static boolean noteThumbExists(String courseName, String lessonName, ID itemId) throws IOException {
        return generateNoteThumbFile(courseName, lessonName, itemId).exists();
    }

    protected static void saveQuestionImage(ID id, String courseName, String lessonName, File image)
            throws FileIOException {
        saveImage(image, generateQuestionImageFile(courseName, lessonName, id));
    }

    protected static Bitmap getQuestionImage(String courseName, String lessonName, ID itemId, int scaleTo)
            throws IOException {
        return loadImage(generateQuestionImageFile(courseName, lessonName, itemId), scaleTo);
    }

    protected static void deleteQuestionImage(String courseName, String lessonName, ID id) throws IOException {
        File img = generateQuestionImageFile(courseName, lessonName, id);
        if(!img.delete()) throw new FileIOException(img, "Cannot delete");
    }

    protected static Bitmap getQuestionThumb(String courseName, String lessonName, ID itemId, int scaleTo)
            throws IOException {
        return loadImage(generateQuestionThumbFile(courseName, lessonName, itemId), scaleTo);
    }

    public static String getQuestionImagePath(String courseName, String lessonName, ID itemId) throws FileIOException {
        return generateQuestionImageFile(courseName, lessonName, itemId).getAbsolutePath();
    }

    public static boolean questionImageExists(String courseName, String lessonName, ID itemId) throws FileIOException {
        return generateQuestionImageFile(courseName, lessonName, itemId).exists();
    }

    public static boolean questionThumbExists(String courseName, String lessonName, ID itemId) throws IOException {
        return generateQuestionThumbFile(courseName, lessonName, itemId).exists();
    }

    public static boolean userThumbExists(long userId) throws IOException {
        return generateUserThumbFile(userId).exists();
    }

    public static Bitmap getUserThumb(long userId, int scaleTo) throws IOException {
        return loadImage(generateUserThumbFile(userId), scaleTo);
    }


    public static boolean thumbsEqual(File f1, File f2) {
        if(f1.length() != f2.length() || f1.length() == 0 || f2.length() == 0) return false;

        BitmapFactory.Options opts1 = new BitmapFactory.Options();
        BitmapFactory.Options opts2 = new BitmapFactory.Options();
        opts1.inJustDecodeBounds = true;
        opts2.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f1.getAbsolutePath(), opts1);
        BitmapFactory.decodeFile(f2.getAbsolutePath(), opts2);
        if(opts1.outHeight != opts2.outHeight || opts1.outWidth != opts2.outWidth)
            return false;

        opts1.inJustDecodeBounds = false; opts2.inJustDecodeBounds = false;
        opts1.inPreferQualityOverSpeed = false; opts2.inPreferQualityOverSpeed = false;
        opts1.inSampleSize = 4; opts2.inSampleSize = 4;
        opts1.inPreferredConfig = Bitmap.Config.RGB_565; opts2.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap b1 = BitmapFactory.decodeFile(f1.getAbsolutePath(), opts1);
        Bitmap b2 = BitmapFactory.decodeFile(f2.getAbsolutePath(), opts2);
        return b1.sameAs(b2);
    }

    private static void createThumbsDir() throws IOException {
        if (!APP_THUMBS_DIR.isDirectory()) {
            if(!APP_THUMBS_DIR.mkdirs()) throw new FileIOException(APP_THUMBS_DIR, "Cannot create directory");
            new File(APP_THUMBS_DIR, ".nomedia").createNewFile();
        }
    }

    protected static File createCourseDir(String courseName) throws FileIOException {
        File courseDir = new File(APP_IMAGE_DIR, courseName);
        if (!courseDir.isDirectory()) {
            if(!courseDir.mkdirs()) throw new FileIOException(APP_IMAGE_DIR, "Cannot create directory");
        }
        return courseDir;
    }

    private static File createCourseThumbsDir(String courseName) throws IOException {
        File courseDir = new File(APP_THUMBS_DIR, courseName);
        if (!courseDir.isDirectory()) {
            if(!courseDir.mkdirs()) throw new FileIOException(APP_THUMBS_DIR, "Cannot create directory");
            new File(courseDir, ".nomedia").createNewFile();
        }
        return courseDir;
    }

    protected static File generateGroupImageFile(ID id) throws IOException {
        createThumbsDir();
        return new File(APP_THUMBS_DIR,
                        "IMG_Group-" + id.getGroupIdValue()
                                              + ".jpg"); // TODO: 19.9.15. support i za druge formate sem jpg
    }

    protected static File generateCourseImageFile(ID id) throws IOException {
        createThumbsDir();
        return new File(APP_THUMBS_DIR, "IMG_Course-" + id.getCourseIdValue() + ".jpg");
    }

    protected static File generateNoteImageFile(String courseName, String lessonName, ID itemId)
            throws FileIOException {
        File courseDir = createCourseDir(courseName);
        return new File(courseDir, lessonName + "-NIMG_" + itemId.getItemIdValue() + ".jpg");
    }

    protected static File generateQuestionImageFile(String courseName, String lessonName, ID itemId)
            throws FileIOException {
        File courseDir = createCourseDir(courseName);
        return new File(courseDir, lessonName + "-QIMG_" + itemId.getItemIdValue() + ".jpg");
    }

    protected static File generateNoteThumbFile(String courseName, String lessonName, ID itemId)
            throws IOException {
        File courseDir = createCourseThumbsDir(courseName);
        return new File(courseDir, lessonName + "-nthumb_" + itemId.getItemIdValue() + ".jpg");
    }

    protected static File generateQuestionThumbFile(String courseName, String lessonName, ID itemId)
            throws IOException {
        File courseDir = createCourseThumbsDir(courseName);
        return new File(courseDir, lessonName + "-qthumb_" + itemId.getItemIdValue() + ".jpg");
    }

    protected static File generateUserThumbFile(long userId) throws IOException {
        createThumbsDir();
        return new File(APP_THUMBS_DIR, "IMG_User-" + userId);
    }

    protected static File generateMyImageFile() throws IOException {
        if(!APP_IMAGE_DIR.isDirectory()) APP_IMAGE_DIR.mkdirs();
        return new File(APP_IMAGE_DIR, "myImage");
    }

    protected static File invalidateThumb(File filename) throws IOException {
        File old = new File(filename.getParentFile(), "old");
        if(!filename.renameTo(old)) throw new FileIOException(old, "Cannot rename");
        return old;
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
