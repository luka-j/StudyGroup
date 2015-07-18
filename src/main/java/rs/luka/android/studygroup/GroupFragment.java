package rs.luka.android.studygroup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.networkcontroller.Retriever;

/**
 * Created by Luka on 7/1/2015.
 */
public class GroupFragment extends Fragment {

    private String groupName;
    private UUID groupId;
    private RecyclerView courseRecyclerView;
    private Callbacks callbacks;
    private CourseAdapter adapter;
    private FloatingActionButton fab;

    public static GroupFragment newInstance(UUID groupId, String groupName) {
        GroupFragment f = new GroupFragment();
        Bundle args = new Bundle();
        args.putSerializable(RootActivity.EXTRA_GROUP_ID, groupId);
        args.putString(RootActivity.EXTRA_GROUP_NAME, groupName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        groupName = getArguments().getString(RootActivity.EXTRA_GROUP_NAME);
        groupId = (UUID) getArguments().getSerializable(RootActivity.EXTRA_GROUP_ID);
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddCourseActivity.class));
            }
        });
        courseRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(groupName);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //TODO
        //inflater.inflate(R.menu.fragment_group, menu);
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
        List<Course> courses = Retriever.getCourses(groupId);

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
    }

    private class CourseHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView subjectTextView;
        private TextView teacherTextView;
        private TextView yearTextView;
        private ImageView imageView;

        private Course course;

        public CourseHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            subjectTextView = (TextView) itemView.findViewById(R.id.card_course_subject_text);
            teacherTextView = (TextView) itemView.findViewById(R.id.card_course_teacher_text);
            yearTextView = (TextView) itemView.findViewById(R.id.card_course_year_text);
            imageView = (ImageView) itemView.findViewById(R.id.card_course_image);
        }

        public void bindCourse(Course course) {
            this.course = course;
            subjectTextView.setText(course.getSubject());
            if (course.getTeacher() != null)
                teacherTextView.setText(course.getTeacher());
            else
                teacherTextView.setText("");
            if(course.getYear()!=null)
                yearTextView.setText(getResources().getString(R.string.year_no, course.getYear().toString()));
            else
                yearTextView.setText("");
            Bitmap img = Retriever.getCourseImage(course.getId());
            if (img != null)
                imageView.setImageBitmap(img);
            else
                imageView.setImageResource(R.drawable.placeholder);
        }

        @Override
        public void onClick(View v) {
            callbacks.onCourseSelected(course);
        }
    }

    private class CourseAdapter extends RecyclerView.Adapter<CourseHolder> {

        private List<Course> courses;

        public CourseAdapter(List<Course> courses) {
            this.courses = courses;
        }

        @Override
        public CourseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.card_course, parent, false);
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
    }
}
