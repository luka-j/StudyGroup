package rs.luka.android.studygroup;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by luka on 16.7.15..
 */
public class Utils {


    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Integer intPrimitiveToObj(int val, int invalidValue) {
        return val == invalidValue ? null : val;
    }

    public static int integerObjToPrimitive(Integer val, int invalidValue) {
        return val == null ? invalidValue : val;
    }
}
