package rs.luka.android.studygroup.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Loaders;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.network.Courses;
import rs.luka.android.studygroup.network.Network;

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
    public static final int PERM_READ_PUBLIC = 0;
    public static final int PERM_REQUEST_WRITE = 10;
    public static final int PERM_INVITED = 50;
    public static final int PERM_WRITE = 100;
    public static final int PERM_MODIFY = 200;
    public static final int PERM_OWNER = 300;
    private static final String                    TAG = "Group";
    private List<Integer> years;
    private List<Integer> filtering = new ArrayList<>();
    private final ID id; //private by design (iliti nikada ne napusta ovu klasu)
    private final String name;
    private final String place;
    private final int permission;
    private final boolean imageExists;

    public Group(ID id, String name, String place, boolean imageExists, List<Integer> years, int permission) {
        this.id = id;
        this.name = name;
        this.place = place;
        this.imageExists = imageExists;
        this.years = new ArrayList<>(years);
        this.permission = permission;
    }

    private Group(Parcel in) {
        id = new ID(in.readLong());
        name = in.readString();
        place = in.readString();
        imageExists = in.readInt()!=0;
        years = Utils.stringToList(in.readString());
        permission = in.readInt();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id.getGroupIdValue());
        dest.writeString(name);
        dest.writeString(place);
        dest.writeInt(imageExists?1:0);
        dest.writeString(Utils.listToString(years));
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

    public void getImage(Context c, int minDimension, NetworkExceptionHandler exceptionHandler, ImageView view) {
        DataManager.getGroupImage(c, id, minDimension, exceptionHandler, view);
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

    public void addCourse(Context c, String subject, String teacher, String year, File image,
                          NetworkExceptionHandler exceptionHandler) {
        DataManager.addCourse(c, id, subject, teacher, year.isEmpty()?null:Integer.parseInt(year), image, exceptionHandler);
    }

    public void edit(Context c, String name, String place, File image, NetworkExceptionHandler handler) {
        DataManager.editGroup(c, id, name, place, image, handler);
    }

    public void setCourseYears(List<Integer> years) {
        this.years = years;
    }
    public List<Integer> getCourseYears() {
        return years;
    }

    public void filter(int requestId, int[] years, Network.NetworkCallbacks<String> callbacks) {
        Courses.filterCourses(requestId, id.getGroupIdValue(), years, callbacks);
    }

    public void setFiltering(List<Integer> years) {
        this.filtering = years;
    }

    public List<Integer> getFilteringYears() {
        return filtering;
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
