package rs.luka.android.studygroup.ui.singleitemactivities;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.ui.CursorFragmentStatePagerAdapter;
import rs.luka.android.studygroup.ui.recyclers.LessonActivity;

/**
 * Created by luka on 12.7.15..
 */
public class QuestionPagerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private ViewPager viewPager;
    private CursorFragmentStatePagerAdapter adapter;
    private Course                          course;
    private String                          lesson;
    private Question  callingQuestion;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_data);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        course = getIntent().getParcelableExtra(LessonActivity.EXTRA_CURRENT_COURSE);
        lesson = getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_LESSON);
        callingQuestion = getIntent().getParcelableExtra(LessonActivity.EXTRA_CURRENT_QUESTION);

        adapter = new QuestionAdapter(this, this.getSupportFragmentManager(), null);
        DataManager.getNotes(this, this.getLoaderManager());

        viewPager.setAdapter(adapter);
        //todo set current item
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return course.getQuestionsLoader(this, lesson);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private static class QuestionAdapter extends CursorFragmentStatePagerAdapter {

        public QuestionAdapter(Context context, FragmentManager fm,
                               Cursor cursor) {
            super(context, fm, cursor);
        }

        @Override
        public Fragment getItem(Context context, Cursor cursor) {
            return QuestionFragment.newInstance(((Database.QuestionCursor) cursor).getQuestion());
        }
    }
}
