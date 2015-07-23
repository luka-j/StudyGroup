package rs.luka.android.studygroup.activities.recyclers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.activities.singleitemactivities.AddNoteActivity;
import rs.luka.android.studygroup.activities.singleitemactivities.AddQuestionActivity;
import rs.luka.android.studygroup.activities.singleitemactivities.NotePagerActivity;
import rs.luka.android.studygroup.activities.singleitemactivities.QuestionPagerActivity;
import rs.luka.android.studygroup.google.SlidingTabLayout;
import rs.luka.android.studygroup.io.Retriever;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.model.Question;

/**
 * Created by luka on 5.7.15..
 */
public class LessonActivity extends AppCompatActivity implements NoteListFragment.NoteCallbacks, QuestionListFragment.QuestionCallbacks {

    public static final String EXTRA_LIST_NOTES = "noteList";
    public static final String EXTRA_CURRENT_NOTE = "noteIndex";
    public static final String EXTRA_LIST_QUESTIONS = "questionList";
    public static final String EXTRA_CURRENT_QUESTION = "questionIndex";
    public static final String EXTRA_CURRENT_LESSON = CourseActivity.EXTRA_LESSON_NAME;
    public static final String EXTRA_CURRENT_COURSE = CourseActivity.EXTRA_COURSE_NAME;

    private int numOfTabs = 2;
    private UUID courseId;
    private String lessonName;

    private Toolbar toolbar;
    private ViewPager pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        courseId = (UUID) getIntent().getSerializableExtra(CourseActivity.EXTRA_COURSE_ID);
        lessonName = getIntent().getStringExtra(CourseActivity.EXTRA_LESSON_NAME);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(lessonName);
        setSupportActionBar(toolbar);

        if(NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),
                new String[]{getString(R.string.notes), getString(R.string.questions)}, numOfTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.tab_pager);
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

        fab = (FloatingActionButton) findViewById(R.id.fab_add_noqu);
        final Activity This = this;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pager.getCurrentItem() == 0) {
                    Intent i = new Intent(This, AddNoteActivity.class);
                    i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
                    i.putExtra(EXTRA_CURRENT_COURSE, getIntent().getStringExtra(CourseActivity.EXTRA_COURSE_NAME));
                    startActivity(i);
                } else {
                    Intent i = new Intent(This, AddQuestionActivity.class);
                    i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
                    i.putExtra(EXTRA_CURRENT_COURSE, getIntent().getStringExtra(CourseActivity.EXTRA_COURSE_NAME));
                    startActivity(i);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                //TODO: vidi zasto NavUtils.NavigateUpFromSameTask ne radi
                Intent i = new Intent(this, CourseActivity.class);
                i.putExtra(GroupActivity.EXTRA_COURSE_ID,
                           getIntent().getSerializableExtra(CourseActivity.EXTRA_COURSE_ID));
                i.putExtra(GroupActivity.EXTRA_COURSE_NAME,
                           getIntent().getSerializableExtra(CourseActivity.EXTRA_COURSE_NAME));
                startActivity(i);
                Log.i("test", "starting activity on id.home");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        /*if(Retriever.getNumberOfLessons(courseId) == 1) { //nije neophodno, ali me nervira animacija na Lollipopu
            Intent i = new Intent(this, CourseActivity.class); //neprirodna je, s obzirom da se vracam nazad
            i.putExtra(CourseActivity.EXTRA_GO_BACKWARD, true); //pa je izbegavam koliko je moguce
            startActivity(i);
            Log.i("test", "started activity; going back");
        }
        else {*/
        super.onBackPressed();
        //}
    }

    @Override
    public void onNoteSelected(Note note) {
        List<Note> l = Retriever.getExistingNotes(courseId, lessonName);
        Intent i = new Intent(this, NotePagerActivity.class);
        //standardne implementacije (arraylist/linkedlist) su Serializable
        i.putExtra(EXTRA_LIST_NOTES, (Serializable) l);
        i.putExtra(EXTRA_CURRENT_NOTE, l.indexOf(note));
        startActivity(i);
    }

    @Override
    public void onQuestionSelected(Question question) {
        List<Question> l = Retriever.getExistingQuestions(courseId, lessonName);
        Intent i = new Intent(this, QuestionPagerActivity.class);
        //standardne implementacije (arraylist/linkedlist) su Serializable
        i.putExtra(EXTRA_LIST_QUESTIONS, (Serializable) l);
        i.putExtra(EXTRA_CURRENT_QUESTION, l.indexOf(question));
        startActivity(i);
    }

    protected FloatingActionButton getFab() {
        return fab;
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {

        CharSequence[] titles; // This will Store the titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
        int numOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created


        // Build a Constructor and assign the passed Values to appropriate values in the class
        public ViewPagerAdapter(FragmentManager fm, CharSequence[] mTitles, int numofTabs) {
            super(fm);

            this.titles = mTitles;
            this.numOfTabs = numofTabs;

        }

        //This method return the fragment for the every position in the View Pager
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return NoteListFragment.newInstance(courseId, lessonName);
            } else {
                return QuestionListFragment.newInstance(courseId, lessonName);
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