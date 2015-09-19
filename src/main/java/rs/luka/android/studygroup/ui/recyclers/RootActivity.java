// TODO: 11.9.15. Finish EXTRA_ Strings cleanup (od LessonActivity pa nadalje)
package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.AddGroupActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.LoginActivity;

/**
 * Created by luka on 17.7.15.
 */
public class RootActivity extends SingleFragmentActivity implements GroupListFragment.Callbacks {
    private static final String TAG = "studygroup.RootActivity";

    @Override
    protected Fragment createFragment() {
        return new GroupListFragment();
    }

    @Override
    protected boolean shouldCreateFragment() {
        if (getIntent().getBooleanExtra(GroupActivity.EXTRA_SHOW_LIST, false)) {
            Log.i(TAG, "showing list, extra supplied");
            return true;
        }
        //Log.i(TAG, "called from diff activity: " + getIntent().getPackage());
        if (!User.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            return false;
        } else {
            if (DataManager.getGroupCount(this) == 1) {
                Intent i = new Intent(this, GroupActivity.class);
                Database.GroupCursor c = Database.getInstance(this).queryGroups();
                c.moveToNext();
                i.putExtra(GroupActivity.EXTRA_GROUP, c.getGroup());
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
        i.putExtra(GroupActivity.EXTRA_GROUP, group);
        startActivity(i);
    }

    @Override
    public void onEditGroup(Group group, int requestCode) {
        Intent i = new Intent(this, AddGroupActivity.class);
        i.putExtra(GroupActivity.EXTRA_GROUP, group);
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
