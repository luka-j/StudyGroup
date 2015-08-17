package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.Snackbar;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.singleitemactivities.AddCourseActivity;

/**
 * Created by Luka on 7/1/2015.
 */
public class GroupFragment extends Fragment {

    private Group                group;
    private RecyclerView         courseRecyclerView;
    private Callbacks            callbacks;
    private CourseAdapter        adapter;
    private FloatingActionButton fab;
    private SwipeRefreshLayout   swipe;
    private CoordinatorLayout    coordinator;

    public static GroupFragment newInstance(Group group) {
        GroupFragment f    = new GroupFragment();
        Bundle        args = new Bundle();
        args.putParcelable(RootActivity.EXTRA_GROUP, group);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        group = getArguments().getParcelable(RootActivity.EXTRA_GROUP);
        //setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        courseRecyclerView = (RecyclerView) view.findViewById(R.id.course_recycler_view);
        fab = (FloatingActionButton) view.findViewById(R.id.fab_add_course);
        coordinator = (CoordinatorLayout) view.findViewById(R.id.coordinator);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddCourseActivity.class);
                i.putExtra(RootActivity.EXTRA_GROUP, group);
                startActivity(i);
            }
        });
        courseRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        registerForContextMenu(courseRecyclerView);
        updateUI();


        new ItemTouchHelper(new TouchHelperCallbacks(0,
                                                     ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT))
                .attachToRecyclerView(courseRecyclerView);

        swipe = (SwipeRefreshLayout) view.findViewById(R.id.group_swipe);
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

    private void refresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ;
                stopRefreshing();
            }
        }, 3000);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_edit:
                callbacks.onEditSelected(adapter.selectedCourse);
                return true;
        }
        return onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(getActivity(), RootActivity.class);
                i.putExtra(RootActivity.EXTRA_SHOW_LIST, true);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateUI() {
        List<Course> courses = group.getCourseList();

        if (adapter == null) {
            adapter = new CourseAdapter(courses);
            courseRecyclerView.setAdapter(adapter);
        } else {
            adapter.setCourses(courses);
            adapter.notifyDataSetChanged();
        }
    }

    public interface Callbacks {
        void onCourseSelected(Course course);

        void onEditSelected(Course course);
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
            final int    position = viewHolder.getAdapterPosition();
            adapter.removeCourse(position);
            Snackbar.make(coordinator, R.string.course_hidden, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adapter.addCourse(course, position);
                            course.showCourse();
                        }
                    })
                    .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                    .colorTheFuckingTextToWhite(getActivity())
                    .doStuffThatGoogleDidntFuckingDoProperly(getActivity(), fab)
                    .show();
            // ((TextView)(coordinator.findViewById(android.support.design.R.id.snackbar_text)))
            //       .setTextColor(getActivity().getResources().getColor(R.color.white)); //fuck you Google
            // doesn't actually work
            course.hideCourse();
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
            if (course.hasImage()) { imageView.setImageBitmap(course.getImage()); } else {
                imageView.setImageResource(R.drawable.placeholder);
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
            getActivity().getMenuInflater().inflate(R.menu.context_group, menu);
        }
    }

    private class CourseAdapter extends RecyclerView.Adapter<CourseHolder> {
        private Course selectedCourse;

        private List<Course> courses;

        public CourseAdapter(List<Course> courses) {
            this.courses = courses;
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
        public void onBindViewHolder(CourseHolder holder, int position) {
            Course course = courses.get(position);
            holder.bindCourse(course);
        }

        @Override
        public int getItemCount() {
            return courses.size();
        }

        public void setCourses(List<Course> courses) {
            this.courses = courses;
        }

        public void removeCourse(int position) {
            courses.remove(position);
            this.notifyItemRemoved(position);
            this.notifyItemRangeChanged(position, courses.size());
        }

        public void addCourse(Course course, int position) {
            courses.add(position, course);
            this.notifyItemInserted(position);
            this.notifyItemRangeChanged(position, courses.size());
        }
    }
}
