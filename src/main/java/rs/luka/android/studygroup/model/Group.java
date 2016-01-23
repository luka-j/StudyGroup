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

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Loaders;

/**
 * Created by Luka on 7/1/2015.
 */
public class Group implements Parcelable, Comparable<Group>, PastEvents {

    public static final  Parcelable.Creator<Group> CREATOR
                                                       = new Parcelable.Creator<Group>() {
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
    public static final int PERM_READ_PUBLIC = 0;
    public static final int PERM_REQUEST_WRITE = 10;
    public static final int PERM_WRITE = 100;
    public static final int PERM_MODIFY = 200;
    public static final int PERM_OWNER = 300;
    private static final String                    TAG = "studygroup.Group";
    private static final Integer[] years = new Integer[]{1, 2, 3, 4}; //TODO
    private final ID id; //private by design (iliti nikada ne napusta ovu klasu)
    private final String name;
    private final String place;
    private final int permission;
    private final boolean imageExists;

    public Group(ID id, String name, String place, boolean imageExists, int permission) {
        this.id = id;
        this.name = name;
        this.place = place;
        this.imageExists = imageExists;
        this.permission = permission;
    }

    private Group(Parcel in) {
        id = new ID(in.readLong());
        name = in.readString();
        place = in.readString();
        imageExists = in.readInt()!=0;
        permission = in.readInt();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id.getGroupIdValue());
        dest.writeString(name);
        dest.writeString(place);
        dest.writeInt(imageExists?1:0);
        dest.writeInt(permission);
    }

    public int getPermission() {
        return permission;
    }
    public long getIdValue() {
        return id.getGroupIdValue();
    }

    public String getName() {
        return name;
    }

    public boolean hasImage() {
        return imageExists;
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

    public void addCourse(Context c, String subject, String teacher, String year, File image,
                          NetworkExceptionHandler exceptionHandler) {
        DataManager.addCourse(c, id, subject, teacher, Integer.parseInt(year), image, exceptionHandler);
    }

    public void edit(Context c, String name, String place, File image, NetworkExceptionHandler handler) {
        DataManager.editGroup(c, id, name, place, image, handler);
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
