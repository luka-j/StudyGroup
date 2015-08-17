package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.List;

import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.AddGroupActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.LoginActivity;

/**
 * Created by luka on 17.7.15..
 */
public class RootActivity extends SingleFragmentActivity implements GroupListFragment.Callbacks {
    public static final  String EXTRA_GROUP     = "exGroup";
    public static final  String EXTRA_SHOW_LIST = "showList";
    private static final String TAG             = "studygroup.RootActivity";

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
        if (!User.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            return false;
        } else {
            List<Group> groups = User.getInstance().getGroups();
            if (groups.size() == 1) {
                Intent i = new Intent(this, GroupActivity.class);
                i.putExtra(EXTRA_GROUP, groups.get(0));
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
        i.putExtra(EXTRA_GROUP, group);
        startActivity(i);
    }

    @Override
    public void onEditGroup(Group group) {
        Intent i = new Intent(this, AddGroupActivity.class);
        i.putExtra(EXTRA_GROUP, group);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
