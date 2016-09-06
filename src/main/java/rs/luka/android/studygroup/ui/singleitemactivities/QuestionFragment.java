package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.ui.recyclers.HistoryActivity;

/**
 * Created by luka on 12.7.15.
 */
public class QuestionFragment extends Fragment {
    public static final  String ARG_QUESTION      = "aquestion";
    public static final  String ARG_COURSE_NAME   = "acourse";
    public static final String ARG_MY_PERMISSION = "aperm";
    private static int    IMAGE_IDEAL_DIMEN = 720;
    private Question question;
    private String courseName;
    private int permission;
    private TextView questionText;
    private TextView answerText;
    private ImageView image;
    private NetworkExceptionHandler exceptionHandler;

    public static QuestionFragment newInstance(String courseName, Question question, int permission) {
        QuestionFragment f    = new QuestionFragment();
        Bundle           args = new Bundle();
        args.putParcelable(ARG_QUESTION, question);
        args.putString(ARG_COURSE_NAME, courseName);
        args.putInt(ARG_MY_PERMISSION, permission);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler((AppCompatActivity)context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        question = getArguments().getParcelable(ARG_QUESTION);
        courseName = getArguments().getString(ARG_COURSE_NAME);
        permission = getArguments().getInt(ARG_MY_PERMISSION);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        IMAGE_IDEAL_DIMEN = metrics.widthPixels;
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
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), FullscreenImageActivity.class);
                try {
                    i.putExtra(FullscreenImageActivity.EXTRA_IMAGE_PATH, question.getImagePath(courseName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        });

        updateUI();
        return view;
    }

    private void updateUI() {
        questionText.setText(question.getQuestion());
        answerText.setText(question.getAnswer());
        if (question.hasImage()) {
            question.getImage(getContext(), courseName, IMAGE_IDEAL_DIMEN, exceptionHandler, image);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_question, menu);
        if(permission < Group.PERM_WRITE)
            menu.removeItem(R.id.question_history);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.question_history:
                startActivity(new Intent(getContext(), HistoryActivity.class).putExtra(HistoryActivity.EXTRA_ITEM, question));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
