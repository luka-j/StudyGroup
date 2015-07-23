package rs.luka.android.studygroup.activities.singleitemactivities;

import android.support.v4.app.Fragment;

import rs.luka.android.studygroup.activities.SingleFragmentActivity;
import rs.luka.android.studygroup.activities.recyclers.LessonActivity;

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
