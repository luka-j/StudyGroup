package rs.luka.android.studygroup.activities.singleitemactivities;

import android.support.v4.app.Fragment;

import rs.luka.android.studygroup.activities.SingleFragmentActivity;

/**
 * Created by luka on 18.7.15..
 */
public class AddGroupActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new AddGroupFragment();
    }
}
