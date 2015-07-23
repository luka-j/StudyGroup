package rs.luka.android.studygroup.activities.recyclers;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.Retriever;
import rs.luka.android.studygroup.model.Question;

/**
 * Created by luka on 11.7.15..
 */
public class QuestionListFragment extends Fragment {

    private Set<Question> selected = new HashSet<>();
    private ActionMode actionMode;
    private RecyclerView questionsRecycler;
    private QuestionCallbacks callbacks;
    private QuestionsAdapter adapter;
    private ActionMode.Callback selectItems = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_question, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.context_edit:
                    actionMode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selected.clear();
            adapter.notifyDataSetChanged();
        }
    };
    private UUID courseId;
    private String lessonName;
    private SwipeRefreshLayout swipe;

    public static QuestionListFragment newInstance(UUID courseId, String lessonName) {
        QuestionListFragment f = new QuestionListFragment();
        Bundle args = new Bundle();
        args.putSerializable(CourseActivity.EXTRA_COURSE_ID, courseId);
        args.putSerializable(CourseActivity.EXTRA_LESSON_NAME, lessonName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        courseId = (UUID) getArguments().getSerializable(CourseActivity.EXTRA_COURSE_ID);
        lessonName = getArguments().getString(CourseActivity.EXTRA_LESSON_NAME);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (QuestionCallbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_question_list, container, false);

        questionsRecycler = (RecyclerView) view.findViewById(R.id.questions_recycler);
        questionsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        swipe = (SwipeRefreshLayout) view.findViewById(R.id.questions_swipe);
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
                List<Question> newNotes = Retriever.getQuestions(courseId, lessonName);
                newNotes.add(new Question(UUID.randomUUID(),
                                          "Čemu sad ovo?",
                                          "Don't ask me",
                                          null));
                newNotes.add(new Question(UUID.randomUUID(), "Ali koga ću drugog?", "", null));
                adapter.setNotes(newNotes);
                adapter.notifyDataSetChanged();
                stopRefreshing();
            }
        }, 2500);
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
        List<Question> questions = Retriever.getQuestions(courseId, lessonName);

        if (adapter == null) {
            adapter = new QuestionsAdapter(questions);
            questionsRecycler.setAdapter(adapter);
        } else {
            adapter.setNotes(questions);
            adapter.notifyDataSetChanged();
        }
    }

    public interface QuestionCallbacks {
        void onQuestionSelected(Question question);
    }

    private class QuestionHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        private TextView questionTextView;
        private TextView answerTextView;

        private Question question;

        public QuestionHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            questionTextView = (TextView) itemView.findViewById(R.id.item_question_text);
            answerTextView = (TextView) itemView.findViewById(R.id.item_answer_text);
        }

        public void bindQuestion(Question question) {
            this.question = question;
            questionTextView.setText(question.getQuestion());
            answerTextView.setText(question.getAnswer());
        }

        @Override
        public void onClick(View v) {
            if (selected.isEmpty()) { callbacks.onQuestionSelected(question); } else {
                if (selected.contains(question)) {
                    selected.remove(question);
                    deselect(v);
                    if (selected.isEmpty()) { actionMode.finish(); }
                } else {
                    select(v);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (selected.isEmpty()) {
                actionMode = getActivity().startActionMode(selectItems);
            }
            select(v);
            return true;
            //return false;
        }

        private void select(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                v.animate().translationZ(4).setDuration(100).start();
            }
            v.setBackgroundResource(R.color.card_selected);
            v.setActivated(true);
            selected.add(question);
        }

        private void deselect(View v) {
            if (v.isActivated()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.animate().translationZ(-4).setDuration(100).start();
                }
                v.setBackgroundResource(R.color.background_material_light);
                v.setActivated(false);
            }
        }
    }

    private class QuestionsAdapter extends RecyclerView.Adapter<QuestionHolder> {

        private List<Question> questions;

        public QuestionsAdapter(List<Question> questions) {
            this.questions = questions;
        }

        @Override
        public QuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_question, parent, false);
            return new QuestionHolder(view);
        }

        @Override
        public void onBindViewHolder(QuestionHolder holder, int position) {
            Question question = questions.get(position);
            holder.bindQuestion(question);
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        public void setNotes(List<Question> questions) {
            this.questions = questions;
        }
    }
}
