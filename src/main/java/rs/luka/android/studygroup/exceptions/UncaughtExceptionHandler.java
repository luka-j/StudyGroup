package rs.luka.android.studygroup.exceptions;

import android.content.Context;
import android.util.Log;

/**
 * Created by luka on 8.1.17..
 */

public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context                         context;
    private final Thread.UncaughtExceptionHandler notifyHandler;

    public UncaughtExceptionHandler(Context context) {
        this(context, null);
    }
    public UncaughtExceptionHandler(Context context, Thread.UncaughtExceptionHandler notifyHandler) {
        this.context = context;
        this.notifyHandler = notifyHandler;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e("UncaughtExceptionHandlr", "Encountered uncaught exception in thread " + thread.getName(), ex);
        //todo notify server
        if(notifyHandler != null) notifyHandler.uncaughtException(thread, ex);
    }
}
