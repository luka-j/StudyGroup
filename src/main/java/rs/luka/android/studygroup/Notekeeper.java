package rs.luka.android.studygroup;

import android.content.Context;

import rs.luka.android.studygroup.exceptions.UncaughtExceptionHandler;

/**
 * Created by luka on 8.1.17..
 */

public class Notekeeper extends android.app.Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        final Thread.UncaughtExceptionHandler def = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this, def));
    }

    public static Context getAppContext() {
        return appContext;
    }
}
