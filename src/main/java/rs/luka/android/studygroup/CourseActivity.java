package rs.luka.android.studygroup;

import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

/**
 * Created by luka on 2.7.15..
 */
public class CourseActivity extends SingleFragmentActivity implements CourseFragment.Callbacks {

    protected static final String EXTRA_COURSE_ID = GroupActivity.EXTRA_COURSE_ID; //convenience
    protected static final String EXTRA_LESSON_NAME = "lessonName";

    @Override
    protected Fragment createFragment() {
        return CourseFragment.newInstance((UUID) getIntent().getSerializableExtra(GroupActivity.EXTRA_COURSE_ID),
                getIntent().getStringExtra(GroupActivity.EXTRA_COURSE_NAME));
    }

    @Override
    public void onLessonSelected(String title) {
        Intent i = new Intent(this, LessonActivity.class);
        i.putExtra(EXTRA_COURSE_ID, getIntent().getSerializableExtra(EXTRA_COURSE_ID));
        i.putExtra(EXTRA_LESSON_NAME, title);
        startActivity(i);
    }
}
