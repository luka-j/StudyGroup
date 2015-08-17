package rs.luka.android.studygroup.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import rs.luka.android.studygroup.io.Adder;
import rs.luka.android.studygroup.io.Retriever;

/**
 * Created by Luka on 7/1/2015.
 */
public class Group implements Parcelable {
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
    private final ID     id;
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
        return false;
    }

    public Bitmap getImage() {
        return Retriever.getGroupImage(id);
    }

    public String getPlace() {
        return place;
    }

    public List<Course> getCourseList() {
        return Retriever.getCourses(id);
    }

    public List<Exam> getExamList() { return Retriever.getExams(id); }

    public List<Integer> getExamYears() {
        List<Exam> allExams = getExamList();
        List<Integer> categories = new LinkedList<>();
        for(Exam exam : allExams) {
            if(!categories.contains(exam.getYear()))
                categories.add(exam.getYear());
        }
        return categories;
    }

    public void addCourse(String subject, String teacher, String year, File image) {
        Adder.addCourse(id, subject, teacher, year, image);
    }

    public void edit(String name, String place, File image) {

    }

    public List<Integer> getCourseYears() {
        List<Course> allCourses = getCourseList();
        List<Integer> categories = new LinkedList<>();
        for(Course course : allCourses) {
            if(!categories.contains(course.getYear()))
                categories.add(course.getYear());
        }
        return categories;
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
}
