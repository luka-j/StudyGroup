package rs.luka.android.studygroup.activities.singleitemactivities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.activities.recyclers.LessonActivity;
import rs.luka.android.studygroup.model.Note;

/**
 * Created by luka on 12.7.15..
 */
public class NotePagerActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private List<Note> notes;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_data);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        notes = (List<Note>) getIntent().getSerializableExtra(LessonActivity.EXTRA_LIST_NOTES);
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
        viewPager.setCurrentItem(getIntent().getIntExtra(LessonActivity.EXTRA_CURRENT_NOTE, 0));
    }
}
