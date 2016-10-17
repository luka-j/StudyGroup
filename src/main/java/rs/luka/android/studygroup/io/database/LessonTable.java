package rs.luka.android.studygroup.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.model.ID;

import static rs.luka.android.studygroup.io.database.Database.COMMA_SEP;
import static rs.luka.android.studygroup.io.database.Database.CREATE;
import static rs.luka.android.studygroup.io.database.Database.DROP;
import static rs.luka.android.studygroup.io.database.Database.INSERT;
import static rs.luka.android.studygroup.io.database.Database.PRIMARY;
import static rs.luka.android.studygroup.io.database.Database.REF;
import static rs.luka.android.studygroup.io.database.Database.TYPE_ID;
import static rs.luka.android.studygroup.io.database.Database.TYPE_INT4;
import static rs.luka.android.studygroup.io.database.Database.TYPE_INT8;
import static rs.luka.android.studygroup.io.database.Database.TYPE_UNSIGNED;
import static rs.luka.android.studygroup.io.database.Database.TYPE_VARCHAR;
import static rs.luka.android.studygroup.io.database.Database.VALS;

/**
 * Created by luka on 16.10.16..
 */
public class LessonTable {
    public static class LessonCursor extends CursorWrapper {
        public LessonCursor(Cursor cursor) {
            super(cursor);
        }

        public String getLessonTitle() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            return getString(getColumnIndex(LessonEntry.COLUMN_NAME_LESSON));
        }

        public int getNoteCount() {
            if (isBeforeFirst() || isAfterLast()) {
                return -1;
            }
            return getInt(getColumnIndex(LessonEntry.COLUMN_NAME_NOTE_NO));
        }

        public int getQuestionCount() {
            if (isBeforeFirst() || isAfterLast()) {
                return -1;
            }
            return getInt(getColumnIndex(LessonEntry.COLUMN_NAME_QUESTION_NO));
        }

        public int getRequiredPermission() {
            if (isBeforeFirst() || isAfterLast()) {
                return -1;
            }
            return getInt(getColumnIndex(LessonEntry.COLUMN_NAME_PERMISSION));
        }
    }

    static final String TABLE_NAME       = "lessons";
    static final String SQL_CREATE_TABLE =
            CREATE + TABLE_NAME + "(" +
            LessonEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
            LessonEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + CourseTable.TABLE_NAME + "("
            + CourseTable.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
            LessonEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.LESSON_MAX_LENGTH + ")"
            + COMMA_SEP +
            LessonEntry.COLUMN_NAME_QUESTION_NO + TYPE_UNSIGNED + COMMA_SEP +
            LessonEntry.COLUMN_NAME_NOTE_NO + TYPE_UNSIGNED + COMMA_SEP +
            LessonEntry.COLUMN_NAME_PERMISSION + TYPE_INT4 +
            ")";
    static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;

    /**
     * Order: courseId, lesson, noteNo, questionNo
     */
    private static final String SQL_INSERT = INSERT + TABLE_NAME + " (" +
                                             LessonEntry.COLUMN_NAME_COURSE_ID + COMMA_SEP +
                                             LessonEntry.COLUMN_NAME_LESSON + COMMA_SEP +
                                             LessonEntry.COLUMN_NAME_NOTE_NO + COMMA_SEP +
                                             LessonEntry.COLUMN_NAME_QUESTION_NO + COMMA_SEP +
                                             LessonEntry.COLUMN_NAME_PERMISSION +
                                             ")" + VALS + "(?, ?, ?, ?, ?)";

    public static abstract class LessonEntry implements BaseColumns {
        public static final String COLUMN_NAME_COURSE_ID   = "course_id";
        public static final String COLUMN_NAME_LESSON      = "lesson";
        public static final String COLUMN_NAME_QUESTION_NO = "questions";
        public static final String COLUMN_NAME_NOTE_NO     = "notes";
        public static final String COLUMN_NAME_PERMISSION  = "permission";
    }


    private SQLiteOpenHelper helper;
    public LessonTable(Context c) {
        this.helper = Database.getInstance(c);
    }

    public Cursor queryLessons(ID courseId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = new LessonCursor(db.query(true,
                                             LessonTable.TABLE_NAME,
                                             null,
                                             LessonTable.LessonEntry.COLUMN_NAME_COURSE_ID + "="
                                             + courseId.getCourseIdValue(),
                                             null,
                                             null,
                                             null,
                                             null,
                                             null));
        return c;
    }

    public void removeLesson(ID courseId, String lesson) {
        SQLiteDatabase db = helper.getWritableDatabase();
        hideLesson(courseId, lesson);
        db.delete(NoteTable.TABLE_NAME,
                  NoteTable.NoteEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() +
                  (lesson != null ? " AND " + NoteTable.NoteEntry.COLUMN_NAME_LESSON + "='" + lesson + "'" : ""),
                  null);
        db.delete(QuestionTable.TABLE_NAME,
                  QuestionTable.QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() +
                  (lesson != null ?
                   " AND " + QuestionTable.QuestionEntry.COLUMN_NAME_LESSON + "='" + lesson + "'" :
                   ""),
                  null);
    }

    public void clearLessons(long courseId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(LessonTable.TABLE_NAME,
                  LessonTable.LessonEntry.COLUMN_NAME_COURSE_ID + "=" + courseId,
                  null);
    }

    public void insertLessons(long courseId, rs.luka.android.studygroup.network.Lessons.Lesson[] lessons) {
        SQLiteDatabase  db   = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(LessonTable.SQL_INSERT);
        db.beginTransaction();
        for (rs.luka.android.studygroup.network.Lessons.Lesson lesson : lessons) {
            stmt.bindLong(1, courseId);
            stmt.bindString(2, lesson.name);
            stmt.bindLong(3, lesson.noteNo);
            stmt.bindLong(4, lesson.questionNo);
            stmt.bindLong(5, lesson.permission);
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void hideLesson(ID courseId, String lesson) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(LessonTable.TABLE_NAME,
                  LessonTable.LessonEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() + " AND "
                  + LessonTable.LessonEntry.COLUMN_NAME_LESSON + "='" + lesson + "'",
                  null);
    }

    public void showLesson(ID courseId, int _id, String lesson, int noteCount, int questionCount) {
        ContentValues cv = new ContentValues(6);
        cv.put(LessonTable.LessonEntry._ID, _id);
        cv.put(LessonTable.LessonEntry.COLUMN_NAME_COURSE_ID, courseId.getCourseIdValue());
        cv.put(LessonTable.LessonEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(LessonTable.LessonEntry.COLUMN_NAME_NOTE_NO, noteCount);
        cv.put(LessonTable.LessonEntry.COLUMN_NAME_QUESTION_NO, questionCount);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.insert(LessonTable.TABLE_NAME, null, cv);
    }

    public void renameLesson(ID courseId, String oldName, String newName) {
        ContentValues cv = new ContentValues(1);
        cv.put(LessonTable.LessonEntry.COLUMN_NAME_LESSON, newName);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.update(LessonTable.TABLE_NAME,
                  cv,
                  LessonTable.LessonEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() + " AND "
                  + LessonTable.LessonEntry.COLUMN_NAME_LESSON + "='" + oldName + "'",
                  null);
        db.update(NoteTable.TABLE_NAME, cv, NoteTable.NoteEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() +
                                            " AND " + NoteTable.NoteEntry.COLUMN_NAME_LESSON + "='" + oldName + "'", null);
        db.update(QuestionTable.TABLE_NAME,
                  cv,
                  QuestionTable.QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() + " AND "
                  + QuestionTable.QuestionEntry.COLUMN_NAME_LESSON + "='" + oldName + "'",
                  null);
    }
}
