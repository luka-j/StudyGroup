package rs.luka.android.studygroup.model;

import java.util.Date;
import java.util.LinkedList;

/**
 * Created by luka on 7.9.15..
 */
public class History extends LinkedList<History.HistoryEntry> {
    public static class HistoryEntry {
        private final String author;
        private final Date   date;
        private final String prev;

        public HistoryEntry(ID author, Date date, String prev) {
            this.author = null; //getUserByID
            this.date = date;
            this.prev = prev;
        }

        public HistoryEntry(String author, Date date, String prev) {
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
}
