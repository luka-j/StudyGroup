package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;

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

    @Override
    protected Fragment createFragment() {
        Log.i("test", "creating fragment...");
        return CourseFragment.newInstance((Course) getIntent().getParcelableExtra(GroupActivity.EXTRA_COURSE));
    }

    /*@Override
    protected boolean shouldCreateFragment() {
        Bundle extras = getIntent().getExtras();
        if(extras == null) {
            Log.i("test", "extras == null, creating fragment");
            return true;
        }
        if(Retriever.getNumberOfLessons((UUID) extras.getSerializable(GroupActivity.EXTRA_COURSE_ID)) == 1) {
            if(extras.getBoolean(EXTRA_GO_FORWARD, false)) {
                Intent i = new Intent(this, LessonActivity.class);
                i.putExtra(EXTRA_COURSE_ID, extras.getSerializable(GroupActivity.EXTRA_COURSE_ID));
                i.putExtra(EXTRA_COURSE_NAME, extras.getString(GroupActivity.EXTRA_COURSE_NAME));
                i.putExtra(EXTRA_LESSON_NAME, Retriever.getLessons((UUID) extras.getSerializable(GroupActivity.EXTRA_COURSE_ID)).get(0));
                startActivity(i);
                Log.i("test", "going forward...");
                return false;
            }
            if(extras.getBoolean(EXTRA_GO_BACKWARD, false)) {
                Log.i("test", "...going backward");
                NavUtils.navigateUpFromSameTask(this);
                return false;
            }
        }
        Log.i("test", "not going anywhere. create fragment");
        return true;
    }*/

    @Override
    public void onLessonSelected(String title) {
        Intent i = new Intent(this, LessonActivity.class);
        i.putExtra(EXTRA_COURSE, getIntent().getParcelableExtra(GroupActivity.EXTRA_COURSE));
        i.putExtra(EXTRA_LESSON_NAME, title);
        startActivity(i);
    }

    @Override
    public void onEdit(String title) {
        RenameLessonDialog.newInstance(title).show(getSupportFragmentManager(), "debug");
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onRenamed(String s) {
        Log.i("test", "wanna rename lesson to " + s);
    }
}
