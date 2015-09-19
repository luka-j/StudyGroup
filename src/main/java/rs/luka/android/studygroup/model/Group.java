package rs.luka.android.studygroup.model;

import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Loaders;

/**
 * Created by Luka on 7/1/2015.
 */
public class Group implements Parcelable, Comparable<Group> {
    public static final  Parcelable.Creator<Group> CREATOR
                                                       = new Parcelable.Creator<Group>() {
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
    private static final String                    TAG = "studygroup.Group";
    private static final Integer[] years = new Integer[]{1, 2, 3, 4}; //TODO
    private final ID id; //private by design (iliti nikada ne napusta ovu klasu)
    private final String name;
    private final String place;

    public Group(ID id, String name, String place) {
        this.id = id;
        this.name = name;
        this.place = place;
    }

    private Group(Parcel in) {
        id = in.readParcelable(Group.class.getClassLoader());
        name = in.readString();
        place = in.readString();
    }

    public String getName() {
        return name;
    }

    public boolean hasImage() {
        return DataManager.imageExists(id, name, null);
    }

    public Bitmap getImage(int scaleTo) {
        return DataManager.getImage(id, name, null, scaleTo);
    }

    public String getPlace() {
        return place;
    }

    public Loader<Cursor> getCourseLoader(Context c) {
        return new Loaders.CourseLoader(c, id);
    }

    public Loader<Cursor> getExamLoader(Context c) {
        return new Loaders.ItemLoader(c, id, null, Loaders.ItemLoader.LOAD_EXAMS);
    }

    public List<Integer> getExamYears() {
        return Arrays.asList(years);
    }

    public void addCourse(Context c, String subject, String teacher, String year, File image) {
        DataManager.addCourse(c, id, subject, teacher, Integer.parseInt(year), image);
    }

    public void edit(Context c, String name, String place, File image) {
        DataManager.editGroup(c, id, name, place, image);
    }

    public List<Integer> getCourseYears() {
        return Arrays.asList(years);
    }

    public void filter(Set<Integer> years) {
        Log.i(TAG, "wanna filter years " + years.toString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(id, 0);
        dest.writeString(name);
        dest.writeString(place);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Group && ((Group) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NonNull Group another) {
        return id.compareTo(another.id);
    }
}
