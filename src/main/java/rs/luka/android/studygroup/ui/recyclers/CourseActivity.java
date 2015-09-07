package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;

import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.RenameLessonDialog;

/**
 * Created by luka on 2.7.15..
 */
public class CourseActivity extends SingleFragmentActivity implements CourseFragment.Callbacks,
                                                                      RenameLessonDialog.Callbacks {

    public static final    String EXTRA_LESSON_NAME = "lessonName";
    protected static final String EXTRA_COURSE      = GroupActivity.EXTRA_COURSE;
    protected static final String EXTRA_GO_FORWARD  = "forwardToLesson";
    protected static final String EXTRA_GO_BACKWARD = "backToCourses";
    private CourseFragment fragment;
    private String         oldLessonName;

    @Override
    protected Fragment createFragment() {
        fragment = CourseFragment.newInstance((Course) getIntent().getParcelableExtra(GroupActivity.EXTRA_COURSE));
        return fragment;
    }

    protected void skip(String lesson) {
        if (getIntent().getBooleanExtra(EXTRA_GO_BACKWARD, false)) {
            onBackPressed();
        } else if (getIntent().getBooleanExtra(EXTRA_GO_FORWARD, false)) {
            startActivity(new Intent(this, LessonActivity.class).putExtra(EXTRA_LESSON_NAME, lesson)
                                                                .putExtra(EXTRA_COURSE,
                                                                          getIntent().getParcelableExtra(
                                                                                  GroupActivity.EXTRA_COURSE)));
        }
    }

    @Override
    public void onLessonSelected(String title) {
        Intent i = new Intent(this, LessonActivity.class);
        i.putExtra(EXTRA_COURSE, getIntent().getParcelableExtra(GroupActivity.EXTRA_COURSE));
        i.putExtra(EXTRA_LESSON_NAME, title);
        startActivity(i);
    }

    @Override
    public void onEdit(String title) {
        oldLessonName = title;
        RenameLessonDialog.newInstance(title).show(getSupportFragmentManager(), "debug");
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpTo(this, new Intent(this, GroupActivity.class));
    }

    @Override
    public void onRenamed(String s) {
        ((Course) getIntent().getParcelableExtra(GroupActivity.EXTRA_COURSE)).renameLesson(this, oldLessonName, s);
        fragment.refresh();
    }
}
