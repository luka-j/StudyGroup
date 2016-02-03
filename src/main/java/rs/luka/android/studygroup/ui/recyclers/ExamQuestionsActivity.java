package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.network.Questions;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.AddQuestionActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.QuestionPagerActivity;

/**
 * Created by luka on 31.7.15.
 */
public class ExamQuestionsActivity extends SingleFragmentActivity
        implements QuestionListFragment.QuestionCallbacks,
                   Network.NetworkCallbacks {
    private static final String TAG = "ExamQuestionsActivity";

    public static final  String EXTRA_IMMUTABLE_LESSON = "canEditLessonName";
    public static final  String EXTRA_LESSON_REAL_NAME = "realLesson";
    public static final  String EXTRA_COURSE           = "course";
    public static final  String EXTRA_LESSON           = "lesson";
    public static final  String EXTRA_EXAM_QUESTION    = "isFromExam";
    public static final  String EXTRA_PERMISSION        = "permission";
    private static final int    REQUEST_ADD_QUESTION   = 0;
    private static final int    REQUEST_EDIT_QUESTION  = 1;
    private static final int    REQUEST_REMOVE_QUESTIONS = 2;
    private String               lesson;
    private Course               course;
    private QuestionListFragment fragment;

    private int setSize, count;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fab_fragment;
    }

    @Override
    protected Fragment createFragment() {
        fragment = QuestionListFragment.newInstance((Course) getIntent().getParcelableExtra(EXTRA_COURSE),
                                                    getIntent().getStringExtra(EXTRA_LESSON),
                                                   getIntent().getIntExtra(EXTRA_PERMISSION, 0));
        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        course = getIntent().getParcelableExtra(EXTRA_COURSE);
        lesson = getIntent().getStringExtra(EXTRA_LESSON);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(lesson.substring(Question.EXAM_PREFIX.length()));
        final Activity This = this;
        findViewById(R.id.fab_add_exam_question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(This, AddQuestionActivity.class);
                i.putExtra(ScheduleActivity.EXTRA_CURRENT_COURSE, course);
                i.putExtra(ScheduleActivity.EXTRA_CURRENT_LESSON, lesson.substring(Question.EXAM_PREFIX.length()));
                i.putExtra(EXTRA_LESSON_REAL_NAME, lesson);
                i.putExtra(EXTRA_IMMUTABLE_LESSON, true);
                i.putExtra(EXTRA_EXAM_QUESTION, true);
                startActivityForResult(i, REQUEST_ADD_QUESTION);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ADD_QUESTION:
                case REQUEST_EDIT_QUESTION:
                    fragment.refresh();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return onOptionsItemSelected(item);
    }

    @Override
    public void onQuestionSelected(int questionIndex) {
        Intent i = new Intent(this, QuestionPagerActivity.class);
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_COURSE,
                   getIntent().getParcelableExtra(EXTRA_COURSE));
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_LESSON,
                   getIntent().getStringExtra(EXTRA_LESSON));
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_QUESTION_POSITION, questionIndex);
        startActivity(i);
    }

    @Override
    public void onQuestionsEdit(Set<Question> questions) {
        ArrayList<Question> l = new ArrayList<>(questions);
        Collections.sort(l);
        Intent i = new Intent(this, AddQuestionActivity.class);
        i.putParcelableArrayListExtra(ScheduleActivity.EXTRA_SELECTED_QUESTIONS, l);
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_COURSE,
                   getIntent().getParcelableExtra(EXTRA_COURSE));
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_LESSON, lesson.substring(Question.EXAM_PREFIX.length()));
        i.putExtra(EXTRA_LESSON_REAL_NAME, lesson);
        i.putExtra(EXTRA_IMMUTABLE_LESSON, true);
        i.putExtra(EXTRA_EXAM_QUESTION, true);
        startActivityForResult(i, REQUEST_EDIT_QUESTION);
    }

    @Override
    public void onQuestionsRemove(Set<Long> ids) {
        setSize = ids.size();
        count=0;
        Questions.removeQuestions(REQUEST_REMOVE_QUESTIONS, ids, this);
    }

    @Override
    public FloatingActionButton getFab() {
        return (FloatingActionButton) findViewById(R.id.fab_add_exam_question);
    }

    @Override
    public void onRequestCompleted(final int id, Network.Response response) {
        if(response.responseCode == Network.Response.RESPONSE_OK) {
            count++;
            if(count >= setSize) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fragment.refresh();
                    }
                });
                count=0; setSize=0;
            }
        } else {
            response.handleException(new NetworkExceptionHandler.DefaultHandler(this));
        }
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        if(ex instanceof IOException)
            new NetworkExceptionHandler.DefaultHandler(this).handleIOException((IOException)ex);
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog.newInstance(getString(R.string.error_unknown_ex_title),
                                           getString(R.string.error_unknown_ex_text))
                              .show(getSupportFragmentManager(), "");
                }
            });
            Log.e(TAG, "Unknown Throwable caught", ex);
        }
    }
}
