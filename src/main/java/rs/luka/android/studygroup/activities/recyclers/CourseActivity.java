package rs.luka.android.studygroup.activities.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.util.Log;

import java.util.UUID;

import rs.luka.android.studygroup.activities.SingleFragmentActivity;

/**
 * Created by luka on 2.7.15..
 */
public class CourseActivity extends SingleFragmentActivity implements CourseFragment.Callbacks {

    protected static final String EXTRA_COURSE_ID = GroupActivity.EXTRA_COURSE_ID; //convenience
    protected static final String EXTRA_COURSE_NAME = GroupActivity.EXTRA_COURSE_NAME;
    protected static final String EXTRA_LESSON_NAME = "lessonName";

    protected static final String EXTRA_GO_FORWARD  = "forwardToLesson";
    protected static final String EXTRA_GO_BACKWARD = "backToCourses";

    @Override
    protected Fragment createFragment() {
        Log.i("test", "creating fragment...");
        return CourseFragment.newInstance((UUID) getIntent().getSerializableExtra(GroupActivity.EXTRA_COURSE_ID),
                getIntent().getStringExtra(GroupActivity.EXTRA_COURSE_NAME));
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
        i.putExtra(EXTRA_COURSE_ID, getIntent().getSerializableExtra(EXTRA_COURSE_ID));
        i.putExtra(EXTRA_LESSON_NAME, title);
        i.putExtra(EXTRA_COURSE_NAME, getIntent().getStringExtra(GroupActivity.EXTRA_COURSE_NAME));
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }
}
