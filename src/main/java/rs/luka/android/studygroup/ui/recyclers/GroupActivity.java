package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.network.Courses;
import rs.luka.android.studygroup.network.Groups;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.ConfirmDialog;
import rs.luka.android.studygroup.ui.dialogs.FilterDialog;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.AddCourseActivity;

public class GroupActivity extends SingleFragmentActivity implements GroupFragment.Callbacks,
                                                                     FilterDialog.Callbacks,
                                                                     ConfirmDialog.Callbacks,
                                                                     Network.NetworkCallbacks<String> {

    public static final  String EXTRA_GROUP     = "exGroup";
    public static final  String EXTRA_SHOW_LIST = "showList";
    private static final String DIALOG_JOIN = "studygroup.dialog.joingroup";
    private static final String DIALOG_REMOVE = "studygroup.dialog.removecourse";
    private static final int REQUEST_JOIN_GROUP = 0; //network request
    private static final int REQUEST_FILTER_COURSES = 1; //network request
    private static final String TAG             = "GroupActivity";
    private List<Integer> filterYears;
    private GroupFragment fragment;
    private Course pendingRemove;

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
        i.putExtra(CourseActivity.EXTRA_PERMISSION, getIntent().<Group>getParcelableExtra(EXTRA_GROUP).getPermission());
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
                FilterDialog.newInstance(getItems(), getSelectedItems()).show(getSupportFragmentManager(), null);
                return true;
            case R.id.raspored_kontrolnih:
                startActivity(new Intent(this, ScheduleActivity.class).putExtra(ScheduleActivity.EXTRA_GROUP,
                                                                                getIntent().getParcelableExtra(
                                                                                        EXTRA_GROUP)));
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
    public void onRequestJoin(Group group) {
        ConfirmDialog.newInstance(R.string.confirm_join_group_title,
                                  R.string.confirm_join_group_message,
                                  R.string.join,
                                  R.string.cancel)
                     .show(getSupportFragmentManager(), DIALOG_JOIN);
    }

    @Override
    public void onRemoveCourse(Course course) {
        pendingRemove = course;
        ConfirmDialog.newInstance(R.string.confirm_remove_course_title,
                                  R.string.confirm_remove_course_message,
                                  R.string.confirm_remove_singular,
                                  R.string.cancel)
                     .show(getSupportFragmentManager(), DIALOG_REMOVE);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public String[] getItems() {
        filterYears = getIntent().<Group>getParcelableExtra(EXTRA_GROUP).getCourseYears();
        Collections.sort(filterYears);
        String[] items = new String[filterYears.size()];
        for(int i=0; i<items.length; i++) {
            items[i] = getString(R.string.year_no, filterYears.get(i));
        }
        return items;
    }

    /**
     * Must be called AFTER {@link #getItems}
     * @return
     */
    public int[] getSelectedItems() {
        Group group = getIntent().<Group>getParcelableExtra(EXTRA_GROUP);
        List<Integer> filteringYears = group.getFilteringYears();
        int[] selected = new int[filteringYears.size()];
        for(int i=0; i<selected.length; i++)
            selected[i] = filterYears.indexOf(filteringYears.get(i));
        return selected;
    }

    @Override
    public void onFiltered(Integer[] selected) {
        Group g = getIntent().getParcelableExtra(EXTRA_GROUP);
        int[] selectedYears = new int[selected.length];
        for(int i=0; i<selected.length; i++)
            selectedYears[i] = filterYears.get(selected[i]);
        if(selectedYears.length == 0)
            selectedYears = Utils.integerToIntArray(g.getCourseYears().toArray());
        g.filter(REQUEST_FILTER_COURSES, selectedYears, this); //todo show spinner
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(fragment != null)
            fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPositive(DialogFragment dialog) {
        switch (dialog.getTag()) {
            case DIALOG_JOIN:
                Groups.requestJoin(REQUEST_JOIN_GROUP,
                                   getIntent().<Group>getParcelableExtra(EXTRA_GROUP).getIdValue(),
                                   this);
                break;
            case DIALOG_REMOVE:
                Courses.removeCourse(GroupFragment.REQUEST_REMOVE_COURSE, pendingRemove.getIdValue(), this);
                break;
            default:
        }
    }

    @Override
    public void onNegative(DialogFragment dialog) {
        ;
    }

    @Override
    public void onRequestCompleted(final int id, Network.Response<String> response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (id) {
                    case REQUEST_JOIN_GROUP:
                        InfoDialog.newInstance(getString(R.string.join_request_sent_title),
                                           getString(R.string.join_request_sent_text))
                              .show(getSupportFragmentManager(), "");
                        break;
                    case REQUEST_FILTER_COURSES:
                        fragment.refresh();
                        break;
                    default:
                        Log.w(TAG, "Invalid requestId " + id);
                }
            }
        });
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        if(ex instanceof IOException)
            new NetworkExceptionHandler.DefaultHandler(this).handleIOException((IOException)ex);
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog.newInstance(getString(R.string.error_unknown_ex_title),
                                           getString(R.string.error_unknown_ex_text))
                              .show(getSupportFragmentManager(), "");
                }
            });
            Log.e(TAG, "Unknown Throwable caught", ex);
        }
    }
}
