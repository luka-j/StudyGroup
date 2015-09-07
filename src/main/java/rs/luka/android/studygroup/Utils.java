package rs.luka.android.studygroup;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.Collection;

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

    /**
     * Fuck you, lazily-implemented generics
     */
    public static String[] toStringArray(Collection list) {
        String[] array = new String[list.size()];
        int i=0;
        for(Object el : list) {
            array[i] = el.toString();
            i++;
        }
        return array;
    }

    public static byte[] shortToByteArray(short s) {
        return new byte[]{
                (byte) (s >>> 8),
                (byte) s
        };
    }

    public static byte[] intToByteArray(int i) {
        return new byte[]{
                (byte) (i >>> 3 * 8),
                (byte) (i >>> 2 * 8),
                (byte) (i >>> 8),
                (byte) i};
    }

    public static byte[] longToByteArray(long l) {
        return new byte[]{
                (byte) (l >>> 7 * 8),
                (byte) (l >>> 6 * 8),
                (byte) (l >>> 5 * 8),
                (byte) (l >>> 4 * 8),
                (byte) (l >>> 3 * 8),
                (byte) (l >>> 2 * 8),
                (byte) (l >>> 8),
                (byte) l};
    }

    public static short bytesToShort(byte hi, byte lo) {
        return (short) ((hi << 8) | lo);
    }

    public static int bytesToInt(byte hi1, byte hi2, byte lo2, byte lo1) {
        return hi1 << 3 * 8 | hi2 << 2 * 8 | lo2 << 8 | lo1;
    }

    public static long byteArrayToLong(byte[] b) {
        long l = 0;
        for (int i = 0; i < b.length; i++) {
            l |= (long) b[i] << (b.length - i) * 8;
        }
        return l;
    }
}
