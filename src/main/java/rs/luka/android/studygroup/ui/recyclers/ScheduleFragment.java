package rs.luka.android.studygroup.ui.recyclers;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Exam;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.network.Exams;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.network.NetworkRequests;
import rs.luka.android.studygroup.ui.CursorAdapter;
import rs.luka.android.studygroup.ui.PoliteSwipeRefreshLayout;
import rs.luka.android.studygroup.ui.Snackbar;
import rs.luka.android.studygroup.ui.singleitemactivities.AddExamActivity;

/**
 * Created by luka on 29.7.15..
 */
public class ScheduleFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
                                                          NetworkRequests.NetworkCallbacks {
    private static final int REQUEST_ADD_EXAM = 0;
    private static final String ARG_GROUP     = "agroup";
    private static final String TAG           = "ScheduleFragment";
    private static final int REQUEST_SHOW_ALL = 1; //network request
    private static final int REQUEST_REMOVE = 2; //network request
    private NetworkExceptionHandler exceptionHandler;
    private Group                    group;
    private RecyclerView             recycler;
    private Callbacks                callbacks;
    private ExamAdapter              adapter;
    private FloatingActionButton     fab;
    private PoliteSwipeRefreshLayout swipe;
    private CoordinatorLayout        coordinator;
    private CircularProgressView     progress;

    public static ScheduleFragment newInstance(Group g) {
        ScheduleFragment f    = new ScheduleFragment();
        Bundle           args = new Bundle(1);
        args.putParcelable(ARG_GROUP, g);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        group = getArguments().getParcelable(ARG_GROUP);
        //setRetainInstance(true);
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
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progress = (CircularProgressView) view.findViewById(R.id.progress_view);
        recycler = (RecyclerView) view.findViewById(R.id.schedule_recycler_view);
        fab = (FloatingActionButton) view.findViewById(R.id.fab_add_exam);
        coordinator = (CoordinatorLayout) view.findViewById(R.id.coordinator);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddExamActivity.class);
                i.putExtra(AddExamActivity.EXTRA_GROUP, group);
                startActivityForResult(i, REQUEST_ADD_EXAM);
            }
        });
        final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(lm);
        registerForContextMenu(recycler);
        setData();

        new ItemTouchHelper(new TouchHelperCallbacks(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT))
                .attachToRecyclerView(recycler);

        swipe = (PoliteSwipeRefreshLayout) view.findViewById(R.id.schedule_swipe);
        swipe.setOnChildScrollUpListener(new PoliteSwipeRefreshLayout.OnChildScrollUpListener() {
            @Override
            public boolean canChildScrollUp() {
                return lm.findFirstCompletelyVisibleItemPosition() != 0;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_EXAM:
                refresh();
                break;
        }
    }

    public void stopRefreshing() {
        swipe.setRefreshing(false);
    }

    private void refresh() {
        DataManager.refreshExams(getContext(), group.getIdValue(), this, getActivity().getLoaderManager(), exceptionHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(group.getName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_schedule, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_edit:
                callbacks.onEditSelected(adapter.selectedExam);
                return true;
            case R.id.context_remove_exam:
                Exams.removeExam(REQUEST_REMOVE, adapter.selectedExam.getIdValue(), this);
        }
        return onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.filter_exams:
                callbacks.onFilterSelected();
                return true;
            case R.id.show_all:
                Exams.showAllExams(REQUEST_SHOW_ALL, group.getIdValue(), this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setData() {
        if (adapter == null) {
            adapter = new ExamAdapter(getActivity(), null);
            recycler.setAdapter(adapter);
        }
        DataManager.getExams(getContext(), group.getIdValue(), this, getActivity().getLoaderManager(), exceptionHandler);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.VISIBLE);
            }
        });
        return group.getExamLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
        stopRefreshing();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onRequestCompleted(int id, Network.Response response) {
        switch (id) {
            case REQUEST_SHOW_ALL:
            case REQUEST_REMOVE:
                if (response.responseCode == Network.Response.RESPONSE_OK) {
                    getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
                } else {
                    response.handleException(exceptionHandler);
                }
                break;
            default:
                Log.w(TAG, "Invalid requestId: " + id);
        }
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        ex.printStackTrace();
        //todo
    }

    public interface Callbacks {
        void onExamSelected(Exam exam);
        void onEditSelected(Exam exam);
        void onFilterSelected();
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
            final Exam exam   = ((ExamHolder) viewHolder).exam;
            exam.shallowHide(getActivity());
            getActivity().getLoaderManager().restartLoader(DataManager.LOADER_ID_EXAMS, null, ScheduleFragment.this);
            Snackbar.make(coordinator, R.string.exam_hidden, Snackbar.LENGTH_LONG)
                    .setOnHideListener(new Snackbar.OnHideListener() {
                        @Override
                        public void onHide() {
                            exam.hide(getContext(), exceptionHandler);
                        }
                    })
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            exam.show(getActivity());
                            refresh();
                        }
                    })
                    .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                    .colorTheFuckingTextToWhite(getActivity())
                    .doStuffThatGoogleDidntFuckingDoProperly(getActivity(), fab)
                    .show();
        }
    }

    private class ExamHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener,
                       View.OnCreateContextMenuListener {

        private TextView  subjectTextView;
        private TextView  dateTextView;
        private TextView  yearTextView;
        private ImageView imageView;

        private Exam exam;

        public ExamHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnCreateContextMenuListener(this);

            subjectTextView = (TextView) itemView.findViewById(R.id.card_exam_subject_text);
            dateTextView = (TextView) itemView.findViewById(R.id.card_exam_date_text);
            yearTextView = (TextView) itemView.findViewById(R.id.card_exam_year_text);
            imageView = (ImageView) itemView.findViewById(R.id.card_exam_image);
        }

        public void bindExam(Exam exam) {
            this.exam = exam;
            subjectTextView.setText(exam.getTitle());
            dateTextView.setText(exam.getDate(getActivity()));
            yearTextView.setText(exam.getKlass());
            Course examCourse = exam.getCourse();
            if (examCourse.hasImage()) {
                imageView.setImageBitmap(examCourse.getImage(getResources().getDimensionPixelSize(R.dimen.card_image_size)));
            } else {
                imageView.setImageResource(R.drawable.placeholder);
            }
        }

        @Override
        public void onClick(View v) {
            callbacks.onExamSelected(exam);
        }

        @Override
        public boolean onLongClick(View v) {
            adapter.selectedExam = exam;
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            getActivity().getMenuInflater().inflate(R.menu.context_schedule, menu);
            if(group.getPermission() < Group.PERM_MODIFY) {
                menu.removeItem(R.id.context_remove_exam);
            }
        }
    }

    private class ExamAdapter extends CursorAdapter<ExamHolder> {
        private Exam selectedExam;

        public ExamAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public ExamHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.card_exam, parent, false);
            return new ExamHolder(view);
        }

        @Override
        public void onBindViewHolder(ExamHolder viewHolder, Cursor cursor) {
            viewHolder.bindExam(((Database.ExamCursor) cursor).getExam());
        }
    }
}
