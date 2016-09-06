package rs.luka.android.studygroup.io;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Date;
import java.util.List;

import rs.luka.android.studygroup.misc.Utils;
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
    private static final int    VERSION       = 15;
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
    private static final String INSERT        = "INSERT INTO ";
    private static final String DELETE        = "DELETE FROM ";
    private static final String VALS          = " VALUES ";
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

    public void insertGroup(ID id, String name, String place, boolean hasImage, int permission) {
        ContentValues cv = new ContentValues(4);
        cv.put(Groups.GroupEntry.COLUMN_NAME_ID, id.getGroupIdValue());
        cv.put(Groups.GroupEntry.COLUMN_NAME_SCHOOL, name);
        cv.put(Groups.GroupEntry.COLUMN_NAME_PLACE, place);
        cv.put(Groups.GroupEntry.COLUMN_NAME_IMAGE, hasImage);
        cv.put(Groups.GroupEntry.COLUMN_NAME_PERMISSION, permission);
        SQLiteDatabase db   = getWritableDatabase();
        long           code = db.insert(Groups.TABLE_NAME, null, cv);
    }

    public void updateGroup(ID id, String name, String place, boolean hasImage) {
        ContentValues cv = new ContentValues(3);
        cv.put(Groups.GroupEntry.COLUMN_NAME_SCHOOL, name);
        cv.put(Groups.GroupEntry.COLUMN_NAME_PLACE, place);
        cv.put(Groups.GroupEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db = getWritableDatabase();
        long code = db.update(Groups.TABLE_NAME,
                              cv,
                              Groups.GroupEntry.COLUMN_NAME_ID + "=" + id.getGroupIdValue(),
                              null);
    }

    public void updateFilteringData(long id, List<Integer> allYears, List<Integer> filteringYears) {
        ContentValues cv = new ContentValues(2);
        cv.put(Groups.GroupEntry.COLUMN_NAME_YEARS, Utils.listToString(allYears));
        cv.put(Groups.GroupEntry.COLUMN_NAME_FILTERING, Utils.listToString(filteringYears));
        SQLiteDatabase db = getWritableDatabase();
        long code = db.update(Groups.TABLE_NAME,
                              cv,
                              Groups.GroupEntry.COLUMN_NAME_ID + "=" + id,
                              null);
    }

    public void removeGroup(ID id) {
        SQLiteDatabase db = getWritableDatabase();
        long code = db.delete(Groups.TABLE_NAME,
                              Groups.GroupEntry.COLUMN_NAME_ID + "=" + id.getGroupIdValue(),
                              null);
    }

    public void clearGroups() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(Groups.SQL_REMOVE_ENTRIES);
    }

    public void insertGroups(Group[] groups) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(Groups.SQL_INSERT);
        db.beginTransaction();

        for (Group group : groups) {
            stmt.bindLong(1, group.getIdValue());
            stmt.bindString(2, group.getName());
            stmt.bindString(3, group.getPlace());
            stmt.bindLong(4, group.hasImage() ? 1 : 0);
            stmt.bindString(5, Utils.listToString(group.getCourseYears()));
            stmt.bindLong(6, group.getPermission());
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
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

    /**
     * Like removeCourse, but doesn't remove lessons
     * @param id
     */
    public void hideCourse(ID id) {
        SQLiteDatabase db = getWritableDatabase();
        long code = db.delete(Courses.TABLE_NAME,
                               Courses.CourseEntry.COLUMN_NAME_ID + "=" + id.getCourseIdValue(),
                              null);
        Log.i(TAG, "hideCourse status: " + code);
    }

    public void clearCourses(long groupId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Courses.TABLE_NAME,
                  Courses.CourseEntry.COLUMN_NAME_GROUP_ID + "=" + groupId,
                  null);
    }

    public void insertCourses(Course[] courses) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(Courses.SQL_INSERT);
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

    public Group queryGroup(ID groupId) {
        SQLiteDatabase db = getReadableDatabase();
        GroupCursor c = new GroupCursor(db.query(Groups.TABLE_NAME,
                                                 null,
                                                 Groups.GroupEntry.COLUMN_NAME_ID
                                                 + "=" + groupId.getGroupIdValue(),
                                                 null,
                                                 null,
                                                 null,
                                                 null,
                                                 "1"));
        c.moveToNext();
        return c.getGroup();
    }

    public Cursor queryLessons(ID courseId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = new LessonCursor(db.query(true,
                                             Lessons.TABLE_NAME,
                                             null,
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

    public void clearLessons(long courseId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Lessons.TABLE_NAME,
                  Lessons.LessonEntry.COLUMN_NAME_COURSE_ID + "=" + courseId,
                  null);
    }

    public void insertLessons(long courseId, rs.luka.android.studygroup.network.Lessons.Lesson[] lessons) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(Lessons.SQL_INSERT);
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
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Lessons.TABLE_NAME,
                  Lessons.LessonEntry.COLUMN_NAME_COURSE_ID + "=" + courseId.getCourseIdValue() + " AND "
                  + Lessons.LessonEntry.COLUMN_NAME_LESSON + "='" + lesson + "'",
                  null);
    }

    public void showLesson(ID courseId, int _id, String lesson, int noteCount, int questionCount) {
        ContentValues cv = new ContentValues(6);
        cv.put(Lessons.LessonEntry._ID, _id);
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

    public void insertNote(ID id, String lesson, String text, boolean hasImage, boolean hasAudio, long order) {
        ContentValues cv = new ContentValues(5);
        cv.put(Notes.NoteEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(Notes.NoteEntry.COLUMN_NAME_COURSE_ID, id.getCourseIdValue());
        cv.put(Notes.NoteEntry.COLUMN_NAME_ID, id.getItemIdValue());
        cv.put(Notes.NoteEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Notes.NoteEntry.COLUMN_NAME_TEXT, text);
        cv.put(Notes.NoteEntry.COLUMN_NAME_IMAGE, hasImage);
        cv.put(Notes.NoteEntry.COLUMN_NAME_AUDIO, hasAudio);
        if(order != 0)
            cv.put(Notes.NoteEntry.COLUMN_NAME_ORDER, order);
        else
            cv.put(Notes.NoteEntry.COLUMN_NAME_ORDER, Integer.MAX_VALUE);
        SQLiteDatabase db   = getWritableDatabase();
        long           code = db.insert(Notes.TABLE_NAME, null, cv);
    }

    public void updateNote(ID id, String lesson, String text, boolean hasImage, boolean hasAudio) {
        ContentValues cv = new ContentValues(4);
        cv.put(Notes.NoteEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Notes.NoteEntry.COLUMN_NAME_TEXT, text);
        cv.put(Notes.NoteEntry.COLUMN_NAME_IMAGE, hasImage);
        cv.put(Notes.NoteEntry.COLUMN_NAME_AUDIO, hasAudio);
        SQLiteDatabase db = getWritableDatabase();
        db.update(Notes.TABLE_NAME, cv, Notes.NoteEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(), null);
    }

    public void reorderNote(ID id, String lesson, int newPosition, int currentPosition) { //todo fix occasional off-by-1
        SQLiteDatabase db = getWritableDatabase();
        if(newPosition < currentPosition) {
            db.rawQuery("UPDATE " + Notes.TABLE_NAME + " SET " +
                        Notes.NoteEntry.COLUMN_NAME_ORDER + " = " + Notes.NoteEntry.COLUMN_NAME_ORDER + "+1 "
                        + "WHERE " + Notes.NoteEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() +
                        " AND " + Notes.NoteEntry.COLUMN_NAME_LESSON + "='" + lesson + "' AND " +
                        Notes.NoteEntry.COLUMN_NAME_ORDER + " BETWEEN " + currentPosition + " AND " + newPosition,
                        null);
        } else if(newPosition > currentPosition) {
            db.rawQuery("UPDATE " + Notes.TABLE_NAME + " SET " +
                        Notes.NoteEntry.COLUMN_NAME_ORDER + " = " + Notes.NoteEntry.COLUMN_NAME_ORDER + "-1 "
                        + "WHERE " + Notes.NoteEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() +
                        " AND " + Notes.NoteEntry.COLUMN_NAME_LESSON + "='" + lesson + "' AND " +
                        Notes.NoteEntry.COLUMN_NAME_ORDER + " BETWEEN " + currentPosition + " AND " + newPosition,
                        null);
        }
        ContentValues cv = new ContentValues(1);
        cv.put(Notes.NoteEntry.COLUMN_NAME_ORDER, newPosition);
        db.update(Notes.TABLE_NAME, cv, Notes.NoteEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(), null);
    }

    public void removeNote(ID id, String lesson) {
        SQLiteDatabase db = getWritableDatabase();
        long code = db.delete(Notes.TABLE_NAME,
                              Notes.NoteEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                              null);
    }

    public void clearNotes(long courseId, String lesson) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Notes.TABLE_NAME,
                  Notes.NoteEntry.COLUMN_NAME_COURSE_ID + "=" + courseId + " AND " +
                  Notes.NoteEntry.COLUMN_NAME_LESSON + "='" + lesson + "'",
                  null);
    }

    public void insertNotes(Note[] notes) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(Notes.SQL_INSERT);
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
                                               Notes.NoteEntry.COLUMN_NAME_ORDER + " asc"));
        return c;
    }

    public void insertQuestion(ID id, String lesson, String question, String answer, boolean hasImage, int order) {
        ContentValues cv = new ContentValues(5);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(Questions.QuestionEntry.COLUMN_NAME_COURSE_ID, id.getCourseIdValue());
        cv.put(Questions.QuestionEntry.COLUMN_NAME_ID, id.getItemIdValue());
        cv.put(Questions.QuestionEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_QUESTION, question);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_ANSWER, answer);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_IMAGE, hasImage);
        if(order != 0) cv.put(Questions.QuestionEntry.COLUMN_NAME_ORDER, order);
        else           cv.put(Questions.QuestionEntry.COLUMN_NAME_ORDER, Integer.MAX_VALUE);
        SQLiteDatabase db   = getWritableDatabase();
        long           code = db.insert(Questions.TABLE_NAME, null, cv);
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

    public void reorderQuestion(ID id, String lesson, int newPosition, int currentPosition) {
        SQLiteDatabase db = getWritableDatabase();
        if(newPosition < currentPosition) {
            db.rawQuery("UPDATE " + Questions.TABLE_NAME + " SET " +
                        Questions.QuestionEntry.COLUMN_NAME_ORDER + " = " + Questions.QuestionEntry.COLUMN_NAME_ORDER + "+1 "
                        + "WHERE " + Questions.QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() +
                        " AND " + Questions.QuestionEntry.COLUMN_NAME_LESSON + "='" + lesson + "' AND " +
                        Questions.QuestionEntry.COLUMN_NAME_ORDER + " BETWEEN " + currentPosition + " AND " + newPosition,
                        null);
        } else if(newPosition > currentPosition) {
            db.rawQuery("UPDATE " + Questions.TABLE_NAME + " SET " +
                        Questions.QuestionEntry.COLUMN_NAME_ORDER + " = " + Questions.QuestionEntry.COLUMN_NAME_ORDER + "-1 "
                        + "WHERE " + Questions.QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() +
                        " AND " + Questions.QuestionEntry.COLUMN_NAME_LESSON + "='" + lesson + "' AND " +
                        Questions.QuestionEntry.COLUMN_NAME_ORDER + " BETWEEN " + currentPosition + " AND " + newPosition,
                        null);
        }
        ContentValues cv = new ContentValues(1);
        cv.put(Questions.QuestionEntry.COLUMN_NAME_ORDER, newPosition);
        db.update(Questions.TABLE_NAME, cv, Questions.QuestionEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(), null);
    }

    public void removeQuestion(ID id, String lesson) {
        SQLiteDatabase db = getWritableDatabase();
        long code = db.delete(Questions.TABLE_NAME,
                              Questions.QuestionEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                              null);
    }

    public void clearQuestions(long courseId, String lesson) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Questions.TABLE_NAME,
                  Questions.QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + courseId + " AND " +
                  Questions.QuestionEntry.COLUMN_NAME_LESSON + "='" + lesson + "'",
                  null);
    }

    public void insertQuestions(Question[] questions) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(Questions.SQL_INSERT);
        db.beginTransaction();

        for (Question question : questions) {
            stmt.bindLong(1, question.getIdValue());      //id
            stmt.bindLong(2, question.getGroupIdValue()); //group id
            stmt.bindLong(3, question.getCourseIdValue());//courseId
            stmt.bindString(4, question.getLesson());     //lesson
            stmt.bindString(5, question.getQuestion());   //question
            stmt.bindString(6, question.getAnswer());     //question
            stmt.bindLong(7, question.hasImage()?1:0);    //hasImage
            stmt.bindLong(8, question.getOrder());
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public QuestionCursor queryQuestions(ID courseId, String lesson) {
        if (lesson.isEmpty()) { return null; }
        SQLiteDatabase db = getReadableDatabase();
        QuestionCursor c = new QuestionCursor(db.query(Questions.TABLE_NAME,
                                                       null,
                                                       Questions.QuestionEntry.COLUMN_NAME_COURSE_ID
                                                       + "=" + courseId.getCourseIdValue() +
                                                       " AND "
                                                       + Notes.NoteEntry.COLUMN_NAME_LESSON + "='"
                                                       + lesson + "'",
                                                       null,
                                                       null,
                                                       null,
                                                       Questions.QuestionEntry.COLUMN_NAME_ORDER + " asc"));
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

    public void clearExams(long groupId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Exams.TABLE_NAME,
                  Exams.ExamEntry.COLUMN_NAME_GROUP_ID + "=" + groupId,
                  null);
    }

    public void insertExams(Exam[] exams) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(Exams.SQL_INSERT);
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
                                                        + "=" + groupId.getGroupIdValue(),
                                                        null,
                                                        null,
                                                        null,
                                                        Exams.ExamEntry.COLUMN_NAME_ID
                                                        + " asc"));
        return c;
    }

    private static class Groups {
        private static final String TABLE_NAME       = "groups";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                //GroupEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                GroupEntry.COLUMN_NAME_ID + TYPE_INT8 + PRIMARY  + COMMA_SEP +
                GroupEntry.COLUMN_NAME_SCHOOL + TYPE_VARCHAR + "(" + Limits.GROUP_NAME_MAX_LENGTH + ")"
                + COMMA_SEP +
                GroupEntry.COLUMN_NAME_PLACE + TYPE_VARCHAR + "(" + Limits.GROUP_PLACE_MAX_LENGTH + ")" + COMMA_SEP +
                GroupEntry.COLUMN_NAME_IMAGE + TYPE_INT1 + COMMA_SEP +
                GroupEntry.COLUMN_NAME_YEARS + TYPE_TEXT + COMMA_SEP +
                GroupEntry.COLUMN_NAME_FILTERING + TYPE_TEXT + COMMA_SEP +
                GroupEntry.COLUMN_NAME_PERMISSION + TYPE_INT4 +
                ")";

        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;
        private static final String SQL_REMOVE_ENTRIES = DELETE + TABLE_NAME;
        /**
         * Order: id, name, place, allYears, hasImage
         */
        private static final String SQL_INSERT = INSERT + TABLE_NAME + " (" +
                                                 GroupEntry.COLUMN_NAME_ID + COMMA_SEP +
                                                 GroupEntry.COLUMN_NAME_SCHOOL + COMMA_SEP +
                                                 GroupEntry.COLUMN_NAME_PLACE + COMMA_SEP +
                                                 GroupEntry.COLUMN_NAME_IMAGE + COMMA_SEP +
                                                 GroupEntry.COLUMN_NAME_YEARS + COMMA_SEP +
                                                 GroupEntry.COLUMN_NAME_PERMISSION  +
                                                 ")" + VALS + "(?, ?, ?, ?, ?, ?)";

        public Groups() {}

        public static abstract class GroupEntry implements BaseColumns {
            public static final String COLUMN_NAME_ID         = _ID;
            public static final String COLUMN_NAME_SCHOOL     = "name";
            public static final String COLUMN_NAME_PLACE      = "place";
            public static final String COLUMN_NAME_IMAGE      = "image_exists";
            public static final String COLUMN_NAME_YEARS      = "course_years";
            public static final String COLUMN_NAME_FILTERING  = "filtering_years";
            public static final String COLUMN_NAME_PERMISSION = "permission";
        }
    }

    public static class GroupCursor extends CursorWrapper {

        public GroupCursor(Cursor cursor) {
            super(cursor);
        }

        public Group getGroup() {
            if (isBeforeFirst() || isAfterLast()) { return null; }
            ID id = new ID(getLong(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_ID)));
            Group ret =  new Group(id, getString(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_SCHOOL)),
                             getString(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_PLACE)),
                             getInt(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_IMAGE))!=0,
                             Utils.stringToList(getString(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_YEARS))),
                             getInt(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_PERMISSION)));
            ret.setFiltering(Utils.stringToList(getString(getColumnIndex(Groups.GroupEntry.COLUMN_NAME_FILTERING))));
            return ret;
        }
    }

    private static class Courses {
        private static final String TABLE_NAME       = "courses";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                //CourseEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                CourseEntry.COLUMN_NAME_ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
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
        /**
         * Order: id, groupId, subject, teacher, year, image
         */
        private static final String SQL_INSERT = INSERT + TABLE_NAME + " (" +
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

        public int getRequiredPermission() {
            if (isBeforeFirst() || isAfterLast()) {
                return -1;
            }
            return getInt(getColumnIndex(Lessons.LessonEntry.COLUMN_NAME_PERMISSION)); //todo fix (returns -1 as column index)
            //todo AddQuestionActivity private checkbox, adding private questions
        }
    }

    private static class Notes {
        private static final String TABLE_NAME       = "notes";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                //NoteEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                NoteEntry.COLUMN_NAME_ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                NoteEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + Groups.TABLE_NAME + "("
                + Groups.GroupEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                NoteEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + Courses.TABLE_NAME + "("
                + Courses.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                NoteEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.LESSON_MAX_LENGTH + ")"
                + TYPE_NONNULL + COMMA_SEP +
                NoteEntry.COLUMN_NAME_TEXT + TYPE_TEXT + COMMA_SEP +
                NoteEntry.COLUMN_NAME_IMAGE + TYPE_INT1 + COMMA_SEP +
                NoteEntry.COLUMN_NAME_AUDIO + TYPE_INT1 + COMMA_SEP +
                NoteEntry.COLUMN_NAME_ORDER + TYPE_INT4 +
                ")";

        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;
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
                            getInt(getColumnIndex(Notes.NoteEntry.COLUMN_NAME_AUDIO)) != 0,
                            getInt(getColumnIndex(Notes.NoteEntry.COLUMN_NAME_ORDER)));
        }
    }

    private static class Questions {
        private static final String TABLE_NAME       = "questions";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                //QuestionEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + Groups.TABLE_NAME + "("
                + Groups.GroupEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + Courses.TABLE_NAME + "("
                + Courses.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.LESSON_MAX_LENGTH + ")"
                + TYPE_NONNULL + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_QUESTION + TYPE_TEXT + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_ANSWER + TYPE_TEXT + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_IMAGE + TYPE_INT1 + COMMA_SEP +
                QuestionEntry.COLUMN_NAME_ORDER + TYPE_INT4 +
                ")";
        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;
        /**
         * Order: id, groupId, courseId, lesson, question, answer, image
         */
        private static final String SQL_INSERT = INSERT + TABLE_NAME + " (" +
                                                 QuestionEntry.COLUMN_NAME_ID + COMMA_SEP +
                                                 QuestionEntry.COLUMN_NAME_GROUP_ID + COMMA_SEP +
                                                 QuestionEntry.COLUMN_NAME_COURSE_ID + COMMA_SEP +
                                                 QuestionEntry.COLUMN_NAME_LESSON + COMMA_SEP +
                                                 QuestionEntry.COLUMN_NAME_QUESTION + COMMA_SEP +
                                                 QuestionEntry.COLUMN_NAME_ANSWER + COMMA_SEP +
                                                 QuestionEntry.COLUMN_NAME_IMAGE + COMMA_SEP +
                                                 QuestionEntry.COLUMN_NAME_ORDER +
                                                 ")" + VALS + "(?, ?, ?, ?, ?, ?, ?, ?)";

        public static abstract class QuestionEntry implements BaseColumns {
            public static final String COLUMN_NAME_ID        = Notes.NoteEntry.COLUMN_NAME_ID;
            public static final String COLUMN_NAME_GROUP_ID  = Notes.NoteEntry.COLUMN_NAME_GROUP_ID;
            public static final String COLUMN_NAME_COURSE_ID = Notes.NoteEntry.COLUMN_NAME_COURSE_ID;
            public static final String COLUMN_NAME_LESSON    = Notes.NoteEntry.COLUMN_NAME_LESSON;
            public static final String COLUMN_NAME_QUESTION  = "question";
            public static final String COLUMN_NAME_ANSWER    = "answer";
            public static final String COLUMN_NAME_IMAGE     = Notes.NoteEntry.COLUMN_NAME_IMAGE;
            public static final String COLUMN_NAME_ORDER     = Notes.NoteEntry.COLUMN_NAME_ORDER;
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
                                getInt(getColumnIndex(Questions.QuestionEntry.COLUMN_NAME_IMAGE)) != 0,
                                getInt(getColumnIndex(Questions.QuestionEntry.COLUMN_NAME_ORDER)));
        }
    }

    private static class Exams {
        private static final String TABLE_NAME       = "exams";
        private static final String SQL_CREATE_TABLE =
                CREATE + TABLE_NAME + " (" +
                //ExamEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
                ExamEntry.COLUMN_NAME_ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
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
    }

    public static class ExamCursor extends CursorWrapper {
        private Context context;

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
                LessonEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + Courses.TABLE_NAME + "("
                + Courses.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
                LessonEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.LESSON_MAX_LENGTH + ")"
                + COMMA_SEP +
                LessonEntry.COLUMN_NAME_QUESTION_NO + TYPE_UNSIGNED + COMMA_SEP +
                LessonEntry.COLUMN_NAME_NOTE_NO + TYPE_UNSIGNED + COMMA_SEP +
                LessonEntry.COLUMN_NAME_PERMISSION + TYPE_INT4 +
                ")";
        private static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;

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
    }
}
