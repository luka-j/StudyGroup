package rs.luka.android.studygroup.model;

import android.content.Context;
import android.text.format.DateUtils;

import rs.luka.android.studygroup.R;

/**
 * Created by luka on 2.2.16..
 */
public class Edit {

    public static final int ACTION_EDIT_TEXT = 1;
    public static final int ACTION_REPLACE_IMAGE = 2;
    public static final int ACTION_ADD_IMAGE = 3;
    public static final int ACTION_REMOVE_IMAGE = 4;
    public static final int ACTION_CHANGE_LESSON = 5;
    public static final int ACTION_CHANGE_DATE = 6;
    public static final int ACTION_CHANGE_TYPE = 7;
    public static final int ACTION_ADD_AUDIO = 8;
    public static final int ACTION_REMOVE_AUDIO = 9;
    public static final int ACTION_REPLACE_AUDIO = 10;

    private final User editor;
    private final int action;
    private final long time;

    public Edit(User editor, int action, long time) {
        this.editor = editor;
        this.action = action;
        this.time = time;
    }

    public String getUserName() {
        return editor.getName();
    }

    public String getLocalizedAction(Context c) {
        switch (action) {
            case ACTION_EDIT_TEXT: return c.getString(R.string.edit_edit_text);
            case ACTION_REPLACE_IMAGE: return c.getString(R.string.edit_replace_image);
            case ACTION_ADD_IMAGE: return c.getString(R.string.edit_add_image);
            case ACTION_REMOVE_IMAGE: return c.getString(R.string.edit_remove_image);
            case ACTION_CHANGE_LESSON: return c.getString(R.string.edit_change_lesson);
            case ACTION_CHANGE_DATE: return c.getString(R.string.edit_change_date);
            case ACTION_CHANGE_TYPE: return c.getString(R.string.edit_change_type);
            case ACTION_ADD_AUDIO: return c.getString(R.string.edit_add_audio);
            case ACTION_REMOVE_AUDIO: return c.getString(R.string.edit_remove_audio);
            case ACTION_REPLACE_AUDIO: return c.getString(R.string.edit_replace_audio);
            default: return c.getString(R.string.edit_unknown);
        }
    }

    public String getLocalizedDate(Context c) {
        return DateUtils.formatDateTime(c, time, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                                                 DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_NO_YEAR);
    }
}
