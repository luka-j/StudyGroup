package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Intent;
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

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Question;

/**
 * Created by luka on 12.7.15.
 */
public class QuestionFragment extends Fragment {
    public static final  String ARG_QUESTION      = "aquestion";
    public static final  String ARG_COURSE_NAME   = "acourse";
    private static final int    IMAGE_IDEAL_DIMEN = 700;
    private Question question;
    private String courseName;
    private TextView questionText;
    private TextView answerText;
    private ImageView image;
    private TextView history;

    public static QuestionFragment newInstance(String courseName, Question question) {
        QuestionFragment f    = new QuestionFragment();
        Bundle           args = new Bundle();
        args.putParcelable(ARG_QUESTION, question);
        args.putString(ARG_COURSE_NAME, courseName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        question = getArguments().getParcelable(ARG_QUESTION);
        courseName = getArguments().getString(ARG_COURSE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question, container, false);
        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        questionText = (TextView) view.findViewById(R.id.question_text);
        answerText = (TextView) view.findViewById(R.id.answer_text);
        image = (ImageView) view.findViewById(R.id.question_image);
        history = (TextView) view.findViewById(R.id.question_history);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), FullscreenImageActivity.class);
                i.putExtra(FullscreenImageActivity.EXTRA_IMAGE_PATH, question.getImagePath(courseName));
                startActivity(i);
            }
        });

        updateUI();
        return view;
    }

    private void updateUI() {
        questionText.setText(question.getQuestion());
        answerText.setText(question.getAnswer());
        if (question.hasImage(courseName)) {
            image.setImageBitmap(question.getImage(courseName, IMAGE_IDEAL_DIMEN));
        }
        history.setText(question.getHistory(getActivity()));
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
