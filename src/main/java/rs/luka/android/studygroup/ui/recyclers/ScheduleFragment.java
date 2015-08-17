package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
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
import rs.luka.android.studygroup.model.Exam;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.singleitemactivities.AddExamActivity;

/**
 * Created by luka on 29.7.15..
 */
public class ScheduleFragment extends Fragment {
    private Group                group;
    private RecyclerView         recycler;
    private Callbacks            callbacks;
    private ExamAdapter          adapter;
    private FloatingActionButton fab;
    private SwipeRefreshLayout   swipe;
    private CoordinatorLayout    coordinator;

    public static ScheduleFragment newInstance(Group g) {
        ScheduleFragment f    = new ScheduleFragment();
        Bundle           args = new Bundle(1);
        args.putParcelable(GroupActivity.EXTRA_GROUP, g);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        group = getArguments().getParcelable(GroupActivity.EXTRA_GROUP);
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
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recycler = (RecyclerView) view.findViewById(R.id.schedule_recycler_view);
        fab = (FloatingActionButton) view.findViewById(R.id.fab_add_exam);
        coordinator = (CoordinatorLayout) view.findViewById(R.id.coordinator);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AddExamActivity.class);
                i.putExtra(RootActivity.EXTRA_GROUP, group);
                startActivity(i);
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        registerForContextMenu(recycler);
        updateUI();


        new ItemTouchHelper(new TouchHelperCallbacks(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT))
                .attachToRecyclerView(recycler);

        swipe = (SwipeRefreshLayout) view.findViewById(R.id.schedule_swipe);
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
        }, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
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
            /*case R.id.raspored_kontrolnih:
                callbacks.onScheduleSelected();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateUI() {
        List<Exam> exams = group.getExamList();

        if (adapter == null) {
            adapter = new ExamAdapter(exams);
            recycler.setAdapter(adapter);
        } else {
            adapter.setExams(exams);
            adapter.notifyDataSetChanged();
        }
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
            final int  position = viewHolder.getAdapterPosition();
            adapter.removeExam(position);
            Snackbar.make(coordinator, R.string.exam_hidden, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adapter.addExam(exam, position);
                            exam.show();
                        }
                    })
                    .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                    .colorTheFuckingTextToWhite(getActivity())
                    .doStuffThatGoogleDidntFuckingDoProperly(getActivity(), fab)
                    .show();
            // ((TextView)(coordinator.findViewById(android.support.design.R.id.snackbar_text)))
            //       .setTextColor(getActivity().getResources().getColor(R.color.white)); //fuck you Google
            // doesn't actually work
            exam.hide();
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
            if (exam.hasImage()) { imageView.setImageBitmap(exam.getImage()); } else {
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
        }
    }

    private class ExamAdapter extends RecyclerView.Adapter<ExamHolder> {
        private Exam selectedExam;

        private List<Exam> exams;

        public ExamAdapter(List<Exam> exams) {
            this.exams = exams;
        }

        @Override
        public ExamHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.card_exam, parent, false);
            return new ExamHolder(view);
        }

        @Override
        public void onBindViewHolder(ExamHolder holder, int position) {
            Exam exam = exams.get(position);
            holder.bindExam(exam);
        }

        @Override
        public int getItemCount() {
            return exams.size();
        }

        public void setExams(List<Exam> exams) {
            this.exams = exams;
        }

        public void removeExam(int position) {
            exams.remove(position);
            this.notifyItemRemoved(position);
            this.notifyItemRangeChanged(position, exams.size());
        }

        public void addExam(Exam exam, int position) {
            exams.add(position, exam);
            this.notifyItemInserted(position);
            this.notifyItemRangeChanged(position, exams.size());
        }
    }
}
