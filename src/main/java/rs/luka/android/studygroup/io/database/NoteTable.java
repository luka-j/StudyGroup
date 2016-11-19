package rs.luka.android.studygroup.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.Note;

import static rs.luka.android.studygroup.io.database.Database.COMMA_SEP;
import static rs.luka.android.studygroup.io.database.Database.CREATE;
import static rs.luka.android.studygroup.io.database.Database.DROP;
import static rs.luka.android.studygroup.io.database.Database.INSERT;
import static rs.luka.android.studygroup.io.database.Database.PRIMARY;
import static rs.luka.android.studygroup.io.database.Database.REF;
import static rs.luka.android.studygroup.io.database.Database.TYPE_ID;
import static rs.luka.android.studygroup.io.database.Database.TYPE_INT1;
import static rs.luka.android.studygroup.io.database.Database.TYPE_INT4;
import static rs.luka.android.studygroup.io.database.Database.TYPE_INT8;
import static rs.luka.android.studygroup.io.database.Database.TYPE_NONNULL;
import static rs.luka.android.studygroup.io.database.Database.TYPE_TEXT;
import static rs.luka.android.studygroup.io.database.Database.TYPE_VARCHAR;
import static rs.luka.android.studygroup.io.database.Database.VALS;

/**
 * Created by luka on 16.10.16..
 */
public class NoteTable {
    static final String TABLE_NAME       = "notes";
    static final String SQL_CREATE_TABLE =
            CREATE + TABLE_NAME + " (" +
            //NoteEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
            NoteEntry.COLUMN_NAME_ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
            NoteEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + GroupTable.TABLE_NAME + "("
            + GroupTable.GroupEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
            NoteEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + CourseTable.TABLE_NAME + "("
            + CourseTable.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
            NoteEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.LESSON_MAX_LENGTH + ")"
            + TYPE_NONNULL + COMMA_SEP +
            NoteEntry.COLUMN_NAME_TEXT + TYPE_TEXT + COMMA_SEP +
            NoteEntry.COLUMN_NAME_IMAGE + TYPE_INT1 + COMMA_SEP +
            NoteEntry.COLUMN_NAME_AUDIO + TYPE_INT1 + COMMA_SEP +
            NoteEntry.COLUMN_NAME_ORDER + TYPE_INT4 +
            ")";

    static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;
    /**
     * Order: id, groupId, courseId, lesson, text, image, audio
     */
    private static final String SQL_INSERT = INSERT + TABLE_NAME + " (" +
                                             NoteEntry.COLUMN_NAME_ID + COMMA_SEP +
                                             NoteEntry.COLUMN_NAME_GROUP_ID + COMMA_SEP +
                                             NoteEntry.COLUMN_NAME_COURSE_ID + COMMA_SEP +
                                             NoteEntry.COLUMN_NAME_LESSON + COMMA_SEP +
                                             NoteEntry.COLUMN_NAME_TEXT + COMMA_SEP +
                                             NoteEntry.COLUMN_NAME_IMAGE + COMMA_SEP +
                                             NoteEntry.COLUMN_NAME_AUDIO  + COMMA_SEP +
                                             NoteEntry.COLUMN_NAME_ORDER +
                                             ")" + VALS + "(?, ?, ?, ?, ?, ?, ?, ?)";

    public static abstract class NoteEntry implements BaseColumns {
        public static final String COLUMN_NAME_ID        = _ID;
        public static final String COLUMN_NAME_GROUP_ID  = "group_id";
        public static final String COLUMN_NAME_COURSE_ID = "course_id";
        public static final String COLUMN_NAME_LESSON    = "lesson";
        public static final String COLUMN_NAME_TEXT      = "text";
        public static final String COLUMN_NAME_IMAGE     = "image_exists";
        public static final String COLUMN_NAME_AUDIO     = "audio_exists";
        public static final String COLUMN_NAME_ORDER     = "order_col";
    }

    public static class NoteCursor extends CursorWrapper {
        public NoteCursor(Cursor cursor) {
            super(cursor);
        }

        public Note getNote() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            ID id = new ID(getLong(getColumnIndex(NoteEntry.COLUMN_NAME_GROUP_ID)),
                           getLong(getColumnIndex(NoteEntry.COLUMN_NAME_COURSE_ID)),
                           getLong(getColumnIndex(NoteEntry.COLUMN_NAME_ID)));
            return new Note(id,
                            getString(getColumnIndex(NoteEntry.COLUMN_NAME_LESSON)),
                            getString(getColumnIndex(NoteEntry.COLUMN_NAME_TEXT)),
                            getInt(getColumnIndex(NoteEntry.COLUMN_NAME_IMAGE)) != 0,
                            getInt(getColumnIndex(NoteEntry.COLUMN_NAME_AUDIO)) != 0,
                            getInt(getColumnIndex(NoteEntry.COLUMN_NAME_ORDER)));
        }
    }


    private Database helper;
    public NoteTable(Context c) {
        helper = Database.getInstance(c);
    }

    public void insertNote(ID id, String lesson, String text, boolean hasImage, boolean hasAudio, long order) {
        ContentValues cv = new ContentValues(5);
        cv.put(NoteEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(NoteEntry.COLUMN_NAME_COURSE_ID, id.getCourseIdValue());
        cv.put(NoteEntry.COLUMN_NAME_ID, id.getItemIdValue());
        cv.put(NoteEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(NoteEntry.COLUMN_NAME_TEXT, text);
        cv.put(NoteEntry.COLUMN_NAME_IMAGE, hasImage);
        cv.put(NoteEntry.COLUMN_NAME_AUDIO, hasAudio);
        if(order != 0)
            cv.put(NoteEntry.COLUMN_NAME_ORDER, order);
        else
            cv.put(NoteEntry.COLUMN_NAME_ORDER, Integer.MAX_VALUE);
        SQLiteDatabase db   = helper.getWritableDatabase();
        long           code = db.insert(TABLE_NAME, null, cv);
    }

    public void updateNote(ID id, String lesson, String text, boolean hasImage, boolean hasAudio) {
        ContentValues cv = new ContentValues(4);
        cv.put(NoteEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(NoteEntry.COLUMN_NAME_TEXT, text);
        cv.put(NoteEntry.COLUMN_NAME_IMAGE, hasImage);
        cv.put(NoteEntry.COLUMN_NAME_AUDIO, hasAudio);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.update(TABLE_NAME, cv, NoteEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(), null);
    }

    public void reorderNote(ID id, String lesson, int newPosition, int currentPosition) { //todo fix occasional off-by-1
        SQLiteDatabase db = helper.getWritableDatabase();
        if(newPosition < currentPosition) {
            db.rawQuery("UPDATE " + TABLE_NAME + " SET " +
                        NoteEntry.COLUMN_NAME_ORDER + " = " + NoteEntry.COLUMN_NAME_ORDER + "+1 "
                        + "WHERE " + NoteEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() +
                        " AND " + NoteEntry.COLUMN_NAME_LESSON + "='" + lesson + "' AND " +
                        NoteEntry.COLUMN_NAME_ORDER + " BETWEEN " + currentPosition + " AND " + newPosition,
                        null);
        } else if(newPosition > currentPosition) {
            db.rawQuery("UPDATE " + TABLE_NAME + " SET " +
                        NoteEntry.COLUMN_NAME_ORDER + " = " + NoteEntry.COLUMN_NAME_ORDER + "-1 "
                        + "WHERE " + NoteEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() +
                        " AND " + NoteEntry.COLUMN_NAME_LESSON + "='" + lesson + "' AND " +
                        NoteEntry.COLUMN_NAME_ORDER + " BETWEEN " + currentPosition + " AND " + newPosition,
                        null);
        }
        ContentValues cv = new ContentValues(1);
        cv.put(NoteEntry.COLUMN_NAME_ORDER, newPosition);
        db.update(TABLE_NAME, cv, NoteEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(), null);
    }

    public void removeNote(ID id, String lesson) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long code = db.delete(TABLE_NAME,
                              NoteEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                              null);
    }

    public void clearNotes(long courseId, String lesson) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_NAME,
                  NoteEntry.COLUMN_NAME_COURSE_ID + "=" + courseId + " AND " +
                  NoteEntry.COLUMN_NAME_LESSON + "='" + lesson + "'",
                  null);
    }

    public void insertNotes(Note[] notes) {
        SQLiteDatabase  db   = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(SQL_INSERT);
        db.beginTransaction();

        for (Note note : notes) {
            stmt.bindLong(1, note.getIdValue());      //id
            stmt.bindLong(2, note.getGroupIdValue()); //group id
            stmt.bindLong(3, note.getCourseIdValue());//courseId
            stmt.bindString(4, note.getLesson());     //lesson
            stmt.bindString(5, note.getText());       //text
            stmt.bindLong(6, note.hasImage()?1:0);    //hasImage
            stmt.bindLong(7, note.hasAudio()?1:0);    //hasAudio
            stmt.bindLong(8, note.getOrder());
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public NoteCursor queryNotes(ID courseId, String lesson) {
        if (lesson.isEmpty()) { return null; }
        SQLiteDatabase db = helper.getReadableDatabase();
        NoteCursor c = new NoteCursor(db.query(TABLE_NAME,
                                               null,
                                               NoteEntry.COLUMN_NAME_COURSE_ID
                                               + "=" + courseId.getCourseIdValue() +
                                               " AND " + NoteEntry.COLUMN_NAME_LESSON
                                               + "='" + lesson + "'",
                                               null,
                                               null,
                                               null,
                                               NoteEntry.COLUMN_NAME_ORDER + " asc"));
        return c;
    }
}
