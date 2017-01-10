package rs.luka.android.studygroup.misc;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by luka on 16.7.15..
 */
public class Utils {
    private static final String TAG = "studygroup.Utils";

    private static final int COPY_BUFFER_SIZE = 1024 * 50; //50kB

    public static String getRealPathFromUri(Context c, Uri uri) {
        return FilePathUtils.getPath(c, uri);
    }


    public static Integer intPrimitiveToObj(int val, int invalidValue) {
        return val == invalidValue ? null : val;
    }

    public static int integerObjToPrimitive(Integer val, int invalidValue) {
        return val == null ? invalidValue : val;
    }

    public static int getTransparentColor(int color, int transparency) {
        int transparencyMask = 255 * (transparency)/100;
        int fullyTransparent = color & 0x00ffffff;
        return fullyTransparent | (transparencyMask << 24);
    }

    /**
     * Used for hiding keyboard
     */
    public static void simulateBackButton() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(700); //because it doesn't pop up immediately
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            }
        }).start();
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

    public static void copyFile(File src, File dst) throws IOException {
        InputStream  in  = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        byte[] buf = new byte[COPY_BUFFER_SIZE];
        int    len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String removeSpaces(String str) {
        boolean       camelCase = false;
        StringBuilder newStr    = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ' ') {
                camelCase = true;
            } else {
                newStr.append(camelCase ? Character.toUpperCase(str.charAt(i)) : str.charAt(i));
                camelCase = false;
            }
        }
        return newStr.toString();
    }

    public static InputStream wrapStream(String contentEncoding, InputStream inputStream)
            throws IOException {
        if (contentEncoding == null || "identity".equalsIgnoreCase(contentEncoding)) {
            return inputStream;
        }
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            return new GZIPInputStream(inputStream);
        }
        if ("deflate".equalsIgnoreCase(contentEncoding)) {
            return new InflaterInputStream(inputStream, new Inflater(false), 512);
        }
        Log.e(TAG, "Unknown contentEncoding " + contentEncoding);
        return inputStream;
    }

    @NonNull
    public static String listToString(List<Integer> list) {
        if(list.isEmpty()) return "";
        StringBuilder str = new StringBuilder((int)(list.size()*2.5));
        for(Object o : list) {
            str.append(o).append(",");
        }
        return str.deleteCharAt(str.length()-1).toString();
    }

    @NonNull
    public static List<Integer> stringToList(String str) {
        if(str == null) return new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(str, ",", false);
        List<Integer> list = new ArrayList<>((int)(str.length()/2.5));
        while(tokenizer.hasMoreTokens()) {
            String next = tokenizer.nextToken();
            if(next.equals("null"))
                list.add(null);
            else
                list.add(Integer.parseInt(next));
        }
        return list;
    }

    public static Integer[] intToIntegerArray(int[] arr) {
        Integer[] ret = new Integer[arr.length];
        for(int i=0; i<arr.length; i++)
            ret[i] = arr[i];
        return ret;
    }

    public static int[] integerToIntArray(Object[] arr) {
        int[] ret = new int[arr.length];
        for(int i=0; i<arr.length; i++)
            ret[i] = (Integer)arr[i];
        return ret;
    }

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private static final int[] GRADE_COLORS = {0xff81C784, //green 300       (1)
                                               0xffCDDC39, //lime 500        (2)
                                               0xff9575CD, //deep purple 300 (3)
                                               0xffE57373, //red 300         (4)
                                               0xff4DD0E1, //cyan 300        (5)
                                               0xffFFD54F, //amber 300       (6)
                                               0xff7986CB, //indigo 300      (7)
                                               0xffA1887F, //brown 300       (8)
                                               0xff4DB6AC, //teal 300        (9)
                                               0xffF06292, //pink 300        (10)
                                               0xffBA68C8, //purple 300      (11)
                                               0xff90A4AE  //blue grey 300   (12)
    };
    public static Bitmap generateBitmapFor(int grade, int width, int height) {
        Bitmap        newImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int color = 0xFF666666;
        if(grade>0 && grade<=GRADE_COLORS.length) color = GRADE_COLORS[grade-1];

        Canvas canvas = new Canvas(newImage);
        int textSize = height*3/4;

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(textSize);
        paint.setFakeBoldText(true);
        int xPos = (canvas.getWidth() / 2 - textSize / 3 * (grade > 9 ? 2 : 1));
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;
        canvas.drawText(String.valueOf(grade), xPos, yPos, paint);

        return newImage;
    }
}
