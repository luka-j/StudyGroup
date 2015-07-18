package rs.luka.android.studygroup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.List;

import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.networkcontroller.Retriever;

/**
 * Created by luka on 17.7.15..
 */
public class RootActivity extends AppCompatActivity implements GroupListFragment.Callbacks {
    public static final String EXTRA_GROUP_ID = "groupId";
    public static final String EXTRA_GROUP_NAME = "groupName";
    public static final String EXTRA_SHOW_LIST = "showList";
    private static final String TAG = "studygroup.RootActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(EXTRA_SHOW_LIST, false)) {
            Log.i(TAG, "showing list, extra supplied");
            inflateFragment();
            return;
        }
        //Log.i(TAG, "called from diff activity: " + getIntent().getPackage());
        if (!Retriever.isUserLoggedIn()) {
            //login
        } else {
            List<Group> groups = Retriever.getGroups();
            if (groups.size() == 1) {
                Intent i = new Intent(this, GroupActivity.class);
                i.putExtra(EXTRA_GROUP_ID, groups.get(0).getId());
                i.putExtra(EXTRA_GROUP_NAME, groups.get(0).getName());
                Log.i(TAG, "one group, starting it");
                startActivity(i);
            } else {
                Log.i(TAG, "multiple groups, showing list");
                inflateFragment();
            }
        }
    }


    private void inflateFragment() {
        setContentView(R.layout.activity_fragment);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new GroupListFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onGroupSelected(Group group) {
        Intent i = new Intent(this, GroupActivity.class);
        i.putExtra(EXTRA_GROUP_ID, group.getId());
        i.putExtra(EXTRA_GROUP_NAME, group.getName());
        startActivity(i);
    }
}
