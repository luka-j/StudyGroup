package rs.luka.android.studygroup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import rs.luka.android.studygroup.model.Question;

/**
 * Created by luka on 12.7.15..
 */
public class QuestionPagerActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private List<Question> questions;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_data);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        questions = (List<Question>) getIntent().getSerializableExtra(LessonActivity.EXTRA_LIST_QUESTIONS);
        FragmentManager fm = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return questions.size();
            }

            @Override
            public Fragment getItem(int pos) {
                return QuestionFragment.newInstance(questions.get(pos));

            }
        });
        viewPager.setCurrentItem(getIntent().getIntExtra(LessonActivity.EXTRA_CURRENT_QUESTION, 0));
    }
}
