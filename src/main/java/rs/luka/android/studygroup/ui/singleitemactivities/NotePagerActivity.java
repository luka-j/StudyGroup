package rs.luka.android.studygroup.ui.singleitemactivities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.ui.recyclers.LessonActivity;

/**
 * Created by luka on 12.7.15..
 */
public class NotePagerActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private List<Note> notes;
    private Note      callingNote;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_data);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        notes = ((Course) getIntent().getParcelableExtra(LessonActivity.EXTRA_CURRENT_COURSE))
                .getNotesByLesson(getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_LESSON));
        callingNote = getIntent().getParcelableExtra(LessonActivity.EXTRA_CURRENT_NOTE);
        FragmentManager fm = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return notes.size();
            }

            @Override
            public Fragment getItem(int pos) {
                return NoteFragment.newInstance(notes.get(pos));

            }
        });
        viewPager.setCurrentItem(notes.indexOf(callingNote));
    }
}
