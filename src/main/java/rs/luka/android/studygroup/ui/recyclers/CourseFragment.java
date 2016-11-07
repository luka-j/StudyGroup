package rs.luka.android.studygroup.ui.recyclers;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.IOException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.backgroundtasks.LessonTasks;
import rs.luka.android.studygroup.io.database.LessonTable;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.network.Lessons;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.ui.CursorAdapter;
import rs.luka.android.studygroup.ui.PoliteSwipeRefreshLayout;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;

//import rs.luka.android.studygroup.ui.Snackbar;

/**
 * Created by luka on 3.7.15..
 */
public class CourseFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
                                                        Network.NetworkCallbacks<String> {

    protected static final int REQUEST_REMOVE_LESSON = 1;
    private static final int REQUEST_SHOW_ALL      = 0;
    private static String TAG = "studygroup.CourseFragment";
    private Course course;
    private int permission;
    private NetworkExceptionHandler exceptionHandler;

    private RecyclerView   lessonsRecyclerView;
    private Callbacks      callbacks;
    private LessonsAdapter adapter;
    private PoliteSwipeRefreshLayout swipe;
    private CircularProgressView     progress;

    protected static CourseFragment newInstance(Course course, int permission) {
        Bundle args = new Bundle();
        args.putParcelable(CourseActivity.EXTRA_COURSE, course);
        args.putInt(CourseActivity.EXTRA_MY_PERMISSION, permission);

        CourseFragment f = new CourseFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        course = getArguments().getParcelable(CourseActivity.EXTRA_COURSE);
        permission = getArguments().getInt(CourseActivity.EXTRA_MY_PERMISSION);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler((AppCompatActivity)activity);
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
        if(permission >= Group.PERM_MODIFY)
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
        LessonTasks.refreshLessons(getContext(), course.getIdValue(),
                                   this, getActivity().getSupportLoaderManager(), exceptionHandler);
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
            case R.id.context_remove_lesson:
                callbacks.removeLesson(adapter.selectedTitle);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.show_all_lessons:
                Lessons.showAllLessons(REQUEST_SHOW_ALL, course.getIdValue(), this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setData() {
        if (adapter == null) {
            adapter = new LessonsAdapter(getActivity(), null);
            lessonsRecyclerView.setAdapter(adapter);
        }
        LessonTasks.getLessons(getContext(), course.getIdValue(), this, getActivity().getSupportLoaderManager(), exceptionHandler);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.VISIBLE);
            }
        });
        return course.getLessonLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
        data.moveToNext();
        if (data.getCount() == 0 && callbacks.handleLessonSkipping(0, "")) { return; }
        if (callbacks.handleLessonSkipping(data.getCount(), ((LessonTable.LessonCursor) data).getLessonTitle())) {
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

    @Override
    public void onRequestCompleted(int id, Network.Response<String> response) {
        switch (id) {
            case REQUEST_SHOW_ALL:
            case REQUEST_REMOVE_LESSON:
                if (response.responseCode == Network.Response.RESPONSE_OK) {
                    getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
                } else {
                    response.handleErrorCode(exceptionHandler);
                }
                break;
            default:
                Log.w(TAG, "Invalid request id: " + id);
        }
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        if(ex instanceof IOException)
            exceptionHandler.handleIOException((IOException)ex);
        else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog.newInstance(getString(R.string.error_unknown_ex_title),
                                           getString(R.string.error_unknown_ex_text))
                            .show(getFragmentManager(), "");
                }
            });
            Log.e(TAG, "Unknown Throwable caught", ex);
        }
    }

    public interface Callbacks {
        void onLessonSelected(String title, boolean isPrivate);

        void onEdit(String title);

        boolean handleLessonSkipping(int lessonNumber, String first);

        void removeLesson(String title);
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
            LessonHolder swipedHolder = (LessonHolder) viewHolder;
            final String       lesson       = swipedHolder.title;
            final int noteCount = swipedHolder.noteCount, questionCount = swipedHolder.questionCount, _id
                    = swipedHolder._id;
            course.shallowHideLesson(getActivity(), lesson);
            getActivity().getSupportLoaderManager().restartLoader(LessonTasks.LOADER_ID, null, CourseFragment.this);
            // TODO: 4.9.15. http://stackoverflow.com/questions/32406144/hiding-and-re-showing-cards-in-recyclerview-backed-by-cursor
            Snackbar snackbar = Snackbar.make(lessonsRecyclerView,
                                              R.string.lesson_hidden,
                                              Snackbar.LENGTH_LONG)
                                        .setCallback(new Snackbar.Callback() {
                                            @Override
                                            public void onDismissed(Snackbar snackbar, int event) {
                                                course.hideLesson(getContext(), lesson, exceptionHandler);
                                            }
                                        })
                                        .setAction(R.string.undo,
                                                   new View.OnClickListener() {
                                                       @Override
                                                       public void onClick(View v) {
                                                           course.showLesson(getActivity(), _id, lesson, noteCount, questionCount);
                                                           refresh();
                                                       }
                                                   })
                                        .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                    //.colorTheFuckingTextToWhite(getActivity())
                    //.doStuffThatGoogleDidntFuckingDoProperly(getActivity(), null)
                    ;
            ((TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text))
                    .setTextColor(getActivity().getResources().getColor(R.color.white));
            snackbar.show();
            //viewHolder.itemView.setVisibility(View.GONE); //ne radi
        }
    }

    private class LessonHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                          View.OnLongClickListener,
                                                                          View.OnCreateContextMenuListener {
        private final TextView titleTextView;
        private final TextView noteCountTextView;
        private final TextView questionCountTextView;
        private final TextView privateTextView;

        private String title;
        private int noteCount;
        private int questionCount;
        private int _id;
        private int permission;

        public LessonHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnCreateContextMenuListener(this);

            titleTextView = (TextView) itemView.findViewById(R.id.card_lesson_title);
            noteCountTextView = (TextView) itemView.findViewById(R.id.note_count_text);
            questionCountTextView = (TextView) itemView.findViewById(R.id.question_count_text);
            privateTextView = (TextView) itemView.findViewById(R.id.card_lesson_private);
        }

        public void bindLesson(String title, int noteNo, int questionNo, int permission, int _id) {
            this.title = title;
            this.noteCount = noteNo;
            this.questionCount = questionNo;
            this._id = _id;
            this.permission = permission;
            titleTextView.setText(title);
            noteCountTextView.setText(getString(R.string.notes_no, noteNo));
            questionCountTextView.setText(getString(R.string.questions_no, questionNo));
            if(permission > Group.PERM_READ_CAN_REQUEST_WRITE)
                privateTextView.setVisibility(View.VISIBLE);
            else
                privateTextView.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            callbacks.onLessonSelected(title, permission > Group.PERM_READ_CAN_REQUEST_WRITE);
        }

        @Override
        public boolean onLongClick(View v) {
            adapter.selectedTitle = title;
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            if(permission >= Group.PERM_MODIFY)
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
            LessonTable.LessonCursor cursor = (LessonTable.LessonCursor)data;
            holder.bindLesson(cursor.getLessonTitle(),
                              cursor.getNoteCount(),
                              cursor.getQuestionCount(),
                              cursor.getRequiredPermission(),
                              data.getInt(data.getColumnIndex("_id")));
        }
    }
}
