package rs.luka.android.studygroup.activities.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

import rs.luka.android.studygroup.activities.SingleFragmentActivity;
import rs.luka.android.studygroup.io.Retriever;
import rs.luka.android.studygroup.model.Course;


public class GroupActivity extends SingleFragmentActivity implements GroupFragment.Callbacks {

    protected static final String EXTRA_COURSE_ID = "CourseId";
    protected static final String EXTRA_COURSE_NAME = "CourseName";
    private static final String TAG = "GroupActivity";

    @Override
    protected Fragment createFragment() {
        return GroupFragment.newInstance((UUID) getIntent().getSerializableExtra(RootActivity.EXTRA_GROUP_ID),
                getIntent().getStringExtra(RootActivity.EXTRA_GROUP_NAME));
    }

    @Override
    public void onCourseSelected(Course course) {
        UUID courseId = course.getId();
        if (Retriever.getNumberOfLessons(courseId) > 1) {
            Intent i = new Intent(this, CourseActivity.class);
            i.putExtra(EXTRA_COURSE_ID, courseId);
            i.putExtra(EXTRA_COURSE_NAME, course.getSubject());
            //i.putExtra(CourseActivity.EXTRA_GO_FORWARD, true);
            startActivity(i);
        } else {
            Intent i = new Intent(this, LessonActivity.class);
            i.putExtra(CourseActivity.EXTRA_LESSON_NAME, Retriever.getLessons(courseId).get(0));
            i.putExtra(CourseActivity.EXTRA_COURSE_NAME, course.getSubject());
            i.putExtra(CourseActivity.EXTRA_COURSE_ID, courseId);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
