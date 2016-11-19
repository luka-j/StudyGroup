package rs.luka.android.studygroup.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.util.Log;

import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.ID;

import static rs.luka.android.studygroup.io.database.Database.COMMA_SEP;
import static rs.luka.android.studygroup.io.database.Database.CREATE;
import static rs.luka.android.studygroup.io.database.Database.DROP;
import static rs.luka.android.studygroup.io.database.Database.INSERT;
import static rs.luka.android.studygroup.io.database.Database.PRIMARY;
import static rs.luka.android.studygroup.io.database.Database.REF;
import static rs.luka.android.studygroup.io.database.Database.TAG;
import static rs.luka.android.studygroup.io.database.Database.TYPE_ID;
import static rs.luka.android.studygroup.io.database.Database.TYPE_INT1;
import static rs.luka.android.studygroup.io.database.Database.TYPE_INT8;
import static rs.luka.android.studygroup.io.database.Database.TYPE_VARCHAR;
import static rs.luka.android.studygroup.io.database.Database.VALS;

/**
 * Created by luka on 16.10.16..
 */
public class CourseTable {
        static final String TABLE_NAME       = "courses";
        static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                //CourseEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                CourseEntry.COLUMN_NAME_ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                CourseEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + GroupTable.TABLE_NAME + "("
                + GroupTable.GroupEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                CourseEntry.COLUMN_NAME_SUBJECT + TYPE_VARCHAR + "(" + Limits.COURSE_NAME_MAX_LENGTH
                + ")" + COMMA_SEP +
                CourseEntry.COLUMN_NAME_TEACHER + TYPE_VARCHAR + "(" + Limits.COURSE_TEACHER_MAX_LENGTH
                + ")" + COMMA_SEP +
                CourseEntry.COLUMN_NAME_YEAR + TYPE_INT1 + COMMA_SEP +
                CourseEntry.COLUMN_NAME_IMAGE + TYPE_INT1 +
                ")";

        static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;
        /**
         * Order: id, groupId, subject, teacher, year, image
         */
        static final String SQL_INSERT = INSERT + TABLE_NAME + " (" +
                                                 CourseEntry.COLUMN_NAME_ID + COMMA_SEP +
                                                 CourseEntry.COLUMN_NAME_GROUP_ID + COMMA_SEP +
                                                 CourseEntry.COLUMN_NAME_SUBJECT + COMMA_SEP +
                                                 CourseEntry.COLUMN_NAME_TEACHER + COMMA_SEP +
                                                 CourseEntry.COLUMN_NAME_YEAR  + COMMA_SEP +
                                                 CourseEntry.COLUMN_NAME_IMAGE +
                                                 ")" + VALS + "(?, ?, ?, ?, ?, ?)";

        public static abstract class CourseEntry implements BaseColumns {
            public static final String COLUMN_NAME_ID       = _ID;
            public static final String COLUMN_NAME_GROUP_ID = "group_id";
            public static final String COLUMN_NAME_SUBJECT  = "subject";
            public static final String COLUMN_NAME_TEACHER  = "teacher";
            public static final String COLUMN_NAME_YEAR     = "year";
            public static final String COLUMN_NAME_IMAGE    = "image_exists";
        }

    public static class CourseCursor extends CursorWrapper {

        public CourseCursor(Cursor cursor) {
            super(cursor);
        }

        public Course getCourse() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            ID id = new ID(getLong(getColumnIndex(CourseEntry.COLUMN_NAME_GROUP_ID)),
                           getLong(getColumnIndex(CourseEntry.COLUMN_NAME_ID)));
            return new Course(id, getString(getColumnIndex(CourseEntry.COLUMN_NAME_SUBJECT)),
                              getString(getColumnIndex(CourseEntry.COLUMN_NAME_TEACHER)),
                              getInt(getColumnIndex(CourseEntry.COLUMN_NAME_YEAR)),
                              getInt(getColumnIndex(CourseEntry.COLUMN_NAME_IMAGE)) != 0);
        }
    }

    private Database helper;
    public CourseTable(Context c) {
        this.helper = Database.getInstance(c);
    }

    public void insertCourse(ID id, String subject, String teacher, Integer year, boolean hasImage) {
        ContentValues cv = new ContentValues(5);
        cv.put(CourseEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(CourseEntry.COLUMN_NAME_ID, id.getCourseIdValue());
        cv.put(CourseEntry.COLUMN_NAME_SUBJECT, subject);
        cv.put(CourseEntry.COLUMN_NAME_TEACHER, teacher);
        cv.put(CourseEntry.COLUMN_NAME_YEAR, year);
        cv.put(CourseEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db   = helper.getWritableDatabase();
        long           code = db.insert(TABLE_NAME, null, cv);
    }

    public void updateCourse(ID id, String subject, String teacher, Integer year, boolean hasImage) {
        ContentValues cv = new ContentValues(3);
        cv.put(CourseEntry.COLUMN_NAME_SUBJECT, subject);
        cv.put(CourseEntry.COLUMN_NAME_TEACHER, teacher);
        cv.put(CourseEntry.COLUMN_NAME_YEAR, year);
        cv.put(CourseEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db = helper.getWritableDatabase();
        long code = db.update(TABLE_NAME,
                              cv,
                              CourseEntry.COLUMN_NAME_GROUP_ID + "=" + id.getGroupIdValue() +
                              " AND " + CourseEntry.COLUMN_NAME_ID + "=" + id.getCourseIdValue(),
                              null);
    }

    public void removeCourse(ID id) {
        hideCourse(id);
        new LessonTable(helper.context).removeLesson(id, null);
    }

    /**
     * Like removeCourse, but doesn't remove lessons
     * @param id
     */
    public void hideCourse(ID id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long code = db.delete(TABLE_NAME,
                              CourseEntry.COLUMN_NAME_ID + "=" + id.getCourseIdValue(),
                              null);
        Log.i(TAG, "hideCourse status: " + code);
    }

    public void clearCourses(long groupId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_NAME,
                  CourseEntry.COLUMN_NAME_GROUP_ID + "=" + groupId,
                  null);
    }

    public void insertCourses(Course[] courses) {
        SQLiteDatabase  db   = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(SQL_INSERT);
        db.beginTransaction();

        for (Course course : courses) {
            stmt.bindLong(1, course.getIdValue());      //id
            stmt.bindLong(2, course.getGroupIdValue()); //group id
            stmt.bindString(3, course.getSubject());    //subject
            stmt.bindString(4, course.getTeacher());    //teacher
            if(course.getYear() == null) {              //year
                stmt.bindNull(5);
            } else {
                stmt.bindLong(5, course.getYear());
            }
            stmt.bindLong(6, course.hasImage() ? 1:0);   //image
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public CourseCursor queryCourses(ID groupId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        CourseCursor c = new CourseCursor(db.query(TABLE_NAME,
                                                   null,
                                                   CourseEntry.COLUMN_NAME_GROUP_ID
                                                   + "=" + groupId.getGroupIdValue(),
                                                   null,
                                                   null,
                                                   null,
                                                   CourseEntry.COLUMN_NAME_ID
                                                   + " asc"));
        return c;
    }

    public Course queryCourse(ID courseId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        CourseCursor c = new CourseCursor(db.query(TABLE_NAME,
                                                   null,
                                                   CourseEntry.COLUMN_NAME_ID
                                                   + "=" + courseId.getCourseIdValue(),
                                                   null,
                                                   null,
                                                   null,
                                                   null,
                                                   "1"));
        c.moveToNext();
        return c.getCourse();
    }
}
