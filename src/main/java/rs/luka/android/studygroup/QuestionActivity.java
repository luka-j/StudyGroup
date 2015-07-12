package rs.luka.android.studygroup;

import android.support.v4.app.Fragment;

import rs.luka.android.studygroup.model.Question;

/**
 * Created by luka on 12.7.15..
 */
public class QuestionActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return QuestionFragment.newInstance((Question) getIntent().getSerializableExtra(LessonActivity.EXTRA_QUESTION));
    }
}
