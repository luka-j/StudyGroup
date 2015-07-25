package rs.luka.android.studygroup.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;

import rs.luka.android.studygroup.io.Hider;
import rs.luka.android.studygroup.io.Retriever;

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

    public Note(ID id, String text) {
        this.id = id;
        this.text = text;
    }

    private Note(Parcel in) {
        id = in.readParcelable(Note.class.getClassLoader());
        text = in.readString();
    }

    public String getText() {
        return text;
    }

    public boolean hasImage() {
        return false;
    }

    public Bitmap getImage() {
        return Retriever.getNoteImage(id);
    }

    public void hide() {
        Hider.hideNote(id);
    }

    public void show() {
        Hider.showNote(id);
    }

    public void edit(String lesson, String text, File imageFile, File audioFile) {
        //todo
    }

    public String getHistory(Context c) {
        return Retriever.getNoteHistory(id, c);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(id, 0);
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
        if (id.itemId < another.id.itemId) { return -1; } else if (id.itemId > another.id.itemId) {
            return 1;
        }
        return 0;
    }
}
