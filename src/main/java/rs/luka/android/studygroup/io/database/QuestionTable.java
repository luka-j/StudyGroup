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
import rs.luka.android.studygroup.model.Question;

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
public class QuestionTable {
    static final String TABLE_NAME       = "questions";
    static final String SQL_CREATE_TABLE =
            CREATE + TABLE_NAME + " (" +
            //QuestionEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
            QuestionEntry.COLUMN_NAME_ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
            QuestionEntry.COLUMN_NAME_GROUP_ID + TYPE_ID + REF + GroupTable.TABLE_NAME + "("
            + GroupTable.GroupEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
            QuestionEntry.COLUMN_NAME_COURSE_ID + TYPE_ID + REF + CourseTable.TABLE_NAME + "("
            + CourseTable.CourseEntry.COLUMN_NAME_ID + ")" + COMMA_SEP +
            QuestionEntry.COLUMN_NAME_LESSON + TYPE_VARCHAR + "(" + Limits.LESSON_MAX_LENGTH + ")"
            + TYPE_NONNULL + COMMA_SEP +
            QuestionEntry.COLUMN_NAME_QUESTION + TYPE_TEXT + COMMA_SEP +
            QuestionEntry.COLUMN_NAME_ANSWER + TYPE_TEXT + COMMA_SEP +
            QuestionEntry.COLUMN_NAME_IMAGE + TYPE_INT1 + COMMA_SEP +
            QuestionEntry.COLUMN_NAME_ORDER + TYPE_INT4 +
            ")";
    static final String SQL_DELETE_TABLE = DROP + TABLE_NAME;
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
        public static final String COLUMN_NAME_ID        = NoteTable.NoteEntry.COLUMN_NAME_ID;
        public static final String COLUMN_NAME_GROUP_ID  = NoteTable.NoteEntry.COLUMN_NAME_GROUP_ID;
        public static final String COLUMN_NAME_COURSE_ID = NoteTable.NoteEntry.COLUMN_NAME_COURSE_ID;
        public static final String COLUMN_NAME_LESSON    = NoteTable.NoteEntry.COLUMN_NAME_LESSON;
        public static final String COLUMN_NAME_QUESTION  = "question";
        public static final String COLUMN_NAME_ANSWER    = "answer";
        public static final String COLUMN_NAME_IMAGE     = NoteTable.NoteEntry.COLUMN_NAME_IMAGE;
        public static final String COLUMN_NAME_ORDER     = NoteTable.NoteEntry.COLUMN_NAME_ORDER;
    }

    public static class QuestionCursor extends CursorWrapper {
        public QuestionCursor(Cursor cursor) {
            super(cursor);
        }

        public Question getQuestion() {
            if (isBeforeFirst() || isAfterLast()) {
                return null;
            }
            ID id = new ID(getLong(getColumnIndex(QuestionEntry.COLUMN_NAME_GROUP_ID)),
                           getLong(getColumnIndex(QuestionEntry.COLUMN_NAME_COURSE_ID)),
                           getLong(getColumnIndex(QuestionEntry.COLUMN_NAME_ID)));
            return new Question(id,
                                getString(getColumnIndex(QuestionEntry.COLUMN_NAME_LESSON)),
                                getString(getColumnIndex(QuestionEntry.COLUMN_NAME_QUESTION)),
                                getString(getColumnIndex(QuestionEntry.COLUMN_NAME_ANSWER)),
                                getInt(getColumnIndex(QuestionEntry.COLUMN_NAME_IMAGE)) != 0,
                                getInt(getColumnIndex(QuestionEntry.COLUMN_NAME_ORDER)));
        }
    }


    private Database helper;
    public QuestionTable(Context c) {
        this.helper = Database.getInstance(c);
    }

    public void insertQuestion(ID id, String lesson, String question, String answer, boolean hasImage, int order) {
        ContentValues cv = new ContentValues(5);
        cv.put(QuestionEntry.COLUMN_NAME_GROUP_ID, id.getGroupIdValue());
        cv.put(QuestionEntry.COLUMN_NAME_COURSE_ID, id.getCourseIdValue());
        cv.put(QuestionEntry.COLUMN_NAME_ID, id.getItemIdValue());
        cv.put(QuestionEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(QuestionEntry.COLUMN_NAME_QUESTION, question);
        cv.put(QuestionEntry.COLUMN_NAME_ANSWER, answer);
        cv.put(QuestionEntry.COLUMN_NAME_IMAGE, hasImage);
        if(order != 0) cv.put(QuestionEntry.COLUMN_NAME_ORDER, order);
        else           cv.put(QuestionEntry.COLUMN_NAME_ORDER, Integer.MAX_VALUE);
        SQLiteDatabase db   = helper.getWritableDatabase();
        long           code = db.insert(TABLE_NAME, null, cv);
    }

    public void updateQuestion(ID id, String lesson, String question, String answer, boolean hasImage) {
        ContentValues cv = new ContentValues(3);
        cv.put(QuestionEntry.COLUMN_NAME_LESSON, lesson);
        cv.put(QuestionEntry.COLUMN_NAME_QUESTION, question);
        cv.put(QuestionEntry.COLUMN_NAME_ANSWER, answer);
        cv.put(QuestionEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.update(TABLE_NAME,
                  cv,
                  QuestionEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                  null);
    }

    public void reorderQuestion(ID id, String lesson, int newPosition, int currentPosition) {
        SQLiteDatabase db = helper.getWritableDatabase();
        if(newPosition < currentPosition) {
            db.rawQuery("UPDATE " + TABLE_NAME + " SET " +
                        QuestionEntry.COLUMN_NAME_ORDER + " = " + QuestionEntry.COLUMN_NAME_ORDER + "+1 "
                        + "WHERE " + QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() +
                        " AND " + QuestionEntry.COLUMN_NAME_LESSON + "='" + lesson + "' AND " +
                        QuestionEntry.COLUMN_NAME_ORDER + " BETWEEN " + currentPosition + " AND " + newPosition,
                        null);
        } else if(newPosition > currentPosition) {
            db.rawQuery("UPDATE " + TABLE_NAME + " SET " +
                        QuestionEntry.COLUMN_NAME_ORDER + " = " + QuestionEntry.COLUMN_NAME_ORDER + "-1 "
                        + "WHERE " + QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + id.getCourseIdValue() +
                        " AND " + QuestionEntry.COLUMN_NAME_LESSON + "='" + lesson + "' AND " +
                        QuestionEntry.COLUMN_NAME_ORDER + " BETWEEN " + currentPosition + " AND " + newPosition,
                        null);
        }
        ContentValues cv = new ContentValues(1);
        cv.put(QuestionEntry.COLUMN_NAME_ORDER, newPosition);
        db.update(TABLE_NAME, cv, QuestionEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(), null);
    }

    public void removeQuestion(ID id, String lesson) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long code = db.delete(TABLE_NAME,
                              QuestionEntry.COLUMN_NAME_ID + "=" + id.getItemIdValue(),
                              null);
    }

    public void clearQuestions(long courseId, String lesson) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(TABLE_NAME,
                  QuestionEntry.COLUMN_NAME_COURSE_ID + "=" + courseId + " AND " +
                  QuestionEntry.COLUMN_NAME_LESSON + "='" + lesson + "'",
                  null);
    }

    public void insertQuestions(Question[] questions) {
        SQLiteDatabase  db   = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(SQL_INSERT);
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
        SQLiteDatabase db = helper.getReadableDatabase();
        QuestionCursor c = new QuestionCursor(db.query(TABLE_NAME,
                                                       null,
                                                       QuestionEntry.COLUMN_NAME_COURSE_ID
                                                       + "=" + courseId.getCourseIdValue() +
                                                       " AND "
                                                       + NoteTable.NoteEntry.COLUMN_NAME_LESSON + "='"
                                                       + lesson + "'",
                                                       null,
                                                       null,
                                                       null,
                                                       QuestionEntry.COLUMN_NAME_ORDER + " asc"));
        return c;
    }

    public Question queryQuestion(ID questionId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        QuestionCursor c = new QuestionCursor(db.query(TABLE_NAME,
                                               null,
                                               QuestionEntry.COLUMN_NAME_ID
                                               + "=" + questionId.getItemIdValue(),
                                               null,
                                               null,
                                               null,
                                               null,
                                               "1"));
        c.moveToNext();
        return c.getQuestion();
    }
}
