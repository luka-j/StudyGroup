package rs.luka.android.studygroup;

import android.content.Intent;
import android.support.v4.app.Fragment;

import rs.luka.android.studygroup.model.Course;


public class GroupActivity extends SingleFragmentActivity implements GroupFragment.Callbacks {

    protected static final String EXTRA_COURSE_ID = "CourseId";
    protected static final String EXTRA_COURSE_NAME = "CourseName";
    private static final String TAG = "GroupActivity";

    @Override
    protected Fragment createFragment() {
        return new GroupFragment();
    }

    @Override
    public void onCourseSelected(Course course) {
        Intent i = new Intent(this, CourseActivity.class);
        i.putExtra(EXTRA_COURSE_ID, course.getId());
        i.putExtra(EXTRA_COURSE_NAME, course.getSubject());
        startActivity(i);
    }
}
