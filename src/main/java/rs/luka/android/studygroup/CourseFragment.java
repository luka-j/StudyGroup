package rs.luka.android.studygroup;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

import rs.luka.android.studygroup.networkcontroller.Retriever;

/**
 * Created by luka on 3.7.15..
 */
public class CourseFragment extends Fragment {

    private static String TAG = "studygroup.CourseFragment";

    private RecyclerView lessonsRecyclerView;
    private Callbacks callbacks;
    private LessonsAdapter adapter;
    private UUID courseId;
    private String courseName;

    protected static Fragment newInstance(UUID courseId, String courseName) {
        Bundle args = new Bundle();
        args.putSerializable(GroupActivity.EXTRA_COURSE_ID, courseId);
        args.putString(GroupActivity.EXTRA_COURSE_NAME, courseName);

        Fragment f = new CourseFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        courseId = (UUID) getArguments().getSerializable(GroupActivity.EXTRA_COURSE_ID);
        courseName = getArguments().getString(GroupActivity.EXTRA_COURSE_NAME);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course, container, false);

        AppCompatActivity ac =  (AppCompatActivity)getActivity();
        if(NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }
        lessonsRecyclerView = (RecyclerView) view.findViewById(R.id.lessons_recycler_view);
        lessonsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume; setting actionbar title");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(courseName);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateUI() {
        List<String> lessons = Retriever.getLessons(courseId);

        if (adapter == null) {
            adapter = new LessonsAdapter(lessons);
            lessonsRecyclerView.setAdapter(adapter);
        } else {
            adapter.setTitles(lessons);
            adapter.notifyDataSetChanged();
        }
    }

    public interface Callbacks {
        void onLessonSelected(String title);
    }

    private class LessonHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView titleTextView;

        private String title;

        public LessonHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            titleTextView = (TextView) itemView.findViewById(R.id.card_lesson_title);
        }

        public void bindCourse(String title) {
            this.title = title;
            titleTextView.setText(title);
        }

        @Override
        public void onClick(View v) {
            callbacks.onLessonSelected(title);
        }
    }

    private class LessonsAdapter extends RecyclerView.Adapter<LessonHolder> {

        private List<String> titles;

        public LessonsAdapter(List<String> titles) {
            this.titles = titles;
        }

        @Override
        public LessonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.card_lesson, parent, false);
            return new LessonHolder(view);
        }

        @Override
        public void onBindViewHolder(LessonHolder holder, int position) {
            String title = titles.get(position);
            holder.bindCourse(title);
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }

        public void setTitles(List<String> titles) {
            this.titles = titles;
        }
    }
}
