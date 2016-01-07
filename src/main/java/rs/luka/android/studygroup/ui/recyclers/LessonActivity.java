package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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

    public static final  String EXTRA_CURRENT_NOTE_POSITION     = "noteIndex";
    public static final  String EXTRA_CURRENT_QUESTION_POSITION = "questionIndex";
    public static final String EXTRA_CURRENT_LESSON     = CourseActivity.EXTRA_LESSON_NAME;
    public static final String EXTRA_CURRENT_COURSE     = CourseActivity.EXTRA_COURSE;
    public static final String EXTRA_SELECTED_NOTES     = "selNotes";
    public static final String EXTRA_SELECTED_QUESTIONS = "selQuestions";
    public static final  String EXTRA_LESSON                    = CourseActivity.EXTRA_LESSON_NAME;
    private static final String TAG                             = "studygroup.LessonActivity";
    private static final int REQUEST_ADD_NOTE      = 0;
    private static final int REQUEST_ADD_QUESTION  = 1;
    private static final int    REQUEST_EDIT_NOTE               = 2;
    private static final int    REQUEST_EDIT_QUESTION           = 3;

    private static final int TABS_COUNT = 2;
    private Course course;
    private String lessonName;

    private Toolbar             toolbar;
    private ViewPager           pager;
    private NoteQuestionAdapter adapter;
    private SlidingTabLayout    tabs;
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        adapter = new NoteQuestionAdapter(getSupportFragmentManager(),
                                          new String[]{getString(R.string.notes),
                                                       getString(R.string.questions)}, TABS_COUNT);
        pager = (ViewPager) findViewById(R.id.tab_pager);
        pager.setAdapter(adapter);

        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true);
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.white);
            }
        });
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
                    startActivityForResult(i, REQUEST_ADD_NOTE);
                } else {
                    Intent i = new Intent(This, AddQuestionActivity.class);
                    i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
                    i.putExtra(EXTRA_CURRENT_COURSE,
                               getIntent().getParcelableExtra(CourseActivity.EXTRA_COURSE));
                    startActivityForResult(i, REQUEST_ADD_QUESTION);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_NOTE:
            case REQUEST_EDIT_NOTE:
                updateNote(data);
                updateQuestion(data); // TODO: 10.9.15. optimize (note to self: getAdapter() mora sa UI thread-a)
                break;
            case REQUEST_ADD_QUESTION:
            case REQUEST_EDIT_QUESTION:
                updateQuestion(data);
                updateNote(data);
        }
    }

    private void updateNote(Intent data) {
        NoteListFragment noteListFragment = ((NoteListFragment) ((NoteQuestionAdapter) (pager.getAdapter()))
                .getRegisteredFragment(0));
        if (noteListFragment != null && data != null) {
            lessonName = data.getStringExtra(EXTRA_LESSON);
            noteListFragment.refreshForLesson(lessonName);
        }
    }

    private void updateQuestion(Intent data) {
        QuestionListFragment questionListFragment
                = ((QuestionListFragment) ((NoteQuestionAdapter) (pager.getAdapter())).getRegisteredFragment(1));
        if (questionListFragment != null && data != null) {
            lessonName = data.getStringExtra(EXTRA_LESSON);
            questionListFragment.refreshForLesson(lessonName);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lesson, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this,
                                         CourseActivity.class).putExtra(CourseActivity.EXTRA_GO_BACKWARD,
                                                                        false)
                                                              .putExtra(CourseActivity.EXTRA_COURSE,
                                                                        course));
                return true;
            case R.id.show_all:
                // TODO: 9.9.15.
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, CourseActivity.class).putExtra(CourseActivity.EXTRA_GO_BACKWARD,
                                                                      true)
                                                            .putExtra(CourseActivity.EXTRA_COURSE,
                                                                      course));
    }

    @Override
    public void onNoteSelected(int position) {
        Intent i = new Intent(this, NotePagerActivity.class);
        i.putExtra(EXTRA_CURRENT_COURSE, course);
        i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
        i.putExtra(EXTRA_CURRENT_NOTE_POSITION, position);
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
        startActivityForResult(i, REQUEST_EDIT_NOTE);
    }

    @Override
    public void onQuestionSelected(int questionPosition) {
        Intent i = new Intent(this, QuestionPagerActivity.class);
        i.putExtra(EXTRA_CURRENT_COURSE, course);
        i.putExtra(EXTRA_CURRENT_LESSON, lessonName);
        i.putExtra(EXTRA_CURRENT_QUESTION_POSITION, questionPosition);
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
        startActivityForResult(i, REQUEST_EDIT_QUESTION);
    }

    public FloatingActionButton getFab() {
        return fab;
    }

    /**
     * Verovatno nije thread-safe. Nije testirano.
     */
    private class UpdateNoteTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            NoteListFragment noteListFragment = (NoteListFragment) params[0];
            Intent           data             = (Intent) params[1];
            if (noteListFragment != null && data != null) {
                String lessonName = data.getStringExtra(EXTRA_LESSON);
                noteListFragment.refreshForLesson(lessonName);
            }
            return null;
        }
    }

    private class NoteQuestionAdapter extends FragmentStatePagerAdapter {
        SparseArray<Fragment> registeredFragments = new SparseArray<>();

        CharSequence[] titles;
        int            numOfTabs;

        public NoteQuestionAdapter(FragmentManager fm, CharSequence[] mTitles, int numofTabs) {
            super(fm);

            this.titles = mTitles;
            this.numOfTabs = numofTabs;

        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                QuestionListFragment qlf = (QuestionListFragment) registeredFragments.get(1);
                if (qlf != null) {
                    qlf.dismissSnackbar(); // TODO: 5.9.15. fix
                }
                return NoteListFragment.newInstance(course, lessonName);
            } else {
                NoteListFragment nlf = (NoteListFragment) registeredFragments.get(0);
                if (registeredFragments.size() > 0) {
                    nlf.dismissSnackbar(); // TODO: 5.9.15. fix
                }
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

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }
}
