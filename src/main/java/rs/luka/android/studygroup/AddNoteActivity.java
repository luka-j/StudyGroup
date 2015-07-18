package rs.luka.android.studygroup;

import android.support.v4.app.Fragment;

/**
 * Created by luka on 14.7.15..
 */
public class AddNoteActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return AddNoteFragment.newInstance(getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_LESSON),
                getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_COURSE));
    }
}
