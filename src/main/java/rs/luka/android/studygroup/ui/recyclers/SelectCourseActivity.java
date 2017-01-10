package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.Showcase;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.AddCourseActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.AddExamActivity;

/**
 * Created by luka on 31.7.15..
 */
public class SelectCourseActivity extends SingleFragmentActivity implements GroupFragment.Callbacks {
    public static final String EXTRA_COURSE = "resCourse";
    public static final String EXTRA_GROUP = ScheduleActivity.EXTRA_GROUP;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Showcase(this).showShowcase("exam-select-course", R.string.tut_addexam_selectcourse, true);
    }

    @Override
    public void onCourseSelected(Course course) {
        Intent data = new Intent();
        data.putExtra(EXTRA_COURSE, course);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, data);
        } else {
            getParent().setResult(Activity.RESULT_OK, data);
        }
        finish();
    }

    @Override
    public void onEditSelected(Course course, int requestCode) {
        Intent i = new Intent(this, AddCourseActivity.class);
        i.putExtra(CourseActivity.EXTRA_COURSE, course);
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onRequestJoin(Group group) {
        Log.w("SelectCourseActivity", "You cannot request to join group here!");
    }

    @Override
    public void onRemoveCourse(Course course) {
        InfoDialog.newInstance(getString(R.string.remove_course_disabled_title),
                               getString(R.string.remove_course_disabled_message));
    }

    @Override
    public Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.toolbar);
    }

    @Override
    protected Fragment createFragment() {
        return GroupFragment.newInstance(getIntent().<Group>getParcelableExtra(AddExamActivity.EXTRA_GROUP));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_course, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.select_course_cancel:
            case android.R.id.home:
                if (getParent() == null) {
                    setResult(Activity.RESULT_CANCELED, new Intent());
                } else {
                    getParent().setResult(Activity.RESULT_CANCELED, new Intent());
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
