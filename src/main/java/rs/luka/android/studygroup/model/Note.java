package rs.luka.android.studygroup.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.text.DateFormat;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;

/**
 * Created by luka on 2.7.15..
 */
public class Note implements Parcelable, Comparable<Note> {
    public static final Parcelable.Creator<Note> CREATOR
            = new Parcelable.Creator<Note>() {
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
    private final ID id;
    private final String text;
    private final String lesson;

    public Note(ID id, String lesson, String text) {
        this.id = id;
        this.text = text;
        this.lesson = lesson;
    }

    private Note(Parcel in) {
        id = in.readParcelable(Note.class.getClassLoader());
        lesson = in.readString();
        text = in.readString();
    }

    public String getText() {
        return text;
    }

    public boolean hasImage() {
        return false;
    }

    public Bitmap getImage(Context c) {
        return DataManager.getImage(c, id);
    }

    public void hide(Context c) {
        DataManager.removeNote(c, id, lesson);
    }

    public void show(Context c) {
        Database.getInstance(c).insertNote(id, lesson, text);
    }

    public void edit(Context c, String lesson, String text, File imageFile, File audioFile) {
        DataManager.editNote(c, id, lesson, text, imageFile, audioFile);
    }

    public String getHistory(Context c) {
        History h = DataManager.getHistory(c, id);
        return c.getString(R.string.note_history, h.get(0).getAuthor(),
                           DateFormat.getDateTimeInstance().format(h.get(0).getDate()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(id, 0);
        dest.writeString(lesson);
        dest.writeString(text);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Note && ((Note) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NonNull Note another) {
        return id.compareTo(another.id);
    }
}
