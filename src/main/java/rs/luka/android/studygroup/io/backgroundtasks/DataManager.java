package rs.luka.android.studygroup.io.backgroundtasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by luka on 18.8.15..
 */
public class DataManager {
    public static final int EXECUTOR_THREADS = 3;

    private static final String TAG = "DataManager";
    private static final String PREFS_NAME = "fetchHistory";

    static final int FETCH_TIMEOUT_THUMBS = 1000 * 60 * 15; //15min
    static final int FETCH_TIMEOUT_ITEMS  = 1000 * 60 * 3; //3min

    static final int THUMB_THRESHOLD = 250;

    static ThreadPoolExecutor executor;

    static {
        executor = new ThreadPoolExecutor(EXECUTOR_THREADS, EXECUTOR_THREADS, 60, TimeUnit.SECONDS,
                                          new LinkedBlockingQueue<>());
        executor.allowCoreThreadTimeOut(true);
    }

    static long getLastFetch(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(key, 0);
    }

    static void writeLastFetch(Context context, String key) {
        SharedPreferences        prefs       = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor      = prefs.edit();
        long                     currentTime = System.currentTimeMillis();
        editor.putLong(key, currentTime);
        editor.apply();
    }

    static void resetLastFetch(Context context, String key) {
        SharedPreferences        prefs       = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor      = prefs.edit();
        editor.remove(key);
        editor.apply();
    }

    static long getLastFetchTagged(Context context, String key, long itemId) {
        return getLastFetch(context, key+"/"+itemId);
    }
    static void writeLastFetchTagged(Context context, String key, long itemId) {
        writeLastFetch(context, key);
        writeLastFetch(context, key+"/"+itemId);
    }
    static long getLastFetchTagged(Context context, String key, String tag) {
        return getLastFetch(context, key+"/"+tag);
    }
    static void writeLastFetchTagged(Context context, String key, String tag) {
        writeLastFetch(context, key);
        writeLastFetch(context, key+"/"+tag);
    }
    static void resetLastFetchTagged(Context context, String key, long itemId) {
        resetLastFetch(context, key);
        resetLastFetch(context, key+"/"+itemId);
    }

    public static void clearFetchHistory(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }

    //todo fix shitty workaround
    static void pushToExecutor(final Runnable r) {
        executor.execute(() -> {
            try {
                r.run();
            } catch (IllegalStateException e) {
                Log.w(TAG, "got illegalstate, retrying"); //timing issue/race condition (probably)
                try {
                    Thread.sleep(500);
                    r.run(); //call recursively? enough evil for today
                } catch (IllegalStateException e2) {
                    Log.e(TAG, "got illegalstate twice, ignoring", e2); //doesn't happen (for now)
                } catch (InterruptedException e1) {
                    e1.printStackTrace(); //doesn't happen (really)
                }
            }
        });
    }

    private static Handler handler = new Handler(Looper.getMainLooper());
    static void onUIThread(final Runnable r) {
        handler.post(r);
    }
}
