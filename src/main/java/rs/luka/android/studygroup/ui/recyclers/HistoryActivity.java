package rs.luka.android.studygroup.ui.recyclers;

import android.app.Fragment;

import rs.luka.android.studygroup.ui.SingleFragmentActivity;

/**
 * Created by luka on 2.2.16..
 */
public class HistoryActivity extends SingleFragmentActivity {
    public static final String EXTRA_ITEM = "historyitem";

    @Override
    protected Fragment createFragment() {
        return HistoryFragment.newInstance(getIntent().getParcelableExtra(EXTRA_ITEM));
    }
}
