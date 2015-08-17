package rs.luka.android.studygroup.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import rs.luka.android.studygroup.Utils;
import rs.luka.android.studygroup.io.Adder;
import rs.luka.android.studygroup.io.Hider;
import rs.luka.android.studygroup.io.Retriever;

/**
 * Created by Luka on 7/1/2015.
 */
public class Course implements Parcelable {

    public static final Parcelable.Creator<Course> CREATOR
            = new Parcelable.Creator<Course>() {
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        public Course[] newArray(int size) {
            return new Course[size];
        }
    };
    private final ID      id;
    private final String  subject;
    private final String  teacher;
    private final
    @Nullable     Integer year;

    public Course(ID id, String subject, String teacher, @Nullable Integer year) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.year = year;
    }

    private Course(Parcel in) {
        id = in.readParcelable(Course.class.getClassLoader());
        subject = in.readString();
        teacher = in.readString();
        year = Utils.intPrimitiveToObj(in.readInt(), -1);
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    @Nullable
    public Integer getYear() {
        return year;
    }

    @Override
    public String toString() {
        return subject + " by " + teacher + ", year " + String.valueOf(year);
    }

    public boolean hasImage() {
        return false;
    }

    public Bitmap getImage() {
        return Retriever.getCourseImage(id);
    }

    public int getNumberOfLessons() {
        return Retriever.getNumberOfLessons(id);
    }

    public List<String> getLessonList() {
        return Retriever.getLessons(id);
    }

    public int getNoteNumber(String lesson) {
        return Retriever.getNumberOfNotes(id, lesson);
    }

    public List<Note> getNotesByLesson(String lesson) {
        return Retriever.getNotes(id, lesson);
    }

    public int getQuestionNumber(String lesson) {
        return Retriever.getNumberOfQuestions(id, lesson);
    }

    public List<Question> getQuestionsByLesson(String lesson) {
        return Retriever.getQuestions(id, lesson);
    }

    public void hideCourse() {
        Hider.hideCourse(id);
    }

    public void showCourse() {
        Hider.showCourse(id);
    }

    public void hideLesson(String lesson) {
        Hider.hideLesson(id, lesson);
    }

    public void showLesson(String lesson) {
        Hider.showLesson(id, lesson);
    }

    public void addNote(String lesson, String text, File image, File audio) {
        Adder.addNote(id, lesson, text, image, audio);
    }

    public void addQuestion(String lesson, String question, String answer, File image) {
        Adder.addQuestion(id, lesson, question, answer, image);
    }

    public void addExam(String klass, String lesson, String type, Calendar date) {

    }

    public void edit(String subject, String teacher, String year, File image) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(id, 0);
        dest.writeString(subject);
        dest.writeString(teacher);
        dest.writeInt(Utils.integerObjToPrimitive(year, -1));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Course) { return ((Course) o).id.equals(id); }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
