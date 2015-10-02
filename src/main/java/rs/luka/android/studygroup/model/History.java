package rs.luka.android.studygroup.model;

import java.util.Date;

/**
 * Created by luka on 7.9.15..
 */
public class History {
    private final String author;
    private final Date   date;
    private final String prev;

    public History(long author, Date date, String prev) {
        this.author = null; //getUserByID
        this.date = date;
        this.prev = prev;
    }

    public History(String author, Date date, String prev) {
        this.author = author;
        this.date = date;
        this.prev = prev;
    }

    public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }

    public String getPrev() {
        return prev;
    }
}
