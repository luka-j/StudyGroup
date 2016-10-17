package rs.luka.android.studygroup.io.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.provider.BaseColumns;

import java.util.List;

import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 15.10.16..
 */
public class GroupTable {
    static final String TABLE_NAME       = "groups";
    static final String SQL_CREATE_TABLE =
            Database.CREATE + TABLE_NAME + " (" +
            //GroupEntry._ID + TYPE_INT8 + PRIMARY + COMMA_SEP +
            GroupEntry.COLUMN_NAME_ID + Database.TYPE_INT8 + Database.PRIMARY + Database.COMMA_SEP +
            GroupEntry.COLUMN_NAME_SCHOOL + Database.TYPE_VARCHAR + "(" + Limits.GROUP_NAME_MAX_LENGTH + ")"
            + Database.COMMA_SEP +
            GroupEntry.COLUMN_NAME_PLACE + Database.TYPE_VARCHAR + "(" + Limits.GROUP_PLACE_MAX_LENGTH + ")" + Database.COMMA_SEP +
            GroupEntry.COLUMN_NAME_IMAGE + Database.TYPE_INT1 + Database.COMMA_SEP +
            GroupEntry.COLUMN_NAME_YEARS + Database.TYPE_TEXT + Database.COMMA_SEP +
            GroupEntry.COLUMN_NAME_FILTERING + Database.TYPE_TEXT + Database.COMMA_SEP +
            GroupEntry.COLUMN_NAME_PERMISSION + Database.TYPE_INT4 +
            ")";

    static final String SQL_DELETE_TABLE = Database.DROP + TABLE_NAME;
    static final String SQL_REMOVE_ENTRIES = Database.DELETE + TABLE_NAME;
    /**
     * Order: id, name, place, allYears, hasImage
     */
    private static final String SQL_INSERT = Database.INSERT + TABLE_NAME + " (" +
                                             GroupEntry.COLUMN_NAME_ID + Database.COMMA_SEP +
                                             GroupEntry.COLUMN_NAME_SCHOOL + Database.COMMA_SEP +
                                             GroupEntry.COLUMN_NAME_PLACE + Database.COMMA_SEP +
                                             GroupEntry.COLUMN_NAME_IMAGE + Database.COMMA_SEP +
                                             GroupEntry.COLUMN_NAME_YEARS + Database.COMMA_SEP +
                                             GroupEntry.COLUMN_NAME_PERMISSION +
                                             ")" + Database.VALS + "(?, ?, ?, ?, ?, ?)";

    public GroupTable() {}

    public static abstract class GroupEntry implements BaseColumns {
        public static final String COLUMN_NAME_ID         = _ID;
        public static final String COLUMN_NAME_SCHOOL     = "name";
        public static final String COLUMN_NAME_PLACE      = "place";
        public static final String COLUMN_NAME_IMAGE      = "image_exists";
        public static final String COLUMN_NAME_YEARS      = "course_years";
        public static final String COLUMN_NAME_FILTERING  = "filtering_years";
        public static final String COLUMN_NAME_PERMISSION = "permission";
    }

    public static class GroupCursor extends CursorWrapper {

        public GroupCursor(Cursor cursor) {
            super(cursor);
        }

        public Group getGroup() {
            if (isBeforeFirst() || isAfterLast()) { return null; }
            ID id = new ID(getLong(getColumnIndex(GroupEntry.COLUMN_NAME_ID)));
            Group ret =  new Group(id, getString(getColumnIndex(GroupEntry.COLUMN_NAME_SCHOOL)),
                                   getString(getColumnIndex(GroupEntry.COLUMN_NAME_PLACE)),
                                   getInt(getColumnIndex(GroupTable.GroupEntry.COLUMN_NAME_IMAGE)) != 0,
                                   Utils.stringToList(getString(getColumnIndex(GroupTable.GroupEntry.COLUMN_NAME_YEARS))),
                                   getInt(getColumnIndex(GroupTable.GroupEntry.COLUMN_NAME_PERMISSION)));
            ret.setFiltering(Utils.stringToList(getString(getColumnIndex(GroupTable.GroupEntry.COLUMN_NAME_FILTERING))));
            return ret;
        }
    }

    private Database helper;
    public GroupTable(Context c) {
        helper = Database.getInstance(c);
    }

    public void insertGroup(ID id, String name, String place, boolean hasImage, int permission) {
        ContentValues cv = new ContentValues(4);
        cv.put(GroupEntry.COLUMN_NAME_ID, id.getGroupIdValue());
        cv.put(GroupEntry.COLUMN_NAME_SCHOOL, name);
        cv.put(GroupEntry.COLUMN_NAME_PLACE, place);
        cv.put(GroupEntry.COLUMN_NAME_IMAGE, hasImage);
        cv.put(GroupEntry.COLUMN_NAME_PERMISSION, permission);
        SQLiteDatabase db = this.helper.getWritableDatabase();
        long           code = db.insert(TABLE_NAME, null, cv);
    }

    public void updateGroup(ID id, String name, String place, boolean hasImage) {
        ContentValues cv = new ContentValues(3);
        cv.put(GroupEntry.COLUMN_NAME_SCHOOL, name);
        cv.put(GroupEntry.COLUMN_NAME_PLACE, place);
        cv.put(GroupEntry.COLUMN_NAME_IMAGE, hasImage);
        SQLiteDatabase db = this.helper.getWritableDatabase();
        long code = db.update(TABLE_NAME, cv, GroupEntry.COLUMN_NAME_ID + "=" + id.getGroupIdValue(), null);
    }

    public void updateFilteringData(long id, List<Integer> allYears, List<Integer> filteringYears) {
        ContentValues cv = new ContentValues(2);
        cv.put(GroupEntry.COLUMN_NAME_YEARS, Utils.listToString(allYears));
        cv.put(GroupEntry.COLUMN_NAME_FILTERING, Utils.listToString(filteringYears));
        SQLiteDatabase db = Database.instance.getWritableDatabase();
        long code = db.update(TABLE_NAME, cv, GroupEntry.COLUMN_NAME_ID + "=" + id, null);
    }

    public void removeGroup(ID id) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        long code = db.delete(TABLE_NAME, GroupEntry.COLUMN_NAME_ID + "=" + id.getGroupIdValue(), null);
    }

    public void clearGroups() {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(GroupTable.SQL_REMOVE_ENTRIES);
    }

    public void insertGroups(Group[] groups) {
        SQLiteDatabase  db   = this.helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement(SQL_INSERT);
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
        GroupCursor c = new GroupCursor(helper.getReadableDatabase().query(TABLE_NAME,
                                                                           null,
                                                                           null,
                                                                           null,
                                                                           null,
                                                                           null,
                                                 GroupEntry.COLUMN_NAME_ID + " asc"));
        return c;
    }

    public Group queryGroup(ID groupId) {
        GroupCursor c = new GroupCursor(helper.getReadableDatabase().query(TABLE_NAME,
                                                                           null,
                                                 GroupEntry.COLUMN_NAME_ID
                                                 + "=" + groupId.getGroupIdValue(),
                                                                           null,
                                                                           null,
                                                                           null,
                                                                           null,
                                                                           "1"));
        c.moveToNext();
        return c.getGroup();
    }

    public long getGroupCount() {
        return DatabaseUtils.queryNumEntries(helper.getReadableDatabase(), GroupTable.TABLE_NAME);
    }
}
