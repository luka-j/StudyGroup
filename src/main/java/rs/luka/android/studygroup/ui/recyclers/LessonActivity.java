package rs.luka.android.studygroup.ui.recyclers;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.google.SlidingTabLayout;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.ui.singleitemactivities.AddNoteActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.AddQuestionActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.NotePagerActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.QuestionPagerActivity;

/**
 * Created by luka on 5.7.15..
 */
public class LessonActivity extends AppCompatActivity
        implements NoteListFragment.NoteCallbacks, QuestionListFragment.QuestionCallbacks {

    public static final String EXTRA_CURRENT_NOTE       = "noteIndex";
    public static final String EXTRA_CURRENT_QUESTION   = "questionIndex";
    public static final String EXTRA_CURRENT_LESSON     = CourseActivity.EXTRA_LESSON_NAME;
    public static final String EXTRA_CURRENT_COURSE     = CourseActivity.EXTRA_COURSE;
    public static final String EXTRA_SELECTED_NOTES     = "selNotes";
    public static final String EXTRA_SELECTED_QUESTIONS = "selQuestions";

    private int numOfTabs = 2;
    private Course course;
    private String lessonName;

    private Toolbar          toolbar;
    private ViewPager        pager;
    private ViewPagerAdapter adapter;
    private SlidingTabLayout tabs;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        course = getIntent().getParcelableExtra(CourseActivity.EXTRA_COURSE);
        lessonName = getIntent().getStringExtra(CourseActivity.EXTRA_LESSON_NAME);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(lessonName);
        setSupportActionBar(toolbar);

        if (NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        adapter = new ViewPagerAdapter(getSupportFragmentManager(),
                                       new String[]{getString(R.string.notes),
                                                    getString(R.string.questions)}, numOfTabs);

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
                    i.putExtra(EXTRA_CURRENT_COURSE,
                               getIntent().getParcelableExtra(CourseActivity.EXTRA_COURSE));
                    startActivity(i);
                } else {
                    Intent i = new Intent(This, AddQuestionActivity.class);
                    i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
                    i.putExtra(EXTRA_CURRENT_COURSE,
                               getIntent().getParcelableExtra(CourseActivity.EXTRA_COURSE));
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
                if (course.getNumberOfLessons() == 1) {//nervira me animacija na Lollipopu, nije prirodna
                    Intent i = new Intent(this, CourseActivity.class); //pa je izbegavam kad nije needed
                    i.putExtra(GroupActivity.EXTRA_COURSE,
                               getIntent().getSerializableExtra(CourseActivity.EXTRA_COURSE));
                    startActivity(i);
                    Log.i("test", "starting activity on id.home");
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNoteSelected(Note note) {
        Intent i = new Intent(this, NotePagerActivity.class);
        i.putExtra(EXTRA_CURRENT_COURSE, course);
        i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
        i.putExtra(EXTRA_CURRENT_NOTE, note);
        startActivity(i);
    }

    @Override
    public void onNotesEdit(Set<Note> notes) {
        ArrayList<Note> l = new ArrayList<>(notes);
        Collections.sort(l);
        Intent i = new Intent(this, AddNoteActivity.class);
        i.putParcelableArrayListExtra(EXTRA_SELECTED_NOTES, l);
        i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
        i.putExtra(EXTRA_CURRENT_COURSE, course);
        startActivity(i);
    }

    @Override
    public void onQuestionSelected(Question question) {
        Intent i = new Intent(this, QuestionPagerActivity.class);
        i.putExtra(EXTRA_CURRENT_COURSE, course);
        i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
        i.putExtra(EXTRA_CURRENT_QUESTION, question);
        startActivity(i);
    }

    @Override
    public void onQuestionsEdit(Set<Question> questions) {
        ArrayList<Question> l = new ArrayList<>(questions);
        Collections.sort(l);
        Intent i = new Intent(this, AddQuestionActivity.class);
        i.putParcelableArrayListExtra(EXTRA_SELECTED_QUESTIONS, l);
        i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
        i.putExtra(EXTRA_CURRENT_COURSE, course);
        startActivity(i);
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {

        CharSequence[] titles;
        int            numOfTabs;

        public ViewPagerAdapter(FragmentManager fm, CharSequence[] mTitles, int numofTabs) {
            super(fm);

            this.titles = mTitles;
            this.numOfTabs = numofTabs;

        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return NoteListFragment.newInstance(course, lessonName);
            } else {
                return QuestionListFragment.newInstance(course, lessonName);
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return numOfTabs;
        }
    }
}
