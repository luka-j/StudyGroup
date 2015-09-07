package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.FilterDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.AddCourseActivity;

public class GroupActivity extends SingleFragmentActivity implements GroupFragment.Callbacks,
                                                                     FilterDialog.Callbacks {

    public static final  String EXTRA_COURSE = "exCourse";
    public static final  String EXTRA_GROUP  = RootActivity.EXTRA_GROUP;
    private static final String TAG          = "GroupActivity";
    private List<Integer> filterYears;
    private GroupFragment fragment; // TODO: 7.9.15. fix REQUEST_EDIT_COURSE i sl. za fragmente (refresh)

    @Override
    protected Fragment createFragment() {
        fragment = GroupFragment.newInstance((Group) getIntent().getParcelableExtra(RootActivity.EXTRA_GROUP));
        return fragment;
    }

    @Override
    public void onCourseSelected(Course course) {
        Intent i = new Intent(this, CourseActivity.class);
        i.putExtra(EXTRA_COURSE, course);
        i.putExtra(CourseActivity.EXTRA_GO_FORWARD, true);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter_courses:
                FilterDialog.newInstance(getItems()).show(getSupportFragmentManager(), null);
                return true;
            case R.id.raspored_kontrolnih:
                startActivity(new Intent(this, ScheduleActivity.class).putExtra(EXTRA_GROUP,
                                                                                getIntent().getParcelableExtra(
                                                                                        RootActivity.EXTRA_GROUP)));
                return true;
            case R.id.show_all:
                //TODO
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditSelected(Course course, int requestCode) {
        Intent i = new Intent(this, AddCourseActivity.class);
        i.putExtra(EXTRA_COURSE, course);
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public String[] getItems() {
        filterYears = ((Group)getIntent().getParcelableExtra(RootActivity.EXTRA_GROUP)).getCourseYears();
        Collections.sort(filterYears);
        String[] items = new String[filterYears.size()];
        for(int i=0; i<items.length; i++) {
            items[i] = getString(R.string.year_no, filterYears.get(i));
        }
        return items;
    }

    @Override
    public void onFiltered(Integer[] selected) {
        Group g = getIntent().getParcelableExtra(RootActivity.EXTRA_GROUP);
        Set<Integer> selectedYears = new HashSet<>(selected.length);
        for(Integer item : selected) {
            selectedYears.add(filterYears.get(item));
        }
        g.filter(selectedYears);
    }
}
