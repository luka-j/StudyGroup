package rs.luka.android.studygroup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.networkcontroller.CoursesManager;

/**
 * Created by luka on 12.7.15..
 */
public class QuestionFragment extends Fragment {
    public static final String EXTRA_QUESTION = "question";

    private Question question;
    private TextView questionText;
    private TextView answerText;
    private ImageView image;
    private TextView history;

    public static QuestionFragment newInstance(Question question) {
        QuestionFragment f = new QuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_QUESTION, question);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        question = (Question) getArguments().getSerializable(EXTRA_QUESTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);

        questionText = (TextView) view.findViewById(R.id.question_text);
        answerText = (TextView) view.findViewById(R.id.answer_text);
        image = (ImageView) view.findViewById(R.id.question_image);
        history = (TextView) view.findViewById(R.id.question_history);

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        updateUI();
        return view;
    }

    private void updateUI() {
        questionText.setText(question.getQuestion());
        answerText.setText(question.getAnswer());
        if (question.getAnswerImageUrl() != null)
            image.setImageBitmap(CoursesManager.getQuestionImage(question.getId()));
        history.setText(CoursesManager.getQuestionHistory(question.getId(), getActivity()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
