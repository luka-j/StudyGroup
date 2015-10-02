package rs.luka.android.studygroup.io;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;

import rs.luka.android.studygroup.model.History;
import rs.luka.android.studygroup.model.User;

/**
 * Created by luka on 18.8.15..
 */
public class Network {

    public static class UserCursor extends CursorWrapper {
        private static final String COLUMN_ID       = "id";
        private static final String COLUMN_USERNAME = "username";

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        public UserCursor(Cursor cursor) {
            super(cursor);
        }

        public User getUser() {
            return new User(getLong(getColumnIndex(COLUMN_ID)), getString(getColumnIndex(COLUMN_USERNAME)));
        }
    }

    public static class HistoryCursor extends CursorWrapper {
        public static final String COLUMN_USER   = "user";
        public static final String COLUMN_TIME   = "time";
        public static final String COLUMN_ACTION = "action";

        /**
         * Creates a cursor wrapper.
         *
         * @param cursor The underlying cursor to wrap.
         */
        public HistoryCursor(Cursor cursor) {
            super(cursor);
        }

        public History getHistory() {
            return new History(getString(getColumnIndex(COLUMN_USER)),
                               new Date(getLong(getColumnIndex(COLUMN_TIME))),
                               getString(getColumnIndex(COLUMN_ACTION)));
        }
    }
}
