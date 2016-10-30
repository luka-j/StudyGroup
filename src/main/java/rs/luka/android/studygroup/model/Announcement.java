package rs.luka.android.studygroup.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by luka on 29.10.16..
 */
public class Announcement {
    public static final String JSON_KEY_ID    = "id";
    public static final String JSON_KEY_TEXT  = "text";
    public static final String JSON_KEY_DATE  = "date";
    public static final String JSON_KEY_YEARS = "years";

    private long   id;
    private String text;
    private Date   date;
    private String years;

    public Announcement(JSONObject from) throws JSONException {
        this.id = from.getLong(JSON_KEY_ID);
        this.text = from.getString(JSON_KEY_TEXT);
        this.date = new Date(from.getLong(JSON_KEY_DATE));
        String commaYears = from.getString(JSON_KEY_YEARS);
        years = commaYears.substring(1, commaYears.length()-1).replaceAll(",", ", ");
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public String getYears() {
        return years;
    }
}
