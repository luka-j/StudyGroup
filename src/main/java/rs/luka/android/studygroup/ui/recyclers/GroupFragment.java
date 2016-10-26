package rs.luka.android.studygroup.ui.recyclers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.backgroundtasks.CourseTasks;
import rs.luka.android.studygroup.io.database.CourseTable;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.network.Courses;
import rs.luka.android.studygroup.network.Groups;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.ui.CursorAdapter;
import rs.luka.android.studygroup.ui.PoliteSwipeRefreshLayout;
import rs.luka.android.studygroup.ui.dialogs.AnnouncementDialog;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.AddCourseActivity;

//import rs.luka.android.studygroup.ui.Snackbar;

/**
 * Created by Luka on 7/1/2015.
 */
public class GroupFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
                                                       Network.NetworkCallbacks<String> {

    protected static final int    REQUEST_EDIT_COURSE   = 0;
    protected static final int    REQUEST_REMOVE_COURSE = 1; //network request
    private static final   String TAG                   = "studygroup.GroupFrag";
    private static final   int    REQUEST_ADD_COURSE    = 2; //intent request
    private static final int      REQUEST_SHOW_ALL      = 3; //network request
    private static final int      REQUEST_GET_ANNOUNCEMENTS = 4; //network request
    private Group                group;
    private RecyclerView         courseRecyclerView;
    private Callbacks            callbacks;
    private CourseAdapter        adapter;
    private FloatingActionButton fab;
    private PoliteSwipeRefreshLayout swipe;
    private CoordinatorLayout    coordinator;
    private CircularProgressView     progress;

    private NetworkExceptionHandler exceptionHandler;

    public static GroupFragment newInstance(Group group) {
        GroupFragment f    = new GroupFragment();
        Bundle        args = new Bundle();
        args.putParcelable(GroupActivity.EXTRA_GROUP, group);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        group = getArguments().getParcelable(GroupActivity.EXTRA_GROUP);

        Groups.getAnnouncements(REQUEST_GET_ANNOUNCEMENTS, group.getIdValue(), this);
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
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        ac.getSupportActionBar().setTitle(group.getName());

        progress = (CircularProgressView) view.findViewById(R.id.progress_view);

        courseRecyclerView = (RecyclerView) view.findViewById(R.id.course_recycler_view);
        fab = (FloatingActionButton) view.findViewById(R.id.fab_add_course);
        if(group.getPermission() < Group.PERM_WRITE) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pencil));
        }
        coordinator = (CoordinatorLayout) view.findViewById(R.id.coordinator);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(group.getPermission() >= Group.PERM_WRITE) {
                    Intent i = new Intent(getActivity(), AddCourseActivity.class);
                    i.putExtra(AddCourseActivity.EXTRA_MY_PERMISSION, group.getPermission());
                    i.putExtra(AddCourseActivity.EXTRA_GROUP, group);
                    startActivityForResult(i, REQUEST_ADD_COURSE);
                }/* else if(group.getPermission() >= Group.PERM_INVITED) {
                    //todo --accept invitation-- show info describing why can't add stuff yet
                    refresh(); // move to callback
                }*/ else {
                    callbacks.onRequestJoin(group);
                }
            }
        });
        final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        courseRecyclerView.setLayoutManager(lm);
        if(group.getPermission() >= Group.PERM_MODIFY) //context menu postoji samo za one koji mogu da ga koriste
            registerForContextMenu(courseRecyclerView);
        setData();

        new ItemTouchHelper(new TouchHelperCallbacks(0,
                                                     ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT))
                .attachToRecyclerView(courseRecyclerView);

        swipe = (PoliteSwipeRefreshLayout) view.findViewById(R.id.group_swipe);
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
            case REQUEST_ADD_COURSE:
            case REQUEST_EDIT_COURSE:
                refresh();
                break;
        }
    }

    protected void showProgressView() {
        progress.setVisibility(View.VISIBLE);
    }

    public void stopRefreshing() {
        swipe.setRefreshing(false);
    }

    protected void refresh() {
        swipe.setRefreshing(true);
        Groups.getGroupsInBackground(getContext(), exceptionHandler);
        CourseTasks.refreshCourses(getContext(), group, this, getActivity().getSupportLoaderManager(),
                                   exceptionHandler);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_edit:
                callbacks.onEditSelected(adapter.selectedCourse, REQUEST_EDIT_COURSE);
                return true;
            case R.id.context_remove_course:
                callbacks.onRemoveCourse(adapter.selectedCourse);
                return true;
        }
        return onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_all:
                Log.i(TAG, "Show all selected");
                Courses.showAllCourses(REQUEST_SHOW_ALL, group.getIdValue(), this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setData() {
        if (adapter == null) {
            adapter = new CourseAdapter(getActivity(), null);
            courseRecyclerView.setAdapter(adapter);
        }
        Groups.getGroupsInBackground(getContext(), exceptionHandler);
        CourseTasks.getCourses(getContext(), group, this, getActivity().getSupportLoaderManager(),
                               exceptionHandler);
    }

    protected void changeGroup(Group group) {
        this.group = group;
        setData();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.VISIBLE);
            }
        });
        return group.getCourseLoader(getActivity());
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
    public void onRequestCompleted(int id, Network.Response<String> response) {
        switch (id) {
            case REQUEST_SHOW_ALL:
            case REQUEST_REMOVE_COURSE:
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
            case REQUEST_GET_ANNOUNCEMENTS:
                if(response.responseCode == Network.Response.RESPONSE_OK) {
                    try {
                        JSONArray anns = new JSONArray(response.responseData);
                        for(int i=0; i<anns.length(); i++) {
                            final JSONObject announcement = anns.getJSONObject(i);
                            final String     text         = announcement.getString("text");
                            final String     years        = announcement.getString("years");
                            final long       date         = announcement.getLong("date");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AnnouncementDialog.newInstance(text, years, date)
                                                      .show(getActivity().getFragmentManager(), "");
                                }
                            });
                        }
                    } catch (JSONException e) {
                        exceptionHandler.handleJsonException();
                    }
                } else if(response.responseCode != Network.Response.NOT_MODIFIED) {
                    response.handleErrorCode(exceptionHandler);
                }
                break;
            default: Log.w(TAG, "Unknown requestId: " + id);
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
        void onCourseSelected(Course course);

        void onEditSelected(Course course, int requestCode);
        void onRequestJoin(Group group);
        void onRemoveCourse(Course course);
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
            final Course course   = ((CourseHolder) viewHolder).course;
            course.shallowHide(getActivity());
            getActivity().getSupportLoaderManager().restartLoader(CourseTasks.LOADER_ID, null, GroupFragment.this);
            Snackbar snackbar = Snackbar.make(coordinator, R.string.course_hidden, Snackbar.LENGTH_LONG)
                                        .setCallback(new Snackbar.Callback() {
                                            @Override
                                            public void onDismissed(Snackbar snackbar, int event) {
                                                course.hide(getContext(), exceptionHandler);
                                            }
                                        })
                                        .setAction(R.string.undo, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                course.show(getActivity());
                                                refresh();
                                            }
                                        })
                                        .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                    //.colorTheFuckingTextToWhite(getActivity())
                    //.doStuffThatGoogleDidntFuckingDoProperly(getActivity(), fab)
                    ;
            ((TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text))
                    .setTextColor(getActivity().getResources().getColor(R.color.white));
            snackbar.show();
            // ((TextView)(coordinator.findViewById(android.support.design.R.id.snackbar_text)))
            //       .setTextColor(getActivity().getResources().getColor(R.color.white)); //fuck you Google
            // doesn't actually work
        }
    }

    private class CourseHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener,
                       View.OnCreateContextMenuListener {

        private TextView  subjectTextView;
        private TextView  teacherTextView;
        private TextView  yearTextView;
        private ImageView imageView;

        private Course course;

        public CourseHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnCreateContextMenuListener(this);

            subjectTextView = (TextView) itemView.findViewById(R.id.card_course_subject_text);
            teacherTextView = (TextView) itemView.findViewById(R.id.card_course_teacher_text);
            yearTextView = (TextView) itemView.findViewById(R.id.card_course_year_text);
            imageView = (ImageView) itemView.findViewById(R.id.card_course_image);
        }

        public void bindCourse(Course course) {
            this.course = course;
            subjectTextView.setText(course.getSubject());
            if (course.getTeacher() != null) {
                teacherTextView.setText(course.getTeacher());
            } else { teacherTextView.setText(""); }
            if (course.getYear() != null) {
                yearTextView.setText(getResources().getString(R.string.year_no,
                                                              course.getYear().toString()));
            } else { yearTextView.setText(""); }
            if (course.hasImage()) {
                course.getImage(getContext(), getResources().getDimensionPixelSize(R.dimen.card_image_size), exceptionHandler, imageView);
            } else {
                //do nothing
            }
        }

        @Override
        public void onClick(View v) {
            callbacks.onCourseSelected(course);
        }

        @Override
        public boolean onLongClick(View v) {
            adapter.selectedCourse = course;
            return false;
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            if(group.getPermission() >= Group.PERM_MODIFY)
                getActivity().getMenuInflater().inflate(R.menu.context_group, menu);
        }
    }

    private class CourseAdapter extends CursorAdapter<CourseHolder> {

        private Course selectedCourse;

        public CourseAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public CourseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.card_course,
                                               parent,
                                               false);
            return new CourseHolder(view);
        }

        @Override
        public void onBindViewHolder(CourseHolder viewHolder, Cursor cursor) {
            viewHolder.bindCourse(((CourseTable.CourseCursor) cursor).getCourse());
        }
    }
}
