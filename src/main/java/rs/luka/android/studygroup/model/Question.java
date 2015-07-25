package rs.luka.android.studygroup.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;

import rs.luka.android.studygroup.io.Retriever;

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
    private final String question;
    private final String answer;

    public Question(ID id, String question, String answer) {
        this.id = id;
        this.question = question;
        this.answer = answer;
    }

    private Question(Parcel in) {
        id = in.readParcelable(Question.class.getClassLoader());
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

    public Bitmap getImage() {
        return Retriever.getQuestionImage(id);
    }

    public String getHistory(Context c) {
        return Retriever.getQuestionHistory(id, c);
    }

    public void edit(String lesson, String question, String answer, File image) {
        ;//todo
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(id, 0);
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
        if (id.itemId < another.id.itemId) { return -1; } else if (id.itemId > another.id.itemId) {
            return 1;
        }
        return 0;
    }
}
