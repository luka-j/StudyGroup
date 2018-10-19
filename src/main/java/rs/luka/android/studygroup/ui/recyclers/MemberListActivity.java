package rs.luka.android.studygroup.ui.recyclers;

import android.app.Fragment;
import android.support.v4.app.DialogFragment;

import rs.luka.android.studygroup.io.network.Groups;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.ConfirmDialog;

public class MemberListActivity extends SingleFragmentActivity implements ConfirmDialog.Callbacks {
    public static final String EXTRA_GROUP = "group";
    private MemberListFragment fragment;

    @Override
    protected Fragment createFragment() {
        fragment = MemberListFragment.newInstance(getIntent().<Group>getParcelableExtra(EXTRA_GROUP));
        return fragment;
    }

    @Override
    public void onPositive(DialogFragment dialog) {
        Groups.revokeMember(MemberListFragment.REQUEST_CHANGE_PERMISSION,
                            getIntent().<Group>getParcelableExtra(EXTRA_GROUP).getIdValue(),
                            Long.parseLong(dialog.getTag()), fragment);
        fragment.showSpinner();
    }

    @Override
    public void onNegative(DialogFragment dialog) {
        ;
    }
}
