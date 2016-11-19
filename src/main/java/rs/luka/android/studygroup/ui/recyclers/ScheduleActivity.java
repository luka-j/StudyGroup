package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Exam;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.ExamDetailsDialog;
import rs.luka.android.studygroup.ui.dialogs.FilterDialog;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.AddExamActivity;

/**
 * Created by luka on 29.7.15.
 */
public class ScheduleActivity extends SingleFragmentActivity implements ScheduleFragment.Callbacks,
                                                                        FilterDialog.Callbacks,
                                                                        ExamDetailsDialog.Callbacks,
                                                                        Network.NetworkCallbacks<String> {
    public static final String EXTRA_GROUP                     = GroupActivity.EXTRA_GROUP;
    public static final String EXTRA_CURRENT_COURSE            = LessonActivity.EXTRA_CURRENT_COURSE;
    public static final String EXTRA_CURRENT_QUESTION_POSITION = LessonActivity.EXTRA_CURRENT_QUESTION_POSITION;
    public static final String EXTRA_SELECTED_QUESTIONS        = LessonActivity.EXTRA_SELECTED_QUESTIONS;
    public static final String EXTRA_CURRENT_LESSON            = LessonActivity.EXTRA_CURRENT_LESSON;
    private static final int REQUEST_FILTER_COURSES            = 0; //network request
    private static final String TAG                            = "ScheduleActivity";
    private List<Integer> filterYears;
    private ScheduleFragment fragment;

    @Override
    protected Fragment createFragment() {
        fragment = ScheduleFragment.newInstance(getIntent().<Group>getParcelableExtra(EXTRA_GROUP));
        return fragment;
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
        FilterDialog.newInstance(getItems(), getSelectedItems()).show(getSupportFragmentManager(), null);
    }

    public String[] getItems() {
        filterYears = (getIntent().<Group>getParcelableExtra(EXTRA_GROUP)).getCourseYears();
        Collections.sort(filterYears);
        String[] items = new String[filterYears.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = getString(R.string.year_no, filterYears.get(i));
        }
        return items;
    }

    /**
     * Must be called AFTER {@link #getItems}
     * @return
     */
    public int[] getSelectedItems() {
        Group group = getIntent().getParcelableExtra(EXTRA_GROUP);
        List<Integer> filteringYears = group.getFilteringYears();
        int[] selected = new int[filteringYears.size()];
        for(int i=0; i<selected.length; i++)
            selected[i] = filterYears.indexOf(filteringYears.get(i));
        return selected;
    }

    @Override
    public void onFiltered(Integer[] selected) {
        Group g = getIntent().getParcelableExtra(EXTRA_GROUP);
        int[] selectedYears = new int[selected.length];
        for(int i=0; i<selected.length; i++)
            selectedYears[i] = filterYears.get(selected[i]);
        if(selectedYears.length == 0)
            selectedYears = Utils.integerToIntArray(g.getCourseYears().toArray());
        g.filter(REQUEST_FILTER_COURSES, selectedYears, this); //todo show spinner
    }

    @Override
    public void onShowLesson(Exam exam) {
        startActivity(new Intent(this, LessonActivity.class)
                     .putExtra(LessonActivity.EXTRA_IS_PRIVATE, true)
                     .putExtra(LessonActivity.EXTRA_MY_PERMISSION, getIntent().<Group>getParcelableExtra(EXTRA_GROUP).getPermission())
                     .putExtra(LessonActivity.EXTRA_COURSE, exam.getCourse())
                     .putExtra(LessonActivity.EXTRA_LESSON, exam.getUserFriendlyLesson()));
    }

    @Override
    public void onRequestCompleted(int id, Network.Response<String> response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fragment.refresh();
            }
        });
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
