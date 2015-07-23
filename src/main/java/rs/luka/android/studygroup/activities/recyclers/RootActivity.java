package rs.luka.android.studygroup.activities.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.List;

import rs.luka.android.studygroup.activities.SingleFragmentActivity;
import rs.luka.android.studygroup.io.Retriever;
import rs.luka.android.studygroup.model.Group;

/**
 * Created by luka on 17.7.15..
 */
public class RootActivity extends SingleFragmentActivity implements GroupListFragment.Callbacks {
    public static final String EXTRA_GROUP_ID = "groupId";
    public static final String EXTRA_GROUP_NAME = "groupName";
    public static final String EXTRA_SHOW_LIST = "showList";
    private static final String TAG = "studygroup.RootActivity";

    @Override
    protected Fragment createFragment() {
        return new GroupListFragment();
    }

    @Override
    protected boolean shouldCreateFragment() {
        if (getIntent().getBooleanExtra(EXTRA_SHOW_LIST, false)) {
            Log.i(TAG, "showing list, extra supplied");
            return true;
        }
        //Log.i(TAG, "called from diff activity: " + getIntent().getPackage());
        if (!Retriever.isUserLoggedIn()) {
            //login
            return false;
        } else {
            List<Group> groups = Retriever.getGroups();
            if (groups.size() == 1) {
                Intent i = new Intent(this, GroupActivity.class);
                i.putExtra(EXTRA_GROUP_ID, groups.get(0).getId());
                i.putExtra(EXTRA_GROUP_NAME, groups.get(0).getName());
                Log.i(TAG, "one group, starting it");
                startActivity(i);
                return false;
            } else {
                Log.i(TAG, "multiple groups, showing list");
                return true;
            }
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
