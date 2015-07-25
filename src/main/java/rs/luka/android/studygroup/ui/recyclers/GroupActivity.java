package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;

import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.AddCourseActivity;


public class GroupActivity extends SingleFragmentActivity implements GroupFragment.Callbacks {

    public static final  String EXTRA_COURSE = "exCourse";
    public static final  String EXTRA_GROUP  = RootActivity.EXTRA_GROUP;
    private static final String TAG          = "GroupActivity";

    @Override
    protected Fragment createFragment() {
        return GroupFragment.newInstance((Group) getIntent().getParcelableExtra(RootActivity.EXTRA_GROUP));
    }

    @Override
    public void onCourseSelected(Course course) {
        if (course.getNumberOfLessons() > 1) {
            Intent i = new Intent(this, CourseActivity.class);
            i.putExtra(EXTRA_COURSE, course);
            //i.putExtra(CourseActivity.EXTRA_GO_FORWARD, true);
            startActivity(i);
        } else {
            Intent i = new Intent(this, LessonActivity.class);
            i.putExtra(CourseActivity.EXTRA_LESSON_NAME, course.getLessonList().get(0));
            startActivity(i);
        }
    }

    @Override
    public void onEdit(Course course) {
        Intent i = new Intent(this, AddCourseActivity.class);
        i.putExtra(EXTRA_COURSE, course);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
