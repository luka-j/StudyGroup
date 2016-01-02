package rs.luka.android.studygroup.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;

import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.io.LocalImages;

/**
 * Created by luka on 2.7.15..
 */
public class Note implements Parcelable, Comparable<Note>, PastEvents {
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
    private final boolean imageExists;
    private final boolean audioExists;

    public Note(ID id, String lesson, String text, boolean imageExists, boolean audioExists) {
        this.id = id;
        this.text = text;
        this.lesson = lesson;
        this.imageExists = imageExists;
        this.audioExists = audioExists;
    }

    private Note(Parcel in) {
        id = in.readParcelable(Note.class.getClassLoader());
        lesson = in.readString();
        text = in.readString();
        imageExists = in.readInt()!=0;
        audioExists = in.readInt()!=0;
    }

    public String getText() {
        return text;
    }

    public boolean hasImage() {
        return imageExists;
    }

    public boolean hasAudio() {
        return audioExists;
    }

    public Uri getAudioPath(String courseName) {
        return Uri.fromFile(DataManager.getAudio(id, courseName, lesson));
    }

    /**
     * Vraća gde <em>bi trebalo</em> da se nalazi slika (ne proverava da li ona zaista postoji)
     *
     * @param courseName ime predmeta (potrebno za folder)
     * @return putanju do slike
     */
    public String getImagePath(String courseName) {
        return LocalImages.getItemImagePath(courseName, lesson, id);
    }

    public Bitmap getImage(String courseName, int widerDimension) {
        return DataManager.getImage(id, courseName, lesson, widerDimension);
    }

    public void hide(Context c) {
        DataManager.removeNote(c, id, lesson);
    }

    public void show(Context c) {
        Database.getInstance(c).insertNote(id, lesson, text, imageExists, audioExists);
    }

    public void edit(Context c, String lesson, String text, File imageFile, File audioFile) {
        DataManager.editNote(c, id, lesson, text, imageFile, audioFile);
    }

    public String getHistory(Context c) {
        // TODO: 20.9.15.
        return null;
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
        dest.writeInt(imageExists?1:0);
        dest.writeInt(audioExists?1:0);
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
