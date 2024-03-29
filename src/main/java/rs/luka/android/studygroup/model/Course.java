package rs.luka.android.studygroup.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.widget.ImageView;

import java.io.File;
import java.util.Calendar;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Loaders;
import rs.luka.android.studygroup.io.backgroundtasks.CourseTasks;
import rs.luka.android.studygroup.io.backgroundtasks.ExamTasks;
import rs.luka.android.studygroup.io.backgroundtasks.LessonTasks;
import rs.luka.android.studygroup.io.backgroundtasks.NoteTasks;
import rs.luka.android.studygroup.io.backgroundtasks.QuestionTasks;
import rs.luka.android.studygroup.io.database.CourseTable;
import rs.luka.android.studygroup.io.database.LessonTable;
import rs.luka.android.studygroup.misc.Utils;

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
    private final boolean imageExists;

    public Course(ID id, String subject, String teacher, @Nullable Integer year, boolean imageExists) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.year = year;
        this.imageExists = imageExists;
    }

    private Course(Parcel in) {
        id = in.readParcelable(Course.class.getClassLoader());
        subject = in.readString();
        teacher = in.readString();
        year = Utils.intPrimitiveToObj(in.readInt(), -1);
        imageExists = in.readInt()!=0;
    }

    public long getGroupIdValue() {
        return id.getGroupIdValue();
    }
    public long getIdValue() {
        return id.getCourseIdValue();
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
        return imageExists;
    }

    public void getImage(Context c, int minDimension, NetworkExceptionHandler exceptionHandler, ImageView view) {
        CourseTasks.getCourseImage(c, id, minDimension, exceptionHandler, view);
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

    /**
     * Removes course from the database, keeping all lessons and data associated with it
     * @param c
     * @see CourseTable#hideCourse(ID)
     */
    public void shallowHide(Context c) {
        new CourseTable(c).hideCourse(id);
    }

    public void show(Context c) {
        new CourseTable(c).insertCourse(id, subject, teacher, year, imageExists);
    }

    /**
     * Hides course on server, and removes all data associated with it locally
     * @param c
     * @see CourseTasks#hideCourse(Context, ID, NetworkExceptionHandler)
     */
    public void hide(Context c, NetworkExceptionHandler exceptionHandler) {
        CourseTasks.hideCourse(c, id, exceptionHandler);
    }

    public void hideLesson(Context c, String lesson, NetworkExceptionHandler exceptionHandler) {
        LessonTasks.hideLesson(c, id, lesson, exceptionHandler);
    }

    public void shallowHideLesson(Context c, String lesson) {
        new LessonTable(c).hideLesson(id, lesson);
    }

    public void showLesson(Context c, int _id, String lesson, int noteCount, int questionCount) {
        new LessonTable(c).showLesson(id, _id, lesson, noteCount, questionCount);
    }

    public void addNote(Context c, String lesson, String text, File image, File audio, boolean isPrivate,
                        NetworkExceptionHandler handler) {
        NoteTasks.addNote(c, id, subject, lesson, text, image, audio, isPrivate, handler);
    }

    public void addQuestion(Context c, String lesson, String question, String answer, File image, boolean isPrivate,
                            NetworkExceptionHandler handler) {
        QuestionTasks.addQuestion(c, id, subject, lesson, question, answer, image, isPrivate, handler);
    }

    public void addExam(Context c, String klass, String lesson, String type, Calendar date, NetworkExceptionHandler handler) {
        ExamTasks.addExam(c, id, klass, lesson, type, date.getTime(), handler);
    }

    public void edit(Context c, String subject, String teacher, String year, File image,
                     NetworkExceptionHandler exceptionHandler) {
        CourseTasks.editCourse(c, id, subject, teacher, Integer.valueOf(year), image, exceptionHandler);
    }

    public void renameLesson(Context c, String oldName, String newName, NetworkExceptionHandler exceptionHandler) {
        LessonTasks.renameLesson(c, id, oldName, newName, exceptionHandler);
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
        dest.writeInt(imageExists?1:0);
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
