package rs.luka.android.studygroup;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.networkcontroller.Retriever;

/**
 * Created by luka on 11.7.15..
 */
public class QuestionListFragment extends Fragment {

    private RecyclerView questionsRecycler;
    private QuestionCallbacks callbacks;
    private QuestionsAdapter adapter;
    private UUID courseId;
    private String lessonName;

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

    private class QuestionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView questionTextView;
        private TextView answerTextView;

        private Question question;

        public QuestionHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

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
            callbacks.onQuestionSelected(question);
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
