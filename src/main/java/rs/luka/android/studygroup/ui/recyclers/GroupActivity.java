package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import com.pkmmte.view.CircularImageView;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.database.GroupTable;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.network.Courses;
import rs.luka.android.studygroup.network.Groups;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.AddAnnouncementDialog;
import rs.luka.android.studygroup.ui.dialogs.ConfirmDialog;
import rs.luka.android.studygroup.ui.dialogs.FilterDialog;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.dialogs.InputDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.AddCourseActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.AddGroupActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.LoadingActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.UserInfoActivity;

public class GroupActivity extends SingleFragmentActivity implements GroupFragment.Callbacks,
                                                                     FilterDialog.Callbacks,
                                                                     ConfirmDialog.Callbacks,
                                                                     Network.NetworkCallbacks<String>,
                                                                     NavigationView.OnNavigationItemSelectedListener,
                                                                     InputDialog.Callbacks {
    private static final String TAG                 = "GroupActivity";

    public static final  String EXTRA_GROUP         = "exGroup";
    public static final  String EXTRA_SHOW_LIST     = "showList";
    private static final String DIALOG_JOIN         = "studygroup.dialog.joingroup";
    private static final String DIALOG_REMOVE       = "studygroup.dialog.removecourse";
    private static final String DIALOG_SEARCH       = "studygroup.dialog.searchgroups";
    private static final String DIALOG_INVITE       = "studygroup.dialog.invitetogroup";
    private static final String DIALOG_LEAVE        = "studygroup.dialog.leavegroup";
    private static final int REQUEST_JOIN_GROUP     = 0; //network request
    private static final int REQUEST_FILTER_COURSES = 1; //network request
    private static final int REQUEST_INVITE         = 2; //network request
    private static final int REQUEST_CREATE_GROUP   = 1000; //intent request
    private static final int REQUEST_LEAVE          = 3;
    private List<Integer> filterYears;
    private GroupFragment fragment;
    private Course pendingRemove;

    private NetworkExceptionHandler exceptionHandler = new NetworkExceptionHandler.DefaultHandler(this);
    private Group group;

    private DrawerLayout drawer;
    private NavigationView navigation;
    private HashMap<Integer, Group> navbarGroups                 = new HashMap<>();
    private static final int NAVBAR_ITEMS = 6;
    private static final int NAVBAR_VIEW_COURSES_POSITION = 1;
    private static final int NAVBAR_VIEW_EXAMS_POSITION   = 2;
    private static final int NAVBAR_VIEW_MEMBERS_POSITION = 3;
    private static final int NAVBAR_EDIT_POSITION = 4;
    private static final int NAVBAR_INVITE_POSITION = 5;
    private static final int NAVBAR_LEAVE_POSITION = 0;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_navbar;
    }

    @Override
    protected boolean shouldCreateFragment() {
        return getIntent().hasExtra(EXTRA_GROUP);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navigation = (NavigationView) findViewById(R.id.nav_view);
        drawer = (DrawerLayout)findViewById(R.id.group_drawer);
        setNavigationView();
        if(getSupportActionBar() == null) setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        ActionBar ac = getSupportActionBar();
        ac.setHomeAsUpIndicator(R.drawable.ic_menu);
        ac.setDisplayHomeAsUpEnabled(true);
        navigation.setNavigationItemSelectedListener(this);
        if(group == null) group = getIntent().getParcelableExtra(EXTRA_GROUP);
        if(group == null) drawer.openDrawer(GravityCompat.START);
        else LoadingActivity.setPreferredGroup(this, group);
    }

    private void setNavigationView() {
        Menu                 navMenu = navigation.getMenu();
        for(int i=1; i<=navbarGroups.size(); i++)
            navMenu.removeGroup(i);
        for(int i=1; i<=navbarGroups.size()*3; i++) //DOESN'T WORK
            navMenu.removeItem(i);
        navbarGroups.clear();

        GroupTable.GroupCursor groups = new GroupTable(this).queryGroups();
        groups.moveToFirst();
        int groupId = Menu.FIRST, itemId= Menu.FIRST;
        while(!groups.isAfterLast()) {
            Group   group = groups.getGroup();
            SubMenu groupMenu = navMenu.addSubMenu(group.getName());
            groupMenu.add(groupId, itemId, itemId, R.string.navbar_view_courses); itemId++;
            groupMenu.add(groupId, itemId, itemId, R.string.navbar_view_exams); itemId++;
            groupMenu.add(groupId, itemId, itemId, R.string.navbar_view_members); itemId++;
            if(group.getPermission() >= Group.PERM_OWNER)
                groupMenu.add(groupId, itemId, itemId, R.string.navbar_edit);
            itemId++;
            groupMenu.add(groupId, itemId, itemId, R.string.navbar_invite); itemId++;
            groupMenu.add(groupId, itemId, itemId, R.string.navbar_leave); itemId++;
            navbarGroups.put(groupId, group);
            groupId++;
            groups.moveToNext();
        }
        groups.close();
        View headerContainer = navigation.getHeaderView(0);

        TextView          username = (TextView) headerContainer.findViewById(R.id.nav_header_username);
        TextView          email    = (TextView)headerContainer.findViewById(R.id.nav_header_email);
        CircularImageView avatar   = (CircularImageView)headerContainer.findViewById(R.id.nav_header_image);
        User loggedInUser = User.getLoggedInUser();
        if(loggedInUser == null) {
            User.injectPrefs(this);
            loggedInUser = User.getLoggedInUser();
        }
        username.setText(loggedInUser.getName());
        email.setText(User.getMyEmail());
        if(loggedInUser.hasImage())
            loggedInUser.getImage(this, avatar.getWidth(), exceptionHandler,
                                            avatar);
        else
            avatar.setImageDrawable(getResources().getDrawable(R.drawable.default_user));

        if(!headerContainer.hasOnClickListeners())
            headerContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(GroupActivity.this, UserInfoActivity.class));
                }
            });
    }

    @Override
    protected Fragment createFragment() {
        fragment = GroupFragment.newInstance(getIntent().<Group>getParcelableExtra(EXTRA_GROUP));
        return fragment;
    }

    @Override
    public void onCourseSelected(Course course) {
        Intent i = new Intent(this, CourseActivity.class);
        i.putExtra(CourseActivity.EXTRA_COURSE, course);
        i.putExtra(CourseActivity.EXTRA_GO_FORWARD, true);
        i.putExtra(CourseActivity.EXTRA_MY_PERMISSION, group.getPermission());
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
        if(group == null || group.getPermission() < Group.PERM_OWNER)
            menu.removeItem(R.id.add_announcement);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.filter_courses:
                if(group != null)
                    FilterDialog.newInstance(getItems(), getSelectedItems()).show(getSupportFragmentManager(), null);
                return true;
            case R.id.settings:
                // TODO: 19.9.15.
                return true;
            case R.id.add_announcement:
                AddAnnouncementDialog.newInstance(group).show(getFragmentManager(), "");
                return true;
            case R.id.view_announcements:
                Intent i = new Intent(this, AnnouncementsActivity.class);
                i.putExtra(AnnouncementsActivity.EXTRA_GROUP, group);
                startActivity(i);
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
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawers();
        else
            moveTaskToBack(true);
    }

    public String[] getItems() {
        filterYears = group.getCourseYears();
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
        List<Integer> filteringYears = group.getFilteringYears();
        int[] selected = new int[filteringYears.size()];
        for(int i=0; i<selected.length; i++)
            selected[i] = filterYears.indexOf(filteringYears.get(i));
        return selected;
    }

    @Override
    public void onFiltered(Integer[] selected) {
        int[] selectedYears = new int[selected.length];
        for(int i=0; i<selected.length; i++)
            selectedYears[i] = filterYears.get(selected[i]);
        if(selectedYears.length == 0)
            selectedYears = Utils.integerToIntArray(group.getCourseYears().toArray());
        group.filter(REQUEST_FILTER_COURSES, selectedYears, this);
        fragment.showProgressView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CREATE_GROUP) {
            startActivity(new Intent(this, LoadingActivity.class));
        }
        if(fragment != null)
            fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPositive(DialogFragment dialog) {
        switch (dialog.getTag().split(":")[0]) {
            case DIALOG_JOIN:
                Groups.requestJoin(REQUEST_JOIN_GROUP,
                                   group.getIdValue(),
                                   this);
                break;
            case DIALOG_REMOVE:
                Courses.removeCourse(GroupFragment.REQUEST_REMOVE_COURSE, pendingRemove.getIdValue(), fragment);
                break;
            case DIALOG_LEAVE:
                Groups.leave(REQUEST_LEAVE, Long.parseLong(dialog.getTag().split(":")[1]), this);
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
        if(response.responseCode == Network.Response.RESPONSE_OK) {
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
                        case REQUEST_INVITE:
                            InfoDialog.newInstance(getString(R.string.invite_sent), "")
                                      .show(getSupportFragmentManager(), "");
                            break;
                        case REQUEST_LEAVE:
                            startActivity(new Intent(GroupActivity.this, LoadingActivity.class));
                            break;
                        default:
                            Log.w(TAG, "Invalid requestId " + id);
                    }
                }
            });
        } else {
            response.handleErrorCode(exceptionHandler);
        }
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        if(ex instanceof IOException)
            exceptionHandler.handleIOException((IOException)ex);
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

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.nav_create_group) {
            startActivityForResult(new Intent(this, AddGroupActivity.class), REQUEST_CREATE_GROUP);
        } else if(menuItem.getItemId() == R.id.nav_join_group) {
            InputDialog.newInstance(R.string.search_groups, null, R.string.search, R.string.cancel, "", 0)
                .show(getSupportFragmentManager(), DIALOG_SEARCH);
        } else {
            switch (menuItem.getItemId() % NAVBAR_ITEMS) {
                case NAVBAR_VIEW_COURSES_POSITION:
                    group = navbarGroups.get(menuItem.getGroupId());
                    fragment.changeGroup(group);
                    //setNavigationView(); doesn't work
                    getSupportActionBar().setTitle(group.getName());
                    break;
                case NAVBAR_VIEW_EXAMS_POSITION:
                    startActivity(new Intent(this, ScheduleActivity.class).putExtra(ScheduleActivity.EXTRA_GROUP,
                                                                                    navbarGroups.get(menuItem.getGroupId())));
                    break;
                case NAVBAR_VIEW_MEMBERS_POSITION:
                    startActivity(new Intent(this,
                                             MemberListActivity.class).putExtra(MemberListActivity.EXTRA_GROUP,
                                                                                navbarGroups.get(menuItem.getGroupId())));
                    break;
                case NAVBAR_EDIT_POSITION:
                    startActivity(new Intent(this, AddGroupActivity.class)
                            .putExtra(AddGroupActivity.EXTRA_GROUP, navbarGroups.get(menuItem.getGroupId())));
                    break;
                case NAVBAR_INVITE_POSITION:
                    InputDialog.newInstance(R.string.invite,
                                            getString(R.string.dialog_invite_text, getString(R.string.app_name)),
                                            R.string.send_invitation,
                                            R.string.cancel,
                                            "",
                                            R.string.dialog_invite_hint)
                               .show(getSupportFragmentManager(), DIALOG_INVITE + ":" + navbarGroups.get(menuItem.getGroupId()).getIdValue());
                    break;
                case NAVBAR_LEAVE_POSITION:
                    ConfirmDialog.newInstance(R.string.confirm_leave_group_title,
                                              R.string.confirm_leave_group_text,
                                              R.string.leave,
                                              R.string.cancel)
                            .show(getSupportFragmentManager(), DIALOG_LEAVE + ":" + navbarGroups.get(menuItem.getGroupId()).getIdValue());
                    break;
            }
        }
        drawer.closeDrawers();
        return true;
    }

    @Override
    public void onFinishedInput(DialogFragment dialog, String s) {
        switch (dialog.getTag().split(":")[0]) {
            case DIALOG_SEARCH:
                startActivity(new Intent(this,
                                         GroupSearchActivity.class).putExtra(GroupSearchActivity.EXTRA_SEARCH_TERM, s));
                break;
            case DIALOG_INVITE:
                Groups.invite(REQUEST_INVITE, Long.parseLong(dialog.getTag().split(":")[1]), s, this);
                break;
        }
    }
}
