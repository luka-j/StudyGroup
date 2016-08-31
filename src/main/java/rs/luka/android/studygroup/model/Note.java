package rs.luka.android.studygroup.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.network.Notes;

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
    private final int order;

    public Note(ID id, String lesson, String text, boolean imageExists, boolean audioExists, int order) {
        this.id = id;
        this.text = text;
        this.lesson = lesson;
        this.imageExists = imageExists;
        this.audioExists = audioExists;
        this.order = order;
    }

    private Note(Parcel in) {
        id = in.readParcelable(Note.class.getClassLoader());
        lesson = in.readString();
        text = in.readString();
        imageExists = in.readInt()!=0;
        audioExists = in.readInt()!=0;
        order = in.readInt();
    }

    public String getLesson() {return lesson;}
    public long getGroupIdValue() {return id.getGroupIdValue();}
    public long getCourseIdValue() {return id.getCourseIdValue();}
    public long getIdValue() {return id.getItemIdValue();}

    public String getText() {
        return text;
    }

    public boolean hasImage() {
        return imageExists;
    }

    public boolean hasAudio() {
        return audioExists;
    }

    public void getAudioPath(int requestId, String courseName, NetworkExceptionHandler exceptionHandler,
                             DataManager.AudioCallbacks callbacks) {
        DataManager.getAudio(requestId, id, courseName, lesson, exceptionHandler, callbacks);
    }

    /**
     * Vraća gde <em>bi trebalo</em> da se nalazi slika (ne proverava da li ona zaista postoji)
     *
     * @param courseName ime predmeta (potrebno za folder)
     * @return putanju do slike
     */
    public String getImagePath(String courseName) throws IOException {
        return LocalImages.getNoteImagePath(courseName, lesson, id);
    }

    public void getImage(final Context context, String courseName, int widerDimension,
                         NetworkExceptionHandler exceptionHandler, ImageView view) {
        DataManager.getNoteImage(context, id, courseName, lesson, widerDimension, exceptionHandler, view);
    }

    public void hide(Context c, NetworkExceptionHandler exceptionHandler) {
        DataManager.hideNote(c, id, lesson, exceptionHandler);
    }

    public void show(Context c) {
        Database.getInstance(c).insertNote(id, lesson, text, imageExists, audioExists, order);
    }

    public void edit(Context c, String lesson, String text, File imageFile, File audioFile, NetworkExceptionHandler handler) {
        DataManager.editNote(c, id, lesson, text, imageFile, audioFile, handler);
    }

    public void getHistory(int requestId, Network.NetworkCallbacks<String> callbacks) {
        Notes.getEdits(requestId, id.getItemIdValue(), callbacks);
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
        dest.writeInt(order);
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

    public void reorder(Context context, int toPosition, NetworkExceptionHandler handler) {
        DataManager.reorderNote(context, id, lesson, toPosition, order, handler);
    }

    public int getOrder() {
        return order;
    }
}
