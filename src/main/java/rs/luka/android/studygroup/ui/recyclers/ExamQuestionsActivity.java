package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.AddQuestionActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.QuestionPagerActivity;

/**
 * Created by luka on 31.7.15..
 */
public class ExamQuestionsActivity extends SingleFragmentActivity
        implements QuestionListFragment.QuestionCallbacks {

    public static final String EXTRA_IMMUTABLE_LESSON = "canEditLessonName";
    public static final String EXAM_LESSON_PREFIX     = "-exam-";

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fab_fragment;
    }

    @Override
    protected Fragment createFragment() {
        return QuestionListFragment.newInstance((Course) getIntent().getParcelableExtra(ScheduleActivity.EXTRA_COURSE),
                                                getIntent().getStringExtra(ScheduleActivity.EXTRA_LESSON));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(ScheduleActivity.EXTRA_LESSON)
                                                  .substring(EXAM_LESSON_PREFIX.length()));
        final Activity This = this;
        findViewById(R.id.fab_add_exam_question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(This, AddQuestionActivity.class);
                i.putExtra(ScheduleActivity.EXTRA_CURRENT_COURSE,
                           getIntent().getParcelableExtra(ScheduleActivity.EXTRA_COURSE));
                i.putExtra(ScheduleActivity.EXTRA_CURRENT_LESSON,
                           getIntent().getStringExtra(ScheduleActivity.EXTRA_LESSON)
                                      .substring(EXAM_LESSON_PREFIX.length()));
                i.putExtra(EXTRA_IMMUTABLE_LESSON, true);
                startActivity(i);
            }
        });
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
    public void onQuestionSelected(Question question) {
        Intent i = new Intent(this, QuestionPagerActivity.class);
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_COURSE,
                   getIntent().getParcelableExtra(ScheduleActivity.EXTRA_COURSE));
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_LESSON,
                   getIntent().getStringExtra(ScheduleActivity.EXTRA_LESSON));
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_QUESTION, question);
        startActivity(i);
    }

    @Override
    public void onQuestionsEdit(Set<Question> questions) {
        ArrayList<Question> l = new ArrayList<>(questions);
        Collections.sort(l);
        Intent i = new Intent(this, AddQuestionActivity.class);
        i.putParcelableArrayListExtra(ScheduleActivity.EXTRA_SELECTED_QUESTIONS, l);
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_COURSE,
                   getIntent().getParcelableExtra(ScheduleActivity.EXTRA_COURSE));
        i.putExtra(ScheduleActivity.EXTRA_CURRENT_LESSON,
                   getIntent().getStringExtra(ScheduleActivity.EXTRA_LESSON)
                              .substring(EXAM_LESSON_PREFIX.length()));
        i.putExtra(EXTRA_IMMUTABLE_LESSON, true);
        startActivity(i);
    }
}
