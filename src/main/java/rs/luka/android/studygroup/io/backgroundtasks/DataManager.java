package rs.luka.android.studygroup.io.backgroundtasks;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by luka on 18.8.15..
 */
public class DataManager {

    private static final String PREFS_NAME = "fetchHistory";

    static final int FETCH_TIMEOUT_THUMBS = 1000 * 60 * 15; //15min
    static final int FETCH_TIMEOUT_ITEMS  = 1000 * 60 * 5; //5min

    static final int THUMB_THRESHOLD = 250;

    static ThreadPoolExecutor executor;

    static {
        executor = new ThreadPoolExecutor(6, 6, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
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

    static void pushToExecutor(Runnable r) {
        executor.execute(r);
    }
}
