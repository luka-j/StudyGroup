package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.RenameLessonDialog;

/**
 * Created by luka on 2.7.15..
 */
public class CourseActivity extends SingleFragmentActivity implements CourseFragment.Callbacks,
                                                                      RenameLessonDialog.Callbacks {

    private NetworkExceptionHandler exceptionHandler = new NetworkExceptionHandler.DefaultHandler(this);

    private static final String   TAG_DIALOG_RENAME = "studygroup.dialog.renamelesson";
    public static final String    EXTRA_LESSON_NAME = "lessonName";
    public static final String    EXTRA_COURSE      = "exCourse";
    protected static final String EXTRA_GO_FORWARD  = "forwardToLesson";
    protected static final String EXTRA_GO_BACKWARD = "backToCourses";
    private CourseFragment fragment;
    private String         oldLessonName;

    @Override
    protected Fragment createFragment() {
        fragment = CourseFragment.newInstance((Course) getIntent().getParcelableExtra(EXTRA_COURSE));
        return fragment;
    }

    /**
     * @param lesson naziv lekcije
     * @return da li je lekcija preskocena
     */
    public boolean handleLessonSkipping(int lessonCount, String lesson) {
        if (lessonCount < 2) {
            if (getIntent().getBooleanExtra(EXTRA_GO_BACKWARD, false)) {
                onBackPressed();
                return true;
            } else if (getIntent().getBooleanExtra(EXTRA_GO_FORWARD, false)) {
                startActivity(new Intent(this, LessonActivity.class).putExtra(EXTRA_LESSON_NAME, lesson)
                                                                    .putExtra(EXTRA_COURSE,
                                                                              getIntent().getParcelableExtra(
                                                                                      EXTRA_COURSE)));
                return true;
            }
            return false;
        } else { return false; }
    }

    @Override
    public void onLessonSelected(String title) {
        Intent i = new Intent(this, LessonActivity.class);
        i.putExtra(EXTRA_COURSE, getIntent().getParcelableExtra(EXTRA_COURSE));
        i.putExtra(EXTRA_LESSON_NAME, title);
        startActivity(i);
    }

    @Override
    public void onEdit(String title) {
        oldLessonName = title;
        RenameLessonDialog.newInstance(title).show(getSupportFragmentManager(), TAG_DIALOG_RENAME);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpTo(this, new Intent(this, GroupActivity.class));
    }

    @Override
    public void onRenamed(String s) {
        ((Course) getIntent().getParcelableExtra(EXTRA_COURSE)).renameLesson(this, oldLessonName, s, exceptionHandler);
        fragment.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.context_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_all:
                // TODO: 9.9.15.
                return true;
        }
        return false;
    }
}
