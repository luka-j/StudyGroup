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

    public static final  String EXTRA_GROUP     = "exGroup";
    public static final  String EXTRA_SHOW_LIST = "showList";
    private static final String TAG             = "GroupActivity";
    private List<Integer> filterYears;
    private GroupFragment fragment;

    @Override
    protected Fragment createFragment() {
        fragment = GroupFragment.newInstance((Group) getIntent().getParcelableExtra(EXTRA_GROUP));
        return fragment;
    }

    @Override
    public void onCourseSelected(Course course) {
        Intent i = new Intent(this, CourseActivity.class);
        i.putExtra(CourseActivity.EXTRA_COURSE, course);
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
                startActivity(new Intent(this, ScheduleActivity.class).putExtra(ScheduleActivity.EXTRA_GROUP,
                                                                                getIntent().getParcelableExtra(
                                                                                        EXTRA_GROUP)));
                return true;
            case R.id.show_all:
                //TODO
                return true;
            case R.id.settings:
                // TODO: 19.9.15.
                return true;
            case R.id.member_list:
                startActivity(new Intent(this, MemberListActivity.class).putExtra(MemberListActivity.EXTRA_GROUP,
                                                                                  getIntent().getParcelableExtra(
                                                                                          EXTRA_GROUP)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditSelected(Course course, int requestCode) {
        Intent i = new Intent(this, AddCourseActivity.class);
        i.putExtra(CourseActivity.EXTRA_COURSE, course);
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public String[] getItems() {
        filterYears = ((Group) getIntent().getParcelableExtra(EXTRA_GROUP)).getCourseYears();
        Collections.sort(filterYears);
        String[] items = new String[filterYears.size()];
        for(int i=0; i<items.length; i++) {
            items[i] = getString(R.string.year_no, filterYears.get(i));
        }
        return items;
    }

    @Override
    public void onFiltered(Integer[] selected) {
        Group g = getIntent().getParcelableExtra(EXTRA_GROUP);
        Set<Integer> selectedYears = new HashSet<>(selected.length);
        for(Integer item : selected) {
            selectedYears.add(filterYears.get(item));
        }
        g.filter(selectedYears);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        fragment.onActivityResult(requestCode, resultCode, data); // TODO: 10.9.15. fix
    }
}
