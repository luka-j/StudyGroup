package rs.luka.android.studygroup.ui.recyclers;

import android.support.v4.app.Fragment;

import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;

public class MemberListActivity extends SingleFragmentActivity {
    public static final String EXTRA_GROUP = "group";

    @Override
    protected Fragment createFragment() {
        return MemberListFragment.newInstance((Group) getIntent().getParcelableExtra(EXTRA_GROUP));
    }
}
