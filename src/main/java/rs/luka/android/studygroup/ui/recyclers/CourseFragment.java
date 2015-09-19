package rs.luka.android.studygroup.ui.recyclers;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.ui.CursorAdapter;
import rs.luka.android.studygroup.ui.PoliteSwipeRefreshLayout;
import rs.luka.android.studygroup.ui.Snackbar;

/**
 * Created by luka on 3.7.15..
 */
public class CourseFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static String TAG = "studygroup.CourseFragment";

    private Course course;

    private RecyclerView   lessonsRecyclerView;
    private Callbacks      callbacks;
    private LessonsAdapter adapter;
    private PoliteSwipeRefreshLayout swipe;
    private CircularProgressView     progress;

    protected static CourseFragment newInstance(Course course) {
        Bundle args = new Bundle();
        args.putParcelable(CourseActivity.EXTRA_COURSE, course);

        CourseFragment f = new CourseFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        course = getArguments().getParcelable(CourseActivity.EXTRA_COURSE);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        progress = (CircularProgressView) view.findViewById(R.id.progress_view);
        lessonsRecyclerView = (RecyclerView) view.findViewById(R.id.lessons_recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        lessonsRecyclerView.setLayoutManager(layoutManager);
        registerForContextMenu(lessonsRecyclerView);

        setData();

        new ItemTouchHelper(new TouchHelperCallbacks(0,
                                                     ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT))
                .attachToRecyclerView(lessonsRecyclerView);
        swipe = (PoliteSwipeRefreshLayout) view.findViewById(R.id.course_swipe);
        swipe.setOnChildScrollUpListener(new PoliteSwipeRefreshLayout.OnChildScrollUpListener() {
            @Override
            public boolean canChildScrollUp() {
                return layoutManager.findFirstCompletelyVisibleItemPosition() != 0;
            }
        });
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipe.setColorSchemeResources(R.color.refresh_progress_1,
                                      R.color.refresh_progress_2,
                                      R.color.refresh_progress_3);

        return view;
    }

    public void stopRefreshing() {
        swipe.setRefreshing(false);
    }

    protected void refresh() {
        DataManager.refreshLessons(this, getActivity().getLoaderManager());
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(course.getSubject());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_rename:
                callbacks.onEdit(adapter.selectedTitle);
                return true;
        }
        return onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setData() {
        if (adapter == null) {
            adapter = new LessonsAdapter(getActivity(), null);
            lessonsRecyclerView.setAdapter(adapter);
        }
        DataManager.getLessons(this, getActivity().getLoaderManager());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        progress.setVisibility(View.VISIBLE);
        return course.getLessonLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
        data.moveToNext();
        if (data.getCount() == 0 && callbacks.handleLessonSkipping(0, "")) { return; }
        if (callbacks.handleLessonSkipping(data.getCount(), ((Database.LessonCursor) data).getLessonTitle())) {
            return;
        }
        data.moveToFirst();
        adapter.changeCursor(data);
        stopRefreshing();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    public interface Callbacks {
        void onLessonSelected(String title);

        void onEdit(String title);

        boolean handleLessonSkipping(int lessonNumber, String first);
    }

    private class TouchHelperCallbacks extends ItemTouchHelper.SimpleCallback {

        public TouchHelperCallbacks(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final LessonHolder swipedHolder = (LessonHolder) viewHolder;
            final String       lesson       = swipedHolder.title;
            final int noteCount = swipedHolder.noteCount, quesionCount = swipedHolder.questionCount, _id
                    = swipedHolder._id;
            course.hideLesson(getActivity(), lesson);
            refresh();
            // TODO: 4.9.15. http://stackoverflow.com/questions/32406144/hiding-and-re-showing-cards-in-recyclerview-backed-by-cursor
            Snackbar.make(lessonsRecyclerView,
                          R.string.course_hidden,
                          Snackbar.LENGTH_LONG)
                    .setOnHideListener(new Snackbar.OnHideListener() {
                        @Override
                        public void onHide() {
                            new RemoveLessonTask().execute(course, swipedHolder.title);
                        }
                    })
                    .setAction(R.string.undo,
                               new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       course.showLesson(getActivity(), _id, lesson, noteCount, quesionCount);
                                       refresh();
                                   }
                               })
                    .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                    .colorTheFuckingTextToWhite(getActivity())
                    .doStuffThatGoogleDidntFuckingDoProperly(getActivity(), null)
                    .show();
            //viewHolder.itemView.setVisibility(View.GONE); //ne radi
        }
    }

    private class RemoveLessonTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            ((Course) params[0]).removeLesson(getActivity(), (String) params[1]);
            return null;
        }
    }

    private class LessonHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                          View.OnLongClickListener,
                                                                          View.OnCreateContextMenuListener {
        private final TextView titleTextView;
        private final TextView noteCountTextView;
        private final TextView TextView;

        private String title;
        private int noteCount;
        private int questionCount;
        private int _id;

        public LessonHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnCreateContextMenuListener(this);

            titleTextView = (TextView) itemView.findViewById(R.id.card_lesson_title);
            noteCountTextView = (TextView) itemView.findViewById(R.id.note_count_text);
            TextView = (TextView) itemView.findViewById(R.id.question_count_text);
        }

        public void bindLesson(String title, int noteNo, int questionNo, int _id) {
            this.title = title;
            this.noteCount = noteNo;
            this.questionCount = questionNo;
            this._id = _id;
            titleTextView.setText(title);
            noteCountTextView.setText(getString(R.string.notes_no, noteNo));
            TextView.setText(getString(R.string.questions_no, questionNo));
        }

        @Override
        public void onClick(View v) {
            callbacks.onLessonSelected(title);
        }

        @Override
        public boolean onLongClick(View v) {
            adapter.selectedTitle = title;
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            getActivity().getMenuInflater().inflate(R.menu.context_course, menu);
        }
    }

    private class LessonsAdapter extends CursorAdapter<LessonHolder> {

        private String selectedTitle;

        public LessonsAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public LessonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.card_lesson,
                                               parent,
                                               false);
            return new LessonHolder(view);
        }

        @Override
        public void onBindViewHolder(LessonHolder holder, Cursor data) {
            holder.bindLesson(((Database.LessonCursor) data).getLessonTitle(),
                              ((Database.LessonCursor) data).getNoteCount(),
                              ((Database.LessonCursor) data).getQuestionCount(),
                              data.getInt(data.getColumnIndex("_id")));
        }

        public void removeTitle(RecyclerView.ViewHolder holder) {
            //todo
        }

        public void addTitle(String title, int position) {
            //todo
        }
    }
}
