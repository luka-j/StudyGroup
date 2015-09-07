package rs.luka.android.studygroup.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.text.DateFormat;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;

/**
 * Created by luka on 2.7.15..
 */
public class Question implements Parcelable, Comparable<Question> {
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

    public Question(ID id, String lesson, String question, String answer) {
        this.id = id;
        this.lesson = lesson;
        this.question = question;
        this.answer = answer;
    }

    private Question(Parcel in) {
        id = in.readParcelable(Question.class.getClassLoader());
        lesson = in.readString();
        question = in.readString();
        answer = in.readString();
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean hasImage() {
        return false;
    }

    public Bitmap getImage(Context c) {
        return DataManager.getImage(c, id);
    }

    public String getHistory(Context c) {
        History h = DataManager.getHistory(c, id);
        return c.getString(R.string.note_history, h.get(0).getAuthor(),
                           DateFormat.getDateTimeInstance().format(h.get(0).getDate()));
    }

    public void hide(Context c) {
        DataManager.removeQuestion(c, id, lesson);
    }

    public void show(Context c) {
        Database.getInstance(c).insertQuestion(id, lesson, question, answer);
    }

    public void edit(Context c, String lesson, String question, String answer, File image) {
        DataManager.editQuestion(c, id, lesson, question, answer, image);
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
