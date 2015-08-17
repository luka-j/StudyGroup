package rs.luka.android.studygroup.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.util.Date;

import rs.luka.android.studygroup.io.Retriever;

/**
 * Created by luka on 29.7.15..
 */
public class Exam implements Parcelable {

    public static final  Parcelable.Creator<Exam> CREATOR
            = new Parcelable.Creator<Exam>() {
        public Exam createFromParcel(Parcel in) {
            return new Exam(in);
        }

        public Exam[] newArray(int size) {
            return new Exam[size];
        }
    };

    private final ID     id;
    private final Course course;
    private final String klass; //class, as in odeljenje
    private final String lesson;
    private final String type;
    private final Date   date;

    public Exam(Parcel in) {
        id = in.readParcelable(Exam.class.getClassLoader());
        course = in.readParcelable(Exam.class.getClassLoader());
        klass = in.readString();
        lesson = in.readString();
        date = new Date(in.readLong());
        type = in.readString();
    }

    public Exam(ID id, String klass, String lesson, String type, Date date) {
        this.id = id;
        this.course = Retriever.getCourseFor(id);
        this.klass = klass;
        this.lesson = lesson;
        this.date = date;
        this.type = type;
    }

    public String getTitle() {
        return course.getSubject() + " (" + lesson + ")";
    }

    public String getDate(Context c) {
        return DateFormat.getDateInstance().format(date);
    }

    public String getKlass() {
        return klass + " - " + course.getTeacher();
    }

    public Integer getYear() {
        return course.getYear();
    }

    public String getSubject() {
        return course.getSubject();
    }

    public String getKlassName() {
        return klass;
    }

    public String getTeacher() {
        return course.getTeacher();
    }

    public String getLesson() {
        return lesson;
    }

    public String getType() {
        return type;
    }

    public Course getCourse() { return course; }

    public boolean hasImage() {

        return false;
    }

    public Bitmap getImage() {

        return null;
    }

    public void show() {}
    public void hide() {}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(id, 0);
        dest.writeParcelable(course, 0);
        dest.writeString(klass);
        dest.writeString(lesson);
        dest.writeLong(date.getTime());
        dest.writeString(type);
    }
}
