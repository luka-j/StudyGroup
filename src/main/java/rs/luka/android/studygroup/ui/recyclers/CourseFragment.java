package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.Snackbar;
import rs.luka.android.studygroup.model.Course;

/**
 * Created by luka on 3.7.15..
 */
public class CourseFragment extends Fragment {

    private static String TAG = "studygroup.CourseFragment";

    private Course course;

    private RecyclerView   lessonsRecyclerView;
    private Callbacks      callbacks;
    private LessonsAdapter adapter;
    private SwipeRefreshLayout swipe;

    protected static Fragment newInstance(Course course) {
        Bundle args = new Bundle();
        args.putParcelable(GroupActivity.EXTRA_COURSE, course);

        Fragment f = new CourseFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        course = getArguments().getParcelable(GroupActivity.EXTRA_COURSE);
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

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }
        lessonsRecyclerView = (RecyclerView) view.findViewById(R.id.lessons_recycler_view);
        lessonsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        registerForContextMenu(lessonsRecyclerView);

        updateUI();


        new ItemTouchHelper(new TouchHelperCallbacks(0,
                                                     ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT))
                .attachToRecyclerView(lessonsRecyclerView);

        swipe = (SwipeRefreshLayout) view.findViewById(R.id.course_swipe);
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
                List<String> newLessons = new LinkedList<>();
                newLessons.add("Logika");
                newLessons.add("Brojevi");
                newLessons.add("Racionalni algebarski izrazi");
                newLessons.add("Jednačine i nejednačine");
                newLessons.add("Stepenovanje i korenovanje");
                adapter.setTitles(newLessons);
                adapter.notifyDataSetChanged();
                stopRefreshing();
            }
        }, 1500);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume; setting actionbar title");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(course.getSubject());
        updateUI();
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
        List<String> lessons = course.getLessonList();

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

        void onEdit(String title);
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
            final String title    = ((LessonHolder) viewHolder).title;
            final int    position = viewHolder.getAdapterPosition();
            adapter.removeTitle(position);
            Snackbar.make(lessonsRecyclerView,
                          R.string.course_hidden,
                          rs.luka.android.studygroup.Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo,
                               new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       adapter.addTitle(title, position);
                                       course.showLesson(title);
                                   }
                               })
                    .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                    .colorTheFuckingTextToWhite(getActivity())
                    .doStuffThatGoogleDidntFuckingDoProperly(getActivity(), null)
                    .show();
            course.hideLesson(title);
        }
    }

    private class LessonHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                                                          View.OnLongClickListener,
                                                                          View.OnCreateContextMenuListener {
        private final TextView titleTextView;
        private final TextView noteCount;
        private final TextView questionCount;

        private String title;

        public LessonHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnCreateContextMenuListener(this);

            titleTextView = (TextView) itemView.findViewById(R.id.card_lesson_title);
            noteCount = (TextView) itemView.findViewById(R.id.note_count_text);
            questionCount = (TextView) itemView.findViewById(R.id.question_count_text);
        }

        public void bindCourse(String title) {
            this.title = title;
            titleTextView.setText(title);
            noteCount.setText(getString(R.string.notes_no, course.getNoteNumber(title)));
            questionCount.setText(getString(R.string.questions_no, course.getQuestionNumber(title)));
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

    private class LessonsAdapter extends RecyclerView.Adapter<LessonHolder> {

        private List<String> titles;
        private String selectedTitle;

        public LessonsAdapter(List<String> titles) {
            this.titles = titles;
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

        public void removeTitle(int position) {
            titles.remove(position);
            this.notifyItemRemoved(position);
            this.notifyItemRangeChanged(position, titles.size());
        }

        public void addTitle(String title, int position) {
            titles.add(position, title);
            this.notifyItemInserted(position);
            this.notifyItemRangeChanged(position, titles.size());
        }
    }
}