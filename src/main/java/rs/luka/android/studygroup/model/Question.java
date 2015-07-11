package rs.luka.android.studygroup.model;

import android.support.annotation.Nullable;

/**
 * Created by luka on 2.7.15..
 */
public class Question {
    private final String question;
    private final String answer;
    private final @Nullable String answerImageUrl;

    public Question(String question, String answer, @Nullable String answerImageUrl) {
        this.question = question;
        this.answer = answer;
        this.answerImageUrl = answerImageUrl;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    @Nullable
    public String getAnswerImageUrl() {
        return answerImageUrl;
    }
}
