package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Exam;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.ExamDetailsDialog;
import rs.luka.android.studygroup.ui.dialogs.FilterDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.AddExamActivity;

/**
 * Created by luka on 29.7.15.
 */
public class ScheduleActivity extends SingleFragmentActivity implements ScheduleFragment.Callbacks,
                                                                        FilterDialog.Callbacks,
                                                                        ExamDetailsDialog.Callbacks {
    public static final String EXTRA_GROUP                     = GroupActivity.EXTRA_GROUP;
    public static final String EXTRA_CURRENT_COURSE            = LessonActivity.EXTRA_CURRENT_COURSE;
    public static final String EXTRA_CURRENT_QUESTION_POSITION = LessonActivity.EXTRA_CURRENT_QUESTION_POSITION;
    public static final String EXTRA_SELECTED_QUESTIONS        = LessonActivity.EXTRA_SELECTED_QUESTIONS;
    public static final String EXTRA_CURRENT_LESSON            = LessonActivity.EXTRA_CURRENT_LESSON;
    private List<Integer> filterYears;

    @Override
    protected Fragment createFragment() {
        return ScheduleFragment.newInstance((Group) getIntent().getParcelableExtra(EXTRA_GROUP));
    }

    @Override
    public void onExamSelected(Exam exam) {
        ExamDetailsDialog.newInstance(exam).show(getSupportFragmentManager(), null);
    }

    @Override
    public void onEditSelected(Exam exam) {
        Intent i = new Intent(this, AddExamActivity.class);
        i.putExtra(AddExamActivity.EXTRA_EXAM, exam);
        startActivity(i);
    }

    @Override
    public void onFilterSelected() {
        FilterDialog.newInstance(getItems()).show(getSupportFragmentManager(), null);
    }

    public String[] getItems() {
        filterYears = ((Group) getIntent().getParcelableExtra(EXTRA_GROUP)).getExamYears();
        Collections.sort(filterYears);
        String[] items = new String[filterYears.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = getString(R.string.year_no, filterYears.get(i));
        }
        return items;
    }

    @Override
    public void onFiltered(Integer[] selected) {
        Group g = getIntent().getParcelableExtra(EXTRA_GROUP);
        Set<Integer> selectedYears = new HashSet<>(selected.length);
        for (Integer item : selected) {
            selectedYears.add(filterYears.get(item));
        }
        g.filter(selectedYears);
    }

    @Override
    public void onShowQuestions(Exam exam) {
        startActivity(new Intent(this, ExamQuestionsActivity.class).putExtra(ExamQuestionsActivity.EXTRA_COURSE,
                                                                             exam.getCourse())
                                                                   .putExtra(ExamQuestionsActivity.EXTRA_LESSON,
                                                                             Question.EXAM_PREFIX
                                                                             + exam.getLesson()));
    }
}
