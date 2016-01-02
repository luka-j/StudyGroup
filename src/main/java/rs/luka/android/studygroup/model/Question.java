package rs.luka.android.studygroup.model;

import android.content.Context;
import android.graphics.Bitmap;
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
public class Question implements Parcelable, Comparable<Question>, PastEvents {
    public static final String EXAM_PREFIX = "-exam-";

    public static final Parcelable.Creator<Question> CREATOR
            = new Parcelable.Creator<Question>() {
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        public Question[] newArray(int size) {
            return new Question[size];
        }
    };
    private final ID id;
    private final String lesson;
    private final String question;
    private final String answer;
    private final boolean imageExists;

    public Question(ID id, String lesson, String question, String answer, boolean imageExists) {
        this.id = id;
        this.lesson = lesson;
        this.question = question;
        this.answer = answer;
        this.imageExists = imageExists;
    }

    private Question(Parcel in) {
        id = in.readParcelable(Question.class.getClassLoader());
        lesson = in.readString();
        question = in.readString();
        answer = in.readString();
        imageExists = in.readInt()!=0;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean hasImage() {
        return imageExists;
    }

    public Bitmap getImage(String courseName, int idealDimension) {
        return DataManager.getImage(id, courseName, lesson, idealDimension);
    }

    public String getImagePath(String courseName) {
        return LocalImages.getItemImagePath(courseName, lesson, id);
    }

    public String getHistory(Context c) {
        // TODO: 20.9.15.
        return null;
    }

    public void hide(Context c) {
        DataManager.removeQuestion(c, id, lesson);
    }

    public void show(Context c) {
        Database.getInstance(c).insertQuestion(id, lesson, question, answer, imageExists);
    }

    public void edit(Context c, String lesson, String question, String answer, File image) {
        if (lesson.startsWith(EXAM_PREFIX)) {
            DataManager.editQuestion(c, id, lesson.substring(EXAM_PREFIX.length()), question, answer, image);
        } else { DataManager.editQuestion(c, id, lesson, question, answer, image); }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(id, 0);
        dest.writeString(lesson);
        dest.writeString(question);
        dest.writeString(answer);
        dest.writeInt(imageExists?1:0);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Question && ((Question) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(@NonNull Question another) {
        return id.compareTo(another.id);
    }
}
