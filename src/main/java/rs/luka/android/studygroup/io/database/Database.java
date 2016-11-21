package rs.luka.android.studygroup.io.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by luka on 28.8.15..
 */
public class Database extends SQLiteOpenHelper {
    static final String TAG           = "studygroup.Database";
    static final String DB_NAME       = "base.sqlite";
    static final int    VERSION       = 15;
    static final String DROP          = "DROP TABLE IF EXISTS ";
    static final String CREATE        = "CREATE TABLE ";
    static final String TYPE_VARCHAR  = " VARCHAR";
    static final String TYPE_INT8     = " INTEGER";
    static final String TYPE_INT4     = " INTEGER";
    static final String TYPE_INT2     = " INTEGER";
    static final String TYPE_INT1     = " INTEGER";
    static final String TYPE_UNSIGNED = " INTEGER UNSIGNED";
    static final String TYPE_NONNULL  = " NOT NULL";
    static final String TYPE_ID       = TYPE_UNSIGNED + TYPE_NONNULL;
    static final String TYPE_TEXT     = " TEXT";
    static final String COMMA_SEP     = ",";
    static final String PRIMARY       = " PRIMARY KEY";
    static final String REF           = " REFERENCES ";
    static final String INSERT        = "INSERT INTO ";
    static final String DELETE        = "DELETE FROM ";
    static final String VALS          = " VALUES ";
    static Database instance;
           final Context  context;

    private Database(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.context = context.getApplicationContext();
    }

    static Database getInstance(Context c) {
        if (instance == null) { instance = new Database(c); }
        return instance;
    }

    static Context getContext() {
        if(instance == null) return null;
        return instance.context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GroupTable.SQL_CREATE_TABLE);
        db.execSQL(CourseTable.SQL_CREATE_TABLE);
        db.execSQL(NoteTable.SQL_CREATE_TABLE);
        db.execSQL(QuestionTable.SQL_CREATE_TABLE);
        db.execSQL(ExamTable.SQL_CREATE_TABLE);
        db.execSQL(LessonTable.SQL_CREATE_TABLE);
        Log.d(TAG, "Created tables");
    }

    /**
     * Posto sluzi kao lokalna verzija baze koja se vec nalazi na serveru, nema potrebe migrirati podatke,
     * vec je lakse ponovo ih preuzeti.
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(GroupTable.SQL_DELETE_TABLE);
        db.execSQL(CourseTable.SQL_DELETE_TABLE);
        db.execSQL(NoteTable.SQL_DELETE_TABLE);
        db.execSQL(QuestionTable.SQL_DELETE_TABLE);
        db.execSQL(ExamTable.SQL_DELETE_TABLE);
        db.execSQL(LessonTable.SQL_DELETE_TABLE);
        Log.i(TAG, "Upgraded; dropped tables");
        onCreate(db);
    }
}
