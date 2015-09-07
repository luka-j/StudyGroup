package rs.luka.android.studygroup.model;

import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.Calendar;

import rs.luka.android.studygroup.Utils;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.io.Loaders;

/**
 * Created by Luka on 7/1/2015.
 */
public class Course implements Parcelable, Comparable<Course> {

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
    private boolean hidden = false;

    public Course(ID id, String subject, String teacher, @Nullable Integer year) {
        this(id, subject, teacher, year, false);
    }

    public Course(ID id, String subject, String teacher, @Nullable Integer year, boolean hidden) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.year = year;
        this.hidden = hidden;
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

    public Bitmap getImage(Context c) {
        return DataManager.getImage(c, id);
    }

    public Loader<Cursor> getLessonLoader(Context c) {
        return new Loaders.LessonLoader(c, id);
    }

    public Loader<Cursor> getNotesLoader(Context c, String lesson) {
        return new Loaders.ItemLoader(c, id, lesson, Loaders.ItemLoader.LOAD_NOTES);
    }

    public Loader<Cursor> getQuestionsLoader(Context c, String lesson) {
        return new Loaders.ItemLoader(c, id, lesson, Loaders.ItemLoader.LOAD_QUESTIONS);
    }

    public void hide(Context c) {
        Database.getInstance(c).hideCourse(id);
    }

    public void show(Context c) {
        Database.getInstance(c).insertCourse(id, subject, teacher, year);
    }

    public void remove(Context c) {
        DataManager.removeCourse(c, id);
    }

    public void removeLesson(Context c, String lesson) {
        DataManager.removeLesson(c, id, lesson);
    }

    public void hideLesson(Context c, String lesson) {
        Database.getInstance(c).hideLesson(id, lesson);
    }

    public void showLesson(Context c, int _id, String lesson, int noteCount, int questionCount) {
        Database.getInstance(c).showLesson(id, _id, lesson, noteCount, questionCount);
    }

    public void addNote(Context c, String lesson, String text, File image, File audio) {
        DataManager.addNote(c, id, lesson, text); //todo image/audio
    }

    public void addQuestion(Context c, String lesson, String question, String answer, File image) {
        DataManager.addQuestion(c, id, lesson, question, answer); //todo image
    }

    public void addExam(Context c, String klass, String lesson, String type, Calendar date) {
        DataManager.addExam(c, id, klass, lesson, type, date.getTime());
    }

    public void edit(Context c, String subject, String teacher, String year, File image) {
        DataManager.editCourse(c, id, subject, teacher, Integer.valueOf(year), image);
    }

    public void renameLesson(Context c, String oldName, String newName) {
        DataManager.renameLesson(c, id, oldName, newName);
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
        return o instanceof Course && ((Course) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NonNull Course another) {
        return id.compareTo(another.id);
    }
}
