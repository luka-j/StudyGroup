package rs.luka.android.studygroup.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.network.Questions;

/**
 * Created by luka on 2.7.15..
 */
public class Question implements Parcelable, Comparable<Question>, PastEvents {
    public static final Parcelable.Creator<Question> CREATOR
            = new Parcelable.Creator<Question>() {
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        public Question[] newArray(int size) {
            return new Question[size];
        }
    };
    private final ID      id;
    private final String  lesson;
    private final String  question;
    private final String  answer;
    private final boolean imageExists;
    final         int     order;

    public Question(ID id, String lesson, String question, String answer, boolean imageExists, int order) {
        this.id = id;
        this.lesson = lesson;
        this.question = question;
        this.answer = answer;
        this.imageExists = imageExists;
        this.order = order;
    }

    private Question(Parcel in) {
        id = in.readParcelable(Question.class.getClassLoader());
        lesson = in.readString();
        question = in.readString();
        answer = in.readString();
        imageExists = in.readInt()!=0;
        order = in.readInt();
    }


    public String getLesson() {return lesson;}
    public long getGroupIdValue() {return id.getGroupIdValue();}
    public long getCourseIdValue() {return id.getCourseIdValue();}
    public long getIdValue() {return id.getItemIdValue();}


    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean hasImage() {
        return imageExists;
    }

    public void getImage(Context c, String courseName, int idealDimension, NetworkExceptionHandler handler, ImageView view) {
        DataManager.getQuestionImage(c, id, courseName, lesson, idealDimension, handler, view);
    }

    public String getImagePath(String courseName) throws IOException {
        return LocalImages.getQuestionImagePath(courseName, lesson, id);
    }

    public void getHistory(int requestId, Network.NetworkCallbacks<String> callbacks) {
        Questions.getEdits(requestId, id.getItemIdValue(), callbacks);
    }

    public void hide(Context c, NetworkExceptionHandler exceptionHandler) {
        DataManager.hideQuestion(c, id, lesson, exceptionHandler);
    }

    public void show(Context c) {
        Database.getInstance(c).insertQuestion(id, lesson, question, answer, imageExists, order);
    }

    public void edit(Context c, String lesson, String question, String answer, File image, NetworkExceptionHandler handler) {
        DataManager.editQuestion(c, id, lesson, question, answer, image, handler);
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
        dest.writeInt(order);
    }

    public int getOrder() {
        return order;
    }

    public void reorder(Context context, int toPosition, NetworkExceptionHandler handler) {
        DataManager.reorderQuestion(context, id, lesson, toPosition, order, handler);
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
