package rs.luka.android.studygroup.ui.recyclers;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.View;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.dialogs.AddAnnouncementDialog;

/**
 * Created by luka on 29.10.16..
 */
public class AnnouncementsActivity extends SingleFragmentActivity {
    public static final String EXTRA_GROUP = "group";
    private AnnouncementsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if(getIntent().<Group>getParcelableExtra(EXTRA_GROUP).getPermission() < Group.PERM_OWNER) {
            fab.setVisibility(View.GONE);
        } else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddAnnouncementDialog.newInstance(getIntent().<Group>getParcelableExtra(EXTRA_GROUP))
                                         .show(getFragmentManager(), "");
                }
            });
        }
    }

    @Override
    protected Fragment createFragment() {
        fragment = AnnouncementsFragment.newInstance(getIntent().<Group>getParcelableExtra(EXTRA_GROUP));
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_fab_fragment;
    }
}
