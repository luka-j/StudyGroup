package rs.luka.android.studygroup.model;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by luka on 2.7.15..
 */
public class Question implements Serializable {
    private final UUID id;
    private final String question;
    private final String answer;
    private final @Nullable String answerImageUrl;

    public Question(UUID id, String question, String answer, @Nullable String answerImageUrl) {
        this.id = id;
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

    public UUID getId() {
        return id;
    }
}
