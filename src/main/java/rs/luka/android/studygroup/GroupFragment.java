package rs.luka.android.studygroup;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.networkcontroller.CoursesManager;

/**
 * Created by Luka on 7/1/2015.
 */
public class GroupFragment extends Fragment {

    private RecyclerView courseRecyclerView;
    private Callbacks callbacks;
    private CourseAdapter adapter;
    private FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        courseRecyclerView = (RecyclerView) view.findViewById(R.id.course_recycler_view);
        fab = (FloatingActionButton) view.findViewById(R.id.fab_add_course);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("WIP", "FAB (add course) pressed!");
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

    public void updateUI() {
        List<Course> courses = CoursesManager.getCourses();

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
            teacherTextView.setText(course.getTeacher());
            if(course.getYear()!=null)
                yearTextView.setText(getResources().getString(R.string.year, course.getYear().toString()));
            else
                yearTextView.setText("");
            imageView.setImageResource(course.getImage());
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
