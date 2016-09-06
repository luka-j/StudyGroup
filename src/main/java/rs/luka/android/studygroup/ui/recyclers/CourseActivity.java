package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.network.Lessons;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.ConfirmDialog;
import rs.luka.android.studygroup.ui.dialogs.InputDialog;

/**
 * Created by luka on 2.7.15..
 */
public class CourseActivity extends SingleFragmentActivity implements CourseFragment.Callbacks,
                                                                      InputDialog.Callbacks,
                                                                      ConfirmDialog.Callbacks {

    public static final String      EXTRA_LESSON_NAME   = "lessonName";
    public static final String      EXTRA_COURSE        = "exCourse";
    public static final String      EXTRA_MY_PERMISSION = "permission";
    protected static final String   EXTRA_GO_FORWARD    = "forwardToLesson";
    protected static final String   EXTRA_GO_BACKWARD   = "backToCourses";
    private static final String     TAG                 = "CourseActivity";
    private static final String     TAG_DIALOG_RENAME   = "studygroup.dialog.renamelesson";
    private static final String     TAG_DIALOG_REMOVE   = "studygroup.dialog.removelesson";
    private NetworkExceptionHandler exceptionHandler    = new NetworkExceptionHandler.DefaultHandler(this);
    private CourseFragment fragment;
    private String         oldLessonName;
    private String pendingRemove;

    @Override
    protected Fragment createFragment() {
        fragment = CourseFragment.newInstance((Course) getIntent().getParcelableExtra(EXTRA_COURSE),
                                              getIntent().getIntExtra(EXTRA_MY_PERMISSION, 0));
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
                startActivity(new Intent(this, LessonActivity.class)
                                      .putExtra(EXTRA_LESSON_NAME, lesson)
                                      .putExtra(EXTRA_COURSE, getIntent().getParcelableExtra(EXTRA_COURSE))
                             .putExtra(LessonActivity.EXTRA_MY_PERMISSION, getIntent().getIntExtra(EXTRA_MY_PERMISSION, 0)));
                return true;
            }
            return false;
        } else { return false; }
    }

    @Override
    public void onLessonSelected(String title, boolean isPrivate) {
        Intent i = new Intent(this, LessonActivity.class);
        i.putExtra(EXTRA_COURSE, getIntent().getParcelableExtra(EXTRA_COURSE));
        i.putExtra(EXTRA_LESSON_NAME, title);
        i.putExtra(LessonActivity.EXTRA_MY_PERMISSION, getIntent().getIntExtra(EXTRA_MY_PERMISSION, 0));
        i.putExtra(LessonActivity.EXTRA_IS_PRIVATE, isPrivate);
        startActivity(i);
    }

    @Override
    public void onEdit(String title) {
        oldLessonName = title;
        InputDialog.newInstance(R.string.rename_lesson, null, R.string.rename, R.string.cancel, title, 0)
                   .show(getSupportFragmentManager(), TAG_DIALOG_RENAME);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpTo(this, new Intent(this, GroupActivity.class));
    }

    @Override
    public void onFinishedInput(DialogFragment dialog, String s) {
        ((Course) getIntent().getParcelableExtra(EXTRA_COURSE)).renameLesson(this, oldLessonName, s, exceptionHandler);
        fragment.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public void removeLesson(String title) {
        pendingRemove = title;
        ConfirmDialog.newInstance(R.string.confirm_remove_lesson_title,
                                  R.string.confirm_remove_lesson_message,
                                  R.string.confirm_remove_singular,
                                  R.string.cancel)
                     .show(getSupportFragmentManager(), TAG_DIALOG_REMOVE);
    }

    @Override
    public void onPositive(DialogFragment dialog) {
        switch (dialog.getTag()) {
            case TAG_DIALOG_REMOVE:
                Lessons.removeLesson(CourseFragment.REQUEST_REMOVE_LESSON,
                                     ((Course) getIntent().getParcelableExtra(EXTRA_COURSE)).getIdValue(),
                                     pendingRemove,
                                     fragment);
                break;
            default:
                Log.w(TAG, "Invalid dialog tag: " + dialog.getTag());
        }
    }

    @Override
    public void onNegative(DialogFragment dialog) {;}
}
