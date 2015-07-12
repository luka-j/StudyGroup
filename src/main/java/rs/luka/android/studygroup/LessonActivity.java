package rs.luka.android.studygroup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.UUID;

import rs.luka.android.studygroup.google.SlidingTabLayout;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.model.Question;

/**
 * Created by luka on 5.7.15..
 */
public class LessonActivity extends AppCompatActivity implements NoteListFragment.NoteCallbacks, QuestionListFragment.QuestionCallbacks {

    public static final String EXTRA_NOTE = "note";
    public static final String EXTRA_QUESTION = "question";


    private Toolbar toolbar;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private int numOfTabs =2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra(CourseActivity.EXTRA_LESSON_NAME));
        setSupportActionBar(toolbar);

        if(NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),
                new String[]{getString(R.string.notes), getString(R.string.questions)}, numOfTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Assigning the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.white);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNoteSelected(Note note) {
        Intent i = new Intent(this, NoteActivity.class);
        i.putExtra(EXTRA_NOTE, note);
        startActivity(i);
    }

    @Override
    public void onQuestionSelected(Question question) {
        Intent i = new Intent(this, QuestionActivity.class);
        i.putExtra(EXTRA_QUESTION, question);
        startActivity(i);
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {

        CharSequence titles[]; // This will Store the titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
        int numOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


        // Build a Constructor and assign the passed Values to appropriate values in the class
        public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int numofTabs) {
            super(fm);

            this.titles = mTitles;
            this.numOfTabs = numofTabs;

        }

        //This method return the fragment for the every position in the View Pager
        @Override
        public Fragment getItem(int position) {
            Intent i = getIntent();
            if (position == 0) {
                return NoteListFragment.newInstance((UUID) i.getSerializableExtra(CourseActivity.EXTRA_COURSE_ID),
                        i.getStringExtra(CourseActivity.EXTRA_LESSON_NAME));
            } else {
                return QuestionListFragment.newInstance((UUID) i.getSerializableExtra(CourseActivity.EXTRA_COURSE_ID),
                        i.getStringExtra(CourseActivity.EXTRA_LESSON_NAME));
            }

        }

        // This method return the titles for the Tabs in the Tab Strip

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        // This method return the Number of tabs for the tabs Strip

        @Override
        public int getCount() {
            return numOfTabs;
        }
    }
}
