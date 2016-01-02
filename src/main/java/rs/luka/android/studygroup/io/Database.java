package rs.luka.android.studygroup.io;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Date;

import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Exam;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.model.Question;

/**
 * Created by luka on 28.8.15..
 */
public class Database extends SQLiteOpenHelper {
    private static final String TAG           = "studygroup.Database";
    private static final String DB_NAME       = "base.sqlite";
    private static final int    VERSION       = 8;
    private static final String DROP          = "DROP TABLE IF EXISTS ";
    private static final String CREATE        = "CREATE TABLE ";
    private static final String TYPE_VARCHAR  = " VARCHAR";
    private static final String TYPE_INT8     = " INTEGER";
    private static final String TYPE_INT4     = " INTEGER";
    private static final String TYPE_INT2     = " INTEGER";
    private static final String TYPE_INT1     = " INTEGER";
    private static final String TYPE_UNSIGNED = " INTEGER UNSIGNED";
    private static final String TYPE_NONNULL  = " NOT NULL";
    private static final String TYPE_ID       = TYPE_UNSIGNED + TYPE_NONNULL;
    private static final String TYPE_TEXT     = " TEXT";
    private static final String COMMA_SEP     = ",";
    private static final String PRIMARY       = " PRIMARY KEY";
    private static final String REF           = " REFERENCES ";
    private static Database instance;
    private        Context  context;

    private Database(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.context = context;
    }

    public static Database getInstance(Context c) {
        if (instance == null) { instance = new Database(c); }
        return instance;
    }

    static String parens(String s) {
        return "(" + s + ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Groups.SQL_CREATE_TABLE);
        db.execSQL(Courses.SQL_CREATE_TABLE);
        db.execSQL(Notes.SQL_CREATE_TABLE);
        db.execSQL(Questions.SQL_CREATE_TABLE);
        db.execSQL(Exams.SQL_CREATE_TABLE);
        db.execSQL(Lessons.SQL_CREATE_TABLE);
        Log.d(TAG, "Created tables");
    }

    /**
     * Posto sluzi kao lokalna verzija baze koja se vec nalazi na serveru, nema potrebe migrirati podatke,
     * vec je lakse ponovo ih preuzeti.
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Groups.SQL_DELETE_TABLE);
        db.execSQL(Courses.SQL_DELETE_TABLE);
        db.execSQL(Notes.SQL_DELETE_TABLE);
        db.execSQL(Questions.SQL_DELETE_TABLE);
        db.execSQL(Exams.SQL_DELETE_TABLE);
        db.execSQL(Lessons.SQL_DELETE_TABLE);
        Log.i(TAG, "Upgraded; dropped tables");
        onCreate(db);
    }

    public void insertGroup(ID id, String name, String place, boolean hasImage) {
        ContentValues cv = new ContentValues(4);
        cv.put(Groups.GroupEntry.COLUMN_NAME_ID, id.getGroupIdValue());
        cv.put(Groups.GroupEntry.COLUMN_NAME_SCHOOL, name);
        cv.put(Groups.GroupEntry.COLUMN_NAME_PLACE, place);
        cv.put(Groups.GroupEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db   = getWritableDatabase();
        long           code = db.insert(Groups.TABLE_NAME, null, cv);
    }

    public void updateGroup(ID id, String name, String place, boolean hasImage) {
        ContentValues cv = new ContentValues(2);
        cv.put(Groups.GroupEntry.COLUMN_NAME_SCHOOL, name);
        cv.put(Groups.GroupEntry.COLUMN_NAME_PLACE, place);
        cv.put(Groups.GroupEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db = getWritableDatabase();
        long code = db.update(Groups.TABLE_NAME,
                              cv,
                              Groups.GroupEntry.COLUMN_NAME_ID + "=" + id.getGroupIdValue(),
                              null);
    }

    public void removeGroup(ID id) {
        SQLiteDatabase db = getWritableDatabase();
        long code = db.delete(Groups.TABLE_NAME,
                              Groups.GroupEntry.COLUMN_NAME_ID + "=" + id.getGroupIdValue(),
                              null);
    }

    public GroupCursor queryGroups() {
        SQLiteDatabase db = getWritableDatabase();
        GroupCursor c = new GroupCursor(db.query(Groups.TABLE_NAME,
                                                 null,
                                                 null,
                                                 null,
                                                 null,
                                                 null,
                                                 Groups.GroupEntry.COLUMN_NAME_ID + " asc"));
        return c;
    }

    public long getGroupCount() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), Groups.TABLE_NAME);
    }

    public void insertCourse(ID id, String subject, String teacher, Integer year, boolean hasImage) {
        ContentValues cv = new ContentValues(5);
        cv.put(Courses.CourseEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(Courses.CourseEntry.COLUMN_NAME_ID, id.getCourseIdValue());
        cv.put(Courses.CourseEntry.COLUMN_NAME_SUBJECT, subject);
        cv.put(Courses.CourseEntry.COLUMN_NAME_TEACHER, teacher);
        cv.put(Courses.CourseEntry.COLUMN_NAME_YEAR, year);
        cv.put(Courses.CourseEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db   = getWritableDatabase();
        long           code = db.insert(Courses.TABLE_NAME, null, cv);
    }

    public void updateCourse(ID id, String subject, String teacher, Integer year, boolean hasImage) {
        ContentValues cv = new ContentValues(3);
        cv.put(Courses.CourseEntry.COLUMN_NAME_SUBJECT, subject);
        cv.put(Courses.CourseEntry.COLUMN_NAME_TEACHER, teacher);
        cv.put(Courses.CourseEntry.COLUMN_NAME_YEAR, year);
        cv.put(Courses.CourseEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db = getWritableDatabase();
        long code = db.update(Courses.TABLE_NAME,
                              cv,
                              Courses.CourseEntry.COLUMN_NAME_GROUP_ID + "=" + id.getGroupIdValue() +
                              " AND " + Courses.CourseEntry.COLUMN_NAME_ID + "=" + id.getCourseIdValue(),
                              null);
    }

    public void removeCourse(ID id) {
        hideCourse(id);
        removeLesson(id, null);
    }

    public void hideCourse(ID id) {
        SQLiteDatabase db = getWritableDatabase();
        long code = db.delete(Courses.TABLE_NAME,
                               Courses.CourseEntry.COLUMN_NAME_ID + "="
                              + id.getCourseIdValue(),
                              null);
    }

    public CourseCursor queryCourses(ID groupId) {
        SQLiteDatabase db = getReadableDatabase();
        CourseCursor c = new CourseCursor(getReadableDatabase().query(Courses.TABLE_NAME,
                                                                      null,
                                                                      Courses.CourseEntry.COLUMN_NAME_GROUP_ID
                                                                      + "=" + groupId.getGroupIdValue(),
                                                                      null,
                                                                      null,
                                                                      null,
                                                                      Courses.CourseEntry.COLUMN_NAME_ID
                                                                      + " asc"));
        return c;
    }

    public Course queryCourse(ID courseId) {
        SQLiteDatabase db = getReadableDatabase();
        CourseCursor c = new CourseCursor(db.query(Courses.TABLE_NAME,
                                                   null,
                                                   Courses.CourseEntry.COLUMN_NAME_ID
                                                   + "=" + courseId.getCourseIdValue(),
                                                   null,
                                                   null,
                                                   null,
                                                   null,
                                                   "1"));
        c.moveToNext();
        return c.getCourse();
    }

    public Cursor queryLessons(ID courseId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = new LessonCursor(db.query(true,
                                             Lessons.TABLE_NAME,
                                             new String[]{Lessons.LessonEntry._ID,
                                                          Lessons.LessonEntry.COLUMN_NAME_LESSON,
                                                          Lessons.LessonEntry.COLUMN_NAME_NOTE_NO,
                                                          Lessons.LessonEntry.COLUMN_NAME_QUESTION_NO},
                                             Lessons.LessonEntry.COLUMN_NAME_COURSE_ID + "="
                                             + courseId.getCourseIdValue(),
                                             null,
                                             null,
                                             null,
                                             null,
                                             null));
        return c;
    }

    public void removeLesson(ID courseId, String lesson) {
        SQLiteDatabase db = getWritableDatabase();
        hideLesson(courseId, lesson);
        db.delete(Notes.TABLE_NAME,
                  Notes.NoteEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() +
                  (lesson != null ? " AND " + Notes.NoteEntry.COLUMN_NAME_LESSON + "='" + lesson + "'" : ""),
                  null);
        db.delete(Questions.TABLE_NAME,
                  Questions.QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() +
                  (lesson != null ?
                   " AND " + Questions.QuestionEntry.COLUMN_NAME_LESSON + "='" + lesson + "'" :
                   ""),
                  null);
    }

    public void hideLesson(ID courseId, String lesson) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Lessons.TABLE_NAME,
                  Lessons.LessonEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() + " AND "
                  + Lessons.LessonEntry.COLUMN_NAME_LESSON + "='" + lesson + "'",
                  null);
    }

    public void showLesson(ID courseId, int _id, String lesson, int noteCount, int questionCount) {
        ContentValues cv = new ContentValues(6);
        cv.put(Lessons.LessonEntry._ID, _id);
        cv.put(Lessons.LessonEntry.COLUMN_NAME_GROUP_ID, courseId.getGroupIdValue());
        cv.put(Lessons.LessonEntry.COLUMN_NAME_COURSE_ID, courseId.getCourseIdValue());
        cv.put(Lessons.LessonEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Lessons.LessonEntry.COLUMN_NAME_NOTE_NO, noteCount);
        cv.put(Lessons.LessonEntry.COLUMN_NAME_QUESTION_NO, questionCount);
        SQLiteDatabase db = getWritableDatabase();
        db.insert(Lessons.TABLE_NAME, null, cv);
    }

    public void renameLesson(ID courseId, String oldName, String newName) {
        ContentValues cv = new ContentValues(1);
        cv.put(Lessons.LessonEntry.COLUMN_NAME_LESSON, newName);
        SQLiteDatabase db = getWritableDatabase();
        db.update(Lessons.TABLE_NAME,
                  cv,
                  Lessons.LessonEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() + " AND "
                  + Lessons.LessonEntry.COLUMN_NAME_LESSON + "='" + oldName + "'",
                  null);
        db.update(Notes.TABLE_NAME, cv, Notes.NoteEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() +
                                        " AND " + Notes.NoteEntry.COLUMN_NAME_LESSON + "='" + oldName + "'", null);
        db.update(Questions.TABLE_NAME,
                  cv,
                  Questions.QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() + " AND "
                  + Questions.QuestionEntry.COLUMN_NAME_LESSON + "='" + oldName + "'",
                  null);
    }

    public void insertNote(ID id, String lesson, String text, boolean hasImage, boolean hasAudio) {
        ContentValues cv = new ContentValues(5);
        cv.put(Notes.NoteEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(Notes.NoteEntry.COLUMN_NAME_COURSE_ID, id.getCourseIdValue());
        cv.put(Notes.NoteEntry.COLUMN_NAME_ID, id.getItemIdValue());
        cv.put(Notes.NoteEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Notes.NoteEntry.COLUMN_NAME_TEXT, text);
        cv.put(Notes.NoteEntry.COLUMN_NAME_IMAGE, hasImage);
        cv.put(Notes.NoteEntry.COLUMN_NAME_AUDIO, hasAudio);
        SQLiteDatabase db   = getWritableDatabase();
        long           code = db.insert(Notes.TABLE_NAME, null, cv);
        if (code != -1) {
            editLessonCounts(id, lesson, 18, 5); // TODO: 5.9.15. izbrisati kad napišem serverside
        }
    }

    public void updateNote(ID id, String lesson, String text, boolean hasImage, boolean hasAudio) {
        ContentValues cv = new ContentValues(2);
        cv.put(Notes.NoteEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Notes.NoteEntry.COLUMN_NAME_TEXT, text);
        cv.put(Notes.NoteEntry.COLUMN_NAME_IMAGE, hasImage);
        cv.put(Notes.NoteEntry.COLUMN_NAME_AUDIO, hasAudio);
        SQLiteDatabase db = getWritableDatabase();
        db.update(Notes.TABLE_NAME, cv, Notes.NoteEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(), null);
    }

    public void removeNote(ID id, String lesson) {
        SQLiteDatabase db = getWritableDatabase();
        long code = db.delete(Notes.TABLE_NAME,
                              Notes.NoteEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                              null);
    }

    public NoteCursor queryNotes(ID courseId, String lesson) {
        if (lesson.isEmpty()) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        NoteCursor c = new NoteCursor(db.query(Notes.TABLE_NAME,
                                               null,
                                               Notes.NoteEntry.COLUMN_NAME_COURSE_ID
                                               + "=" + courseId.getCourseIdValue() +
                                               " AND " + Notes.NoteEntry.COLUMN_NAME_LESSON
                                               + "='" + lesson + "'",
                                               null,
                                               null,
                                               null,
                                               Notes.NoteEntry.COLUMN_NAME_ID + " asc"));
        return c;
    }

    public void insertQuestion(ID id, String lesson, String question, String answer, boolean hasImage) {
        ContentValues cv = new ContentValues(5);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(Questions.QuestionEntry.COLUMN_NAME_COURSE_ID, id.getCourseIdValue());
        cv.put(Questions.QuestionEntry.COLUMN_NAME_ID, id.getItemIdValue());
        cv.put(Questions.QuestionEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_QUESTION, question);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_ANSWER, answer);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db   = getWritableDatabase();
        long           code = db.insert(Questions.TABLE_NAME, null, cv);
        if (code != -1) {
            editLessonCounts(id, lesson, 18, 5); // TODO: 5.9.15.  izbrisati kad napišem serverside
        }
    }

    public void updateQuestion(ID id, String lesson, String question, String answer, boolean hasImage) {
        ContentValues cv = new ContentValues(3);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_QUESTION, question);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_ANSWER, answer);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db = getWritableDatabase();
        db.update(Questions.TABLE_NAME,
                  cv,
                  Questions.QuestionEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                  null);
    }

    public void removeQuestion(ID id, String lesson) {
        SQLiteDatabase db = getWritableDatabase();
        long code = db.delete(Questions.TABLE_NAME,
                              Questions.QuestionEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                              null);
    }

    public QuestionCursor queryQuestions(ID courseId, String lesson) {
        if (lesson.isEmpty()) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        QuestionCursor c = new QuestionCursor(db.query(Questions.TABLE_NAME,
                                                       null,
                                                       Questions.QuestionEntry.COLUMN_NAME_COURSE_ID
                                                       + "=" + courseId.getCourseId() +
                                                       " AND "
                                                       + Notes.NoteEntry.COLUMN_NAME_LESSON + "='"
                                                       + lesson + "'",
                                                       null,
                                                       null,
                                                       null,
                                                       Questions.QuestionEntry.COLUMN_NAME_ID
                                                       + " asc"));
        return c;
    }

    public void insertExam(ID id, String klass, String lesson, String type, long date) {
        ContentValues cv = new ContentValues(7);
        cv.put(Exams.ExamEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(Exams.ExamEntry.COLUMN_NAME_COURSE_ID, id.getCourseIdValue());
        cv.put(Exams.ExamEntry.COLUMN_NAME_ID, id.getItemIdValue());
        cv.put(Exams.ExamEntry.COLUMN_NAME_CLASS, klass);
        cv.put(Exams.ExamEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Exams.ExamEntry.COLUMN_NAME_TYPE, type);
        cv.put(Exams.ExamEntry.COLUMN_NAME_DATE, date);
        SQLiteDatabase db   = getWritableDatabase();
        long           code = db.insert(Exams.TABLE_NAME, null, cv);
    }

    public void updateExam(ID id, String klass, String lesson, String type, long date) {
        ContentValues cv = new ContentValues(4);
        cv.put(Exams.ExamEntry.COLUMN_NAME_CLASS, klass);
        cv.put(Exams.ExamEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Exams.ExamEntry.COLUMN_NAME_TYPE, type);
        cv.put(Exams.ExamEntry.COLUMN_NAME_DATE, date);
        SQLiteDatabase db = getWritableDatabase();
        db.update(Exams.TABLE_NAME, cv, Exams.ExamEntry.COLUMN_NAME_GROUP_ID + "=" + id.getGroupIdValue() + " AND "
                                        + Exams.ExamEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() + " AND "
                                        + Exams.ExamEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(), null);
    }

    public void hideExam(ID id) {
        SQLiteDatabase db = getWritableDatabase();
        long code = db.delete(Exams.TABLE_NAME,
                              Exams.ExamEntry.COLUMN_NAME_GROUP_ID + "=" + id.getGroupIdValue() + " AND "
                              + Exams.ExamEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() + " AND "
                              + Exams.ExamEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                              null);
    }

    public void removeExam(ID id, String lesson) {
        hideExam(id);
        removeLesson(id, lesson);
    }

    public ExamCursor queryExams(ID groupId) {
        SQLiteDatabase db = getReadableDatabase();
        ExamCursor c = new ExamCursor(context, db.query(Exams.TABLE_NAME,
                                                        null,
                                                        Exams.ExamEntry.COLUMN_NAME_GROUP_ID
                                                        + "=" + groupId.getGroupId(),
                                                        null,
                                                        null,
                                                        null,
                                                        Exams.ExamEntry.COLUMN_NAME_ID
                                                        + " asc"));
        return c;
    }

    public void editLessonCounts(ID courseId, String lesson, int noteCount, int questionCount) {
        if (lesson.startsWith(Question.EXAM_PREFIX)) return; //belezim samo one lekcije koje pripadaju premetu
        ContentValues cv = new ContentValues(5);
        cv.put(Lessons.LessonEntry.COLUMN_NAME_NOTE_NO, noteCount);
        cv.put(Lessons.LessonEntry.COLUMN_NAME_QUESTION_NO, questionCount);

        SQLiteDatabase db = getWritableDatabase();
        Cursor existing = db.query(Lessons.TABLE_NAME,
                                   new String[]{Lessons.LessonEntry._ID},
                                   Lessons.LessonEntry.COLUMN_NAME_GROUP_ID + "=" + courseId.getGroupIdValue() + " AND " +
                                   Lessons.LessonEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() + " AND " +
                                   Lessons.LessonEntry.COLUMN_NAME_LESSON + "='" + lesson + "'",
                                   null,
                                   null,
                                   null,
                                   null);
        existing.moveToFirst();
        if (existing.getCount() == 0) {
            cv.put(Lessons.LessonEntry.COLUMN_NAME_GROUP_ID, courseId.getGroupIdValue());
            cv.put(Lessons.LessonEntry.COLUMN_NAME_COURSE_ID, courseId.getCourseIdValue());
            cv.put(Lessons.LessonEntry.COLUMN_NAME_LESSON, lesson);
            db.insert(Lessons.TABLE_NAME, null, cv);
        } else {
            db.update(Lessons.TABLE_NAME,
                      cv,
                      Lessons.LessonEntry._ID + "="
                      + existing.getInt(existing.getColumnIndex(Lessons.LessonEntry._ID)),
                      null);
        }
        existing.close();
        //TODO http://stackoverflow.com/questions/418898/sqlite-upsert-not-insert-or-replace
    }

    private static class Groups {
        private static final String TABLE_NAME       = "groups";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                GroupEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                GroupEntry.COLUMN_NAME_ID + TYPE_ID + COMMA_SEP +
                GroupEntry.COLUMN_NAME_SCHOOL + TYPE_VARCHAR + "(" + Limits.GROUP_NAME_MAX_LENGTH + ")"
                + COMMA_SEP +
                GroupEntry.COLUMN_NAME_PLACE + TYPE_VARCHAR + "(" + Limits.GROUP_PLACE_MAX_LENGTH + ")" + COMMA_SEP +
                GroupEntry.COLUMN_NAME_IMAGE + TYPE_INT1 +
                ")";

        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;

        public Groups() {}

        public static abstract class GroupEntry implements BaseColumns {
            public static final String COLUMN_NAME_ID     = "id";
            public static final String COLUMN_NAME_SCHOOL = "name";
            public static final String COLUMN_NAME_PLACE  = "place";
            public static final String COLUMN_NAME_IMAGE  = "image_exists";
        }
    }

    public static class GroupCursor extends CursorWrapper {

        public GroupCursor(Cursor cursor) {
            super(cursor);
        }

        public Group getGroup() {
            if (isBeforeFirst() || isAfterLast()) { return null; }
            ID id = new ID(getLong(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_ID)));
            return new Group(id, getString(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_SCHOOL)),
                             getString(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_PLACE)),
                             getInt(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_IMAGE))!=0);
        }
    }

    private static class Courses {
        private static final String TABLE_NAME       = "courses";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                CourseEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                CourseEntry.COLUMN_NAME_ID + TYPE_ID + COMMA_SEP +
                CourseEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + Groups.TABLE_NAME + "("
                + Groups.GroupEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                CourseEntry.COLUMN_NAME_SUBJECT + TYPE_VARCHAR + "(" + Limits.COURSE_NAME_MAX_LENGTH
                + ")" + COMMA_SEP +
                CourseEntry.COLUMN_NAME_TEACHER + TYPE_VARCHAR + "(" + Limits.COURSE_TEACHER_MAX_LENGTH
                + ")" + COMMA_SEP +
                CourseEntry.COLUMN_NAME_YEAR + TYPE_INT1 + COMMA_SEP +
                CourseEntry.COLUMN_NAME_IMAGE + TYPE_INT1 +
                ")";

        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;

        public static abstract class CourseEntry implements BaseColumns {
            public static final String COLUMN_NAME_ID       = "id";
            public static final String COLUMN_NAME_GROUP_ID = "group_id";
            public static final String COLUMN_NAME_SUBJECT  = "subject";
            public static final String COLUMN_NAME_TEACHER  = "teacher";
            public static final String COLUMN_NAME_YEAR     = "year";
            public static final String COLUMN_NAME_IMAGE    = "image_exists";
        }
    }

    public static class CourseCursor extends CursorWrapper {

        public CourseCursor(Cursor cursor) {
            super(cursor);
        }

        public Course getCourse() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            ID id = new ID(getLong(getColumnIndex(Courses.CourseEntry.COLUMN_NAME_GROUP_ID)),
                           getLong(getColumnIndex(Courses.CourseEntry.COLUMN_NAME_ID)));
            return new Course(id, getString(getColumnIndex(Courses.CourseEntry.COLUMN_NAME_SUBJECT)),
                              getString(getColumnIndex(Courses.CourseEntry.COLUMN_NAME_TEACHER)),
                              getInt(getColumnIndex(Courses.CourseEntry.COLUMN_NAME_YEAR)),
                              getInt(getColumnIndex(Courses.CourseEntry.COLUMN_NAME_IMAGE)) != 0);
        }
    }

    public static class LessonCursor extends CursorWrapper {
        public LessonCursor(Cursor cursor) {
            super(cursor);
        }

        public String getLessonTitle() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            return getString(getColumnIndex(Lessons.LessonEntry.COLUMN_NAME_LESSON));
        }

        public int getNoteCount() {
            if (isBeforeFirst() || isAfterLast()) {
                return -1;
            }
            return getInt(getColumnIndex(Lessons.LessonEntry.COLUMN_NAME_NOTE_NO));
        }

        public int getQuestionCount() {
            if (isBeforeFirst() || isAfterLast()) {
                return -1;
            }
            return getInt(getColumnIndex(Lessons.LessonEntry.COLUMN_NAME_QUESTION_NO));
        }
    }

    private static class Notes {
        private static final String TABLE_NAME       = "notes";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                NoteEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                NoteEntry.COLUMN_NAME_ID + TYPE_ID + COMMA_SEP +
                NoteEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + Groups.TABLE_NAME + "("
                + Groups.GroupEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                NoteEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + Courses.TABLE_NAME + "("
                + Courses.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                NoteEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.LESSON_MAX_LENGTH + ")"
                + TYPE_NONNULL + COMMA_SEP +
                NoteEntry.COLUMN_NAME_TEXT + TYPE_TEXT + COMMA_SEP +
                NoteEntry.COLUMN_NAME_IMAGE + TYPE_INT1 + COMMA_SEP +
                NoteEntry.COLUMN_NAME_AUDIO + TYPE_INT1 +
                ")";

        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;

        public static abstract class NoteEntry implements BaseColumns {
            public static final String COLUMN_NAME_ID        = "id";
            public static final String COLUMN_NAME_GROUP_ID  = "group_id";
            public static final String COLUMN_NAME_COURSE_ID = "course_id";
            public static final String COLUMN_NAME_LESSON    = "lesson";
            public static final String COLUMN_NAME_TEXT      = "text";
            public static final String COLUMN_NAME_IMAGE     = "image_exists";
            public static final String COLUMN_NAME_AUDIO     = "audio_exists";
        }
    }

    public static class NoteCursor extends CursorWrapper {
        public NoteCursor(Cursor cursor) {
            super(cursor);
        }

        public Note getNote() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            ID id = new ID(getLong(getColumnIndex(Notes.NoteEntry.COLUMN_NAME_GROUP_ID)),
                           getLong(getColumnIndex(Notes.NoteEntry.COLUMN_NAME_COURSE_ID)),
                           getLong(getColumnIndex(Notes.NoteEntry.COLUMN_NAME_ID)));
            return new Note(id,
                            getString(getColumnIndex(Notes.NoteEntry.COLUMN_NAME_LESSON)),
                            getString(getColumnIndex(Notes.NoteEntry.COLUMN_NAME_TEXT)),
                            getInt(getColumnIndex(Notes.NoteEntry.COLUMN_NAME_IMAGE)) != 0,
                            getInt(getColumnIndex(Notes.NoteEntry.COLUMN_NAME_IMAGE)) != 0);
        }
    }

    private static class Questions {
        private static final String TABLE_NAME       = "questions";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                QuestionEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_ID + TYPE_ID + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + Groups.TABLE_NAME + "("
                + Groups.GroupEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + Courses.TABLE_NAME + "("
                + Courses.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.LESSON_MAX_LENGTH + ")"
                + TYPE_NONNULL + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_QUESTION + TYPE_TEXT + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_ANSWER + TYPE_TEXT + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_IMAGE + TYPE_INT1 +
                ")";
        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;

        public static abstract class QuestionEntry implements BaseColumns {
            public static final String COLUMN_NAME_ID        = Notes.NoteEntry.COLUMN_NAME_ID;
            public static final String COLUMN_NAME_GROUP_ID  = Notes.NoteEntry.COLUMN_NAME_GROUP_ID;
            public static final String COLUMN_NAME_COURSE_ID = Notes.NoteEntry.COLUMN_NAME_COURSE_ID;
            public static final String COLUMN_NAME_LESSON    = Notes.NoteEntry.COLUMN_NAME_LESSON;
            public static final String COLUMN_NAME_QUESTION  = "question";
            public static final String COLUMN_NAME_ANSWER    = "answer";
            public static final String COLUMN_NAME_IMAGE     = Notes.NoteEntry.COLUMN_NAME_IMAGE;
        }
    }

    public static class QuestionCursor extends CursorWrapper {
        public QuestionCursor(Cursor cursor) {
            super(cursor);
        }

        public Question getQuestion() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            ID id = new ID(getLong(getColumnIndex(Questions.QuestionEntry.COLUMN_NAME_GROUP_ID)),
                           getLong(getColumnIndex(Questions.QuestionEntry.COLUMN_NAME_COURSE_ID)),
                           getLong(getColumnIndex(Questions.QuestionEntry.COLUMN_NAME_ID)));
            return new Question(id,
                                getString(getColumnIndex(Questions.QuestionEntry.COLUMN_NAME_LESSON)),
                                getString(getColumnIndex(Questions.QuestionEntry.COLUMN_NAME_QUESTION)),
                                getString(getColumnIndex(Questions.QuestionEntry.COLUMN_NAME_ANSWER)),
                                getInt(getColumnIndex(Questions.QuestionEntry.COLUMN_NAME_IMAGE)) != 0);
        }
    }

    private static class Exams {
        private static final String TABLE_NAME       = "exams";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                ExamEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                ExamEntry.COLUMN_NAME_ID + TYPE_ID + COMMA_SEP +
                ExamEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + Groups.TABLE_NAME + "("
                + Groups.GroupEntry.COLUMN_NAME_ID
                + ")" + COMMA_SEP +
                ExamEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + Courses.TABLE_NAME + "("
                + Courses.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                ExamEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.EXAM_LESSON_MAX_LENGTH + ")"
                + COMMA_SEP +
                ExamEntry.COLUMN_NAME_DATE + TYPE_INT8 + COMMA_SEP +
                ExamEntry.COLUMN_NAME_CLASS + TYPE_VARCHAR + "(" + Limits.EXAM_CLASS_MAX_LENGTH + ")"
                + COMMA_SEP +
                ExamEntry.COLUMN_NAME_TYPE + TYPE_VARCHAR + "(" + Limits.EXAM_TYPE_MAX_LENGTH + ")"
                + ")";
        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;

        public static abstract class ExamEntry implements BaseColumns {
            public static final String COLUMN_NAME_ID        = "id";
            public static final String COLUMN_NAME_GROUP_ID  = "group_id";
            public static final String COLUMN_NAME_COURSE_ID = "course_id";
            public static final String COLUMN_NAME_LESSON    = "lesson";
            public static final String COLUMN_NAME_DATE      = "date";
            public static final String COLUMN_NAME_CLASS     = "class";
            public static final String COLUMN_NAME_TYPE      = "type";
        }
    }

    public static class ExamCursor extends CursorWrapper {
        Context context;

        public ExamCursor(Context c, Cursor cursor) {
            super(cursor);
            context = c;
        }

        public Exam getExam() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            ID id = new ID(getLong(getColumnIndex(Exams.ExamEntry.COLUMN_NAME_GROUP_ID)),
                           getLong(getColumnIndex(Exams.ExamEntry.COLUMN_NAME_COURSE_ID)),
                           getLong(getColumnIndex(Exams.ExamEntry.COLUMN_NAME_ID)));
            return new Exam(context,
                            id,
                            getString(getColumnIndex(Exams.ExamEntry.COLUMN_NAME_CLASS)),
                            getString(getColumnIndex(Exams.ExamEntry.COLUMN_NAME_LESSON)),
                            getString(getColumnIndex(Exams.ExamEntry.COLUMN_NAME_TYPE)),
                            new Date(getLong(getColumnIndex(Exams.ExamEntry.COLUMN_NAME_DATE))));
        }
    }

    public static class Lessons {
        private static final String TABLE_NAME       = "lessons";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + "(" +
                LessonEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                LessonEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + Groups.TABLE_NAME + "("
                + Groups.GroupEntry.COLUMN_NAME_ID
                + ")" + COMMA_SEP +
                LessonEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + Courses.TABLE_NAME + "("
                + Courses.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                LessonEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.LESSON_MAX_LENGTH + ")"
                + COMMA_SEP +
                LessonEntry.COLUMN_NAME_QUESTION_NO + TYPE_UNSIGNED + COMMA_SEP +
                LessonEntry.COLUMN_NAME_NOTE_NO + TYPE_UNSIGNED +
                ")";
        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;

        public static abstract class LessonEntry implements BaseColumns {
            public static final String COLUMN_NAME_GROUP_ID    = "group_id";
            public static final String COLUMN_NAME_COURSE_ID   = "course_id";
            public static final String COLUMN_NAME_LESSON      = "lesson";
            public static final String COLUMN_NAME_QUESTION_NO = "questions";
            public static final String COLUMN_NAME_NOTE_NO     = "notes";
        }
    }
}
