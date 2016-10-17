package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.backgroundtasks.NoteTasks;
import rs.luka.android.studygroup.io.database.NoteTable;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.CursorFragmentStatePagerAdapter;
import rs.luka.android.studygroup.ui.recyclers.LessonActivity;

/**
 * Created by luka on 12.7.15..
 */
public class NotePagerActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private NetworkExceptionHandler         exceptionHandler;
    private ViewPager                       viewPager;
    private Course                          course;
    private String                          lesson;
    private int notePosition;
    private CursorFragmentStatePagerAdapter adapter;
    private int permission;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_data);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler(this);

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        course = getIntent().getParcelableExtra(LessonActivity.EXTRA_CURRENT_COURSE);
        lesson = getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_LESSON);
        notePosition = getIntent().getIntExtra(LessonActivity.EXTRA_CURRENT_NOTE_POSITION, 0);
        permission = getIntent().getIntExtra(LessonActivity.EXTRA_MY_PERMISSION, Group.PERM_READ_PUBLIC);
        adapter = new NoteAdapter(this, this.getSupportFragmentManager(), null);
        NoteTasks.getNotes(this, course.getIdValue(), lesson, this, this.getSupportLoaderManager(), exceptionHandler);

        viewPager.setAdapter(adapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return course.getNotesLoader(this, lesson);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
        viewPager.setCurrentItem(notePosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private class NoteAdapter extends CursorFragmentStatePagerAdapter {

        public NoteAdapter(Context context, FragmentManager fm,
                           Cursor cursor) {
            super(context, fm, cursor);
        }

        @Override
        public Fragment getItem(Context context, Cursor cursor) {
            return NoteFragment.newInstance(course.getSubject(), ((NoteTable.NoteCursor) cursor).getNote(), permission);
        }
    }
}
