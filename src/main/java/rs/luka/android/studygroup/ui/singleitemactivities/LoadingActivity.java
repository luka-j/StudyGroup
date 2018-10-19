package rs.luka.android.studygroup.ui.singleitemactivities;


/*
 * Struktura:
 * RootActivity (GroupListFragment), fragment se ne prikazuje ako postoji samo jedna grupa, postoje Search i Create (AddGroupActivity), ne postoji hiding
 * (0) LoginActivity, uvek se vraca u root
 * (1) GroupActivity (GroupFragment, courseList), postoji Add (AddCourseActivity), ne postoji history
 * (2) ScheduleActivity (ScheduleFragment), postoji Add (AddExamActivity)
 * (3) MemberListActivity (MemberListFragment)
 * (1) * CourseActivity (CourseFragment, lessonList), preskače se ako nema ili postoji samo jedna lekcija, ne postoji History
 * (1) * * LessonActivity (LessonPager: NoteListFragment (1), QuestionListFragment (2)), postoji Add za svaki fragment, ne postoji history
 * (1) * * (1) NotePagerActivity (NoteFragment), postoji FullscreenImage, Add: AddNoteActivity
 * (1) * * (2) QuestionPagerActivity (QuestionFragment), postoji FullScreenImage, Add: AddQuestionActivity
 * (2) (1) AddCourseActivity, sastoji se iz dva koraka (drugi je 'klasicni' unos podataka)
 * (2) (1) * SelectCourseActivity (GroupFragment), odabir predmeta na koji se kontrolni odnosi
 * (2) (2) ExamQuestionsActivity (QuestionListFragment), postoji Add (AddQuestionActivity)
 *
 * Svuda postoji history i hiding (uklj. "show all"), osim ako nije drugacije naznaceno
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.backgroundtasks.GroupTasks;
import rs.luka.android.studygroup.io.database.GroupTable;
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.io.network.UserManager;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.recyclers.GroupActivity;


/**
 * Created by luka on 4.2.16..
 */
public class LoadingActivity extends AppCompatActivity implements GroupTasks.GroupLoaderCallbacks,
                                                                  Network.NetworkCallbacks<String>,
                                                                  InfoDialog.Callbacks {

    private static final int REQUEST_GET_DETAILS = 1;
    private static final int REQUEST_REFRESH = 2;
    private static int currentRequest;
    static final String PREFERRED_GROUP_PREFS_NAME = "preferredGroup";
    static final String PREFERRED_GROUP_KEY        = "groupId";

    private NetworkExceptionHandler exceptionHandler;
    private boolean groupsLoaded = false;
    private GroupTable.GroupCursor groups;
    private boolean userdataLoaded = false;
    private Runnable userLoaded = () -> {
        userdataLoaded = true;
        proceed();
    };

    private class ExceptionHandler extends NetworkExceptionHandler.DefaultHandler {

        public ExceptionHandler() {
            super(LoadingActivity.this);
        }

        @Override
        public void handleSocketException(SocketException ex) {
            if(Network.Status.isOnline()) { //prevents this dialog from popping up multiple times. Should it?
                InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_login_socketex_title),
                                                           hostActivity.getString(R.string.error_login_socketex_text));
                dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                dialog.show(hostActivity.getFragmentManager(), TAG_DIALOG);
            } else {
                ((InfoDialog.Callbacks)hostActivity).onInfoDialogClosed(null);
            }
            Log.e("LoadingActivity", "Unexpected SocketException", ex);
            Network.Status.setOffline(); //todo ?
        }

        public void handleOffline() {
            if(Network.Status.isOnline()) {
                InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_offline_title),
                                                           hostActivity.getString(R.string.error_offline_text));
                dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                dialog.show(hostActivity.getFragmentManager(), TAG_DIALOG);
                Network.Status.setOffline();
            } else {
                ((InfoDialog.Callbacks)hostActivity).onInfoDialogClosed(null);
            }
        }
    }

    public static void setPreferredGroup(Context c, Group group) {
        SharedPreferences prefs = c.getSharedPreferences(PREFERRED_GROUP_PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putLong(PREFERRED_GROUP_KEY+User.getLoggedInUser().getId(), group.getIdValue()).apply();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        exceptionHandler = new ExceptionHandler();
        if(!User.isLoggedIn()) {
            SharedPreferences userPrefs = getSharedPreferences(User.PREFS_NAME, MODE_PRIVATE);
            if (User.hasSavedToken()) {
                currentRequest = REQUEST_REFRESH;
                UserManager.refreshToken(REQUEST_REFRESH,
                                         userPrefs.getString(User.PREFS_KEY_TOKEN, null),
                                         this);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        } else {
            loadData();
        }
    }

    private void loadData() {
        currentRequest = REQUEST_GET_DETAILS;
        GroupTasks.loadGroups(this, exceptionHandler, this);
        UserManager.getMyDetails(REQUEST_GET_DETAILS, this);
    }

    @Override
    public void onGroupsLoaded(GroupTable.GroupCursor groups) {
        this.groups = groups;
        groupsLoaded=true;
        proceed();
    }

    private void proceed() {
        if(groupsLoaded && userdataLoaded) {
            groups.moveToFirst();
            Group first = groups.getGroup();
            if(groups.getCount() == 1) {
                startActivity(new Intent(this, GroupActivity.class)
                             .putExtra(GroupActivity.EXTRA_GROUP, first));
            } else if(groups.getCount() == 0) {
                startActivity(new Intent(this, GroupActivity.class));
            } else {
                SharedPreferences prefs = getSharedPreferences(PREFERRED_GROUP_PREFS_NAME, MODE_PRIVATE);
                long id = prefs.getLong(PREFERRED_GROUP_KEY+User.getLoggedInUser().getId(), 0);
                if(id == 0) { //doesn't contain pref
                    startActivity(new Intent(this, GroupActivity.class)
                                          .putExtra(GroupActivity.EXTRA_GROUP, groups.getGroup()));
                } else {
                    while(groups.getGroup().getIdValue() != id)
                        if(!groups.moveToNext())
                            break;
                    startActivity(new Intent(this, GroupActivity.class)
                                          .putExtra(GroupActivity.EXTRA_GROUP, groups.getGroup()==null?first:groups.getGroup()));
                }
            }
        }
    }

    @Override
    public void onRequestCompleted(int id, Network.Response<String> response) {
        if(response.responseCode == Network.Response.RESPONSE_OK) {
            switch (id) {
                case REQUEST_GET_DETAILS:
                    try {
                        JSONObject jsonUser = new JSONObject(response.responseData);
                        long       userId   = jsonUser.getLong("id");
                        String     name     = jsonUser.getString("username");
                        String     email    = jsonUser.getString("email");
                        boolean    hasImage = jsonUser.getBoolean("hasImage");
                        User.setMyDetails(userId, name, email, hasImage);
                    } catch (JSONException e) {
                        exceptionHandler.handleJsonException();
                    }
                    break;
                case REQUEST_REFRESH:
                    User.instantiateUser(response.responseData, this);
            }
            if(id==REQUEST_GET_DETAILS) runOnUiThread(userLoaded);
            if(id==REQUEST_REFRESH) loadData();
        } else {
            response.handleErrorCode(exceptionHandler);
        }
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error) throw new Error(ex);
        if(Network.Status.checkNetworkStatus(this)) {
            if(ex instanceof IOException) {
                exceptionHandler.handleIOException((IOException)ex);
            } else {
                Log.wtf("LoadingActivity", "Unknown exception in LoadingActivity; this isn't happening");
            }
        }
    }

    @Override
    public void onInfoDialogClosed(InfoDialog dialog) {
        switch (currentRequest) {
            case REQUEST_GET_DETAILS:
                User.loadMyDetailsFromPrefs();
                runOnUiThread(userLoaded);
                break;
            case REQUEST_REFRESH:
                User.setOfflineUser(getSharedPreferences(User.PREFS_NAME, MODE_PRIVATE));
                loadData();
                break;
        }
    }
}
