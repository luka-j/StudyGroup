package rs.luka.android.studygroup.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import java.util.Date;

import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.model.Exam;
import rs.luka.android.studygroup.model.ID;

import static rs.luka.android.studygroup.io.database.Database.COMMA_SEP;
import static rs.luka.android.studygroup.io.database.Database.CREATE;
import static rs.luka.android.studygroup.io.database.Database.DROP;
import static rs.luka.android.studygroup.io.database.Database.INSERT;
import static rs.luka.android.studygroup.io.database.Database.PRIMARY;
import static rs.luka.android.studygroup.io.database.Database.REF;
import static rs.luka.android.studygroup.io.database.Database.TYPE_ID;
import static rs.luka.android.studygroup.io.database.Database.TYPE_INT8;
import static rs.luka.android.studygroup.io.database.Database.TYPE_VARCHAR;
import static rs.luka.android.studygroup.io.database.Database.VALS;

/**
 * Created by luka on 16.10.16..
 */
public class ExamTable {
    private static final String TABLE_NAME       = "exams";
    static final String SQL_CREATE_TABLE =
            CREATE + TABLE_NAME + " (" +
            //ExamEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
            ExamEntry.COLUMN_NAME_ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
            ExamEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + GroupTable.TABLE_NAME + "("
            + GroupTable.GroupEntry.COLUMN_NAME_ID
            + ")" + COMMA_SEP +
            ExamEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + CourseTable.TABLE_NAME + "("
            + CourseTable.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
            ExamEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.EXAM_LESSON_MAX_LENGTH + ")"
            + COMMA_SEP +
            ExamEntry.COLUMN_NAME_DATE + TYPE_INT8 + COMMA_SEP +
            ExamEntry.COLUMN_NAME_CLASS + TYPE_VARCHAR + "(" + Limits.EXAM_CLASS_MAX_LENGTH + ")"
            + COMMA_SEP +
            ExamEntry.COLUMN_NAME_TYPE + TYPE_VARCHAR + "(" + Limits.EXAM_TYPE_MAX_LENGTH + ")"
            + ")";
    static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;

    /**
     * Order: id, groupId, courseId, lesson, date, class, type
     */
    private static final String SQL_INSERT = INSERT + TABLE_NAME + " (" +
                                             ExamEntry.COLUMN_NAME_ID + COMMA_SEP +
                                             ExamEntry.COLUMN_NAME_GROUP_ID + COMMA_SEP +
                                             ExamEntry.COLUMN_NAME_COURSE_ID + COMMA_SEP +
                                             ExamEntry.COLUMN_NAME_LESSON + COMMA_SEP +
                                             ExamEntry.COLUMN_NAME_DATE + COMMA_SEP +
                                             ExamEntry.COLUMN_NAME_CLASS + COMMA_SEP +
                                             ExamEntry.COLUMN_NAME_TYPE +
                                             ")" + VALS + "(?, ?, ?, ?, ?, ?, ?)";

    public static abstract class ExamEntry implements BaseColumns {
        public static final String COLUMN_NAME_ID        = _ID;
        public static final String COLUMN_NAME_GROUP_ID  = "group_id";
        public static final String COLUMN_NAME_COURSE_ID = "course_id";
        public static final String COLUMN_NAME_LESSON    = "lesson";
        public static final String COLUMN_NAME_DATE      = "date";
        public static final String COLUMN_NAME_CLASS     = "class";
        public static final String COLUMN_NAME_TYPE      = "type";
    }

    public static class ExamCursor extends CursorWrapper {
        private Context context;

        public ExamCursor(Context c, Cursor cursor) {
            super(cursor);
            context = c.getApplicationContext();
        }

        public Exam getExam() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            ID id = new ID(getLong(getColumnIndex(ExamEntry.COLUMN_NAME_GROUP_ID)),
                           getLong(getColumnIndex(ExamEntry.COLUMN_NAME_COURSE_ID)),
                           getLong(getColumnIndex(ExamEntry.COLUMN_NAME_ID)));
            return new Exam(context,
                            id,
                            getString(getColumnIndex(ExamEntry.COLUMN_NAME_CLASS)),
                            getString(getColumnIndex(ExamEntry.COLUMN_NAME_LESSON)),
                            getString(getColumnIndex(ExamEntry.COLUMN_NAME_TYPE)),
                            new Date(getLong(getColumnIndex(ExamEntry.COLUMN_NAME_DATE))));
        }
    }


    private Database helper;
    public ExamTable(Context c) {
        this.helper = Database.getInstance(c);
    }

    public void insertExam(ID id, String klass, String lesson, String type, long date) {
        ContentValues cv = new ContentValues(7);
        cv.put(ExamEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(ExamEntry.COLUMN_NAME_COURSE_ID, id.getCourseIdValue());
        cv.put(ExamEntry.COLUMN_NAME_ID, id.getItemIdValue());
        cv.put(ExamEntry.COLUMN_NAME_CLASS, klass);
        cv.put(ExamEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(ExamEntry.COLUMN_NAME_TYPE, type);
        cv.put(ExamEntry.COLUMN_NAME_DATE, date);
        SQLiteDatabase db   = helper.getWritableDatabase();
        long           code = db.insert(TABLE_NAME, null, cv);
    }

    public void updateExam(ID id, String klass, String lesson, String type, long date) {
        ContentValues cv = new ContentValues(4);
        cv.put(ExamEntry.COLUMN_NAME_CLASS, klass);
        cv.put(ExamEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(ExamEntry.COLUMN_NAME_TYPE, type);
        cv.put(ExamEntry.COLUMN_NAME_DATE, date);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.update(TABLE_NAME, cv, ExamEntry.COLUMN_NAME_GROUP_ID + "=" + id.getGroupIdValue() + " AND "
                                            + ExamEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() + " AND "
                                            + ExamEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(), null);
    }

    public void clearExams(long groupId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(ExamTable.TABLE_NAME,
                  ExamTable.ExamEntry.COLUMN_NAME_GROUP_ID + "=" + groupId,
                  null);
    }

    public void insertExams(Exam[] exams) {
        SQLiteDatabase  db   = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(SQL_INSERT);
        db.beginTransaction();

        for (Exam exam : exams) {
            stmt.bindLong(1, exam.getIdValue());                    //id
            stmt.bindLong(2, exam.getGroupIdValue());               //group id
            stmt.bindLong(3, exam.getCourseIdValue());              //courseId
            stmt.bindString(4, exam.getLesson());                   //lesson
            stmt.bindLong(5, exam.getCalendar().getTimeInMillis()); //date
            stmt.bindString(6, exam.getKlassName());                //class
            stmt.bindString(7, exam.getType());                     //type
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void hideExam(ID id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long code = db.delete(TABLE_NAME,
                              ExamEntry.COLUMN_NAME_GROUP_ID + "=" + id.getGroupIdValue() + " AND "
                              + ExamEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() + " AND "
                              + ExamEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                              null);
    }

    public void removeExam(ID id, String lesson) {
        hideExam(id);
        new LessonTable(helper.context).removeLesson(id, lesson);
    }

    public ExamCursor queryExams(ID groupId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        ExamCursor c = new ExamCursor(helper.context, db.query(TABLE_NAME,
                                                               null,
                                                               ExamEntry.COLUMN_NAME_GROUP_ID
                                                               + "=" + groupId.getGroupIdValue(),
                                                               null,
                                                               null,
                                                               null,
                                                               ExamEntry.COLUMN_NAME_DATE + " asc"));
        return c;
    }
}
