package rs.luka.android.studygroup;

import android.support.v4.app.Fragment;

import rs.luka.android.studygroup.model.Note;

/**
 * Created by luka on 12.7.15..
 */
public class NoteActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return NoteFragment.newInstance((Note) getIntent().getSerializableExtra(LessonActivity.EXTRA_NOTE));
    }
}
