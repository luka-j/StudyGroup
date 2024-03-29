package rs.luka.android.studygroup.ui.recyclers;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.HashSet;
import java.util.Set;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.backgroundtasks.NoteTasks;
import rs.luka.android.studygroup.io.backgroundtasks.QuestionTasks;
import rs.luka.android.studygroup.io.database.QuestionTable;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.ui.CursorAdapter;
import rs.luka.android.studygroup.ui.PoliteSwipeRefreshLayout;
//import rs.luka.android.studygroup.ui.Snackbar;

/**
 * Created by luka on 11.7.15..
 */
public class QuestionListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private NetworkExceptionHandler exceptionHandler;

    private int toolbarHeight;
    private Set<Question> selected = new HashSet<>();
    private ActionMode               actionMode;
    private RecyclerView             questionsRecycler;
    private QuestionCallbacks        callbacks;
    private QuestionsAdapter         adapter;
    private int                      permission;
    private Course                   course;
    private String                   lessonName;
    private PoliteSwipeRefreshLayout swipe;
    private CircularProgressView     progress;
    private Snackbar                 snackbar;
    private ActionMode.Callback selectItems = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_question, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if(permission < Group.PERM_MODIFY) {
                menu.removeItem(R.id.context_remove_question);
                return true;
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.context_edit:
                    callbacks.onQuestionsEdit(selected);
                    actionMode.finish();
                    return true;
                case R.id.context_hide:
                    hide();
                    actionMode.finish();
                    return true;
                case R.id.context_remove_question:
                    Set<Long> selectedIds = new HashSet<>(selected.size());
                    for(Question n : selected) selectedIds.add(n.getIdValue());
                    callbacks.onQuestionsRemove(selectedIds);
                    actionMode.finish();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selected.clear();
            adapter.notifyDataSetChanged();
        }
    };

    public static QuestionListFragment newInstance(Course course, String lessonName, int permission) {
        QuestionListFragment f    = new QuestionListFragment();
        Bundle               args = new Bundle();
        args.putParcelable(CourseActivity.EXTRA_COURSE, course);
        args.putString(CourseActivity.EXTRA_LESSON_NAME, lessonName);
        args.putInt(LessonActivity.EXTRA_MY_PERMISSION, permission);
        f.setArguments(args);
        return f;
    }

    protected void refreshForLesson(String lessonName) {
        if (this.lessonName == null || !this.lessonName.equals(lessonName)) {
            this.lessonName = lessonName;
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(lessonName);
        }
        refresh();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        course = getArguments().getParcelable(CourseActivity.EXTRA_COURSE);
        lessonName = getArguments().getString(CourseActivity.EXTRA_LESSON_NAME);
        permission = getArguments().getInt(LessonActivity.EXTRA_MY_PERMISSION);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        callbacks = (QuestionCallbacks) activity;
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler((AppCompatActivity)activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        View view = inflater.inflate(R.layout.fragment_question_list, container, false);
        final ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        progress = (CircularProgressView) view.findViewById(R.id.progress_view);
        questionsRecycler = (RecyclerView) view.findViewById(R.id.questions_recycler);
        final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        questionsRecycler.setLayoutManager(lm);
        new ItemTouchHelper(new TouchHelperCallbacks(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                                                     ItemTouchHelper.LEFT))
                .attachToRecyclerView(questionsRecycler);
        setData();

        swipe = (PoliteSwipeRefreshLayout) view.findViewById(R.id.questions_swipe);
        swipe.setOnChildScrollUpListener(new PoliteSwipeRefreshLayout.OnChildScrollUpListener() {
            @Override
            public boolean canChildScrollUp() {
                return lm.findFirstCompletelyVisibleItemPosition() > 0 || toolbar.getHeight() < toolbarHeight;
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

    public void stopRefreshing() {
        swipe.setRefreshing(false);
    }

    public void refresh() {
        swipe.setRefreshing(true);
        QuestionTasks.refreshQuestions(getContext(), course.getIdValue(), lessonName, this,
                                       getActivity().getSupportLoaderManager(), exceptionHandler);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    private void hide() {
        for (Question q : selected) { q.hide(getActivity(), exceptionHandler); }
        getActivity().getSupportLoaderManager().restartLoader(QuestionTasks.LOADER_ID, null, QuestionListFragment.this);
        /*final Set<Question> selected = new HashSet<>(this.selected); //selected se briše kad se actionmode zatvori
        snackbar = Snackbar.make(questionsRecycler, R.string.questions_hidden, Snackbar.LENGTH_LONG)
                           .setAction(R.string.undo, new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   for (Question q : selected) { q.show(getActivity()); }
                                   refresh();
                               }
                           })
                           .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                           .colorTheFuckingTextToWhite(getActivity())
                           .doStuffThatGoogleDidntFuckingDoProperly(getActivity(),
                                                                    (callbacks.getFab()));
        snackbar.show();*/

        snackbar = Snackbar.make(questionsRecycler, R.string.questions_hidden, Snackbar.LENGTH_SHORT)
                //.colorTheFuckingTextToWhite(getContext())
                //.doStuffThatGoogleDidntFuckingDoProperly(getContext(), callbacks.getFab())
                ;
        ((TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text))
                .setTextColor(getActivity().getResources().getColor(R.color.white));
        snackbar.show();
    }

    protected void dismissSnackbar() {
        if (snackbar != null) { snackbar.dismiss(); }
    }

    public void setData() {
        if (adapter == null) {
            adapter = new QuestionsAdapter(getActivity(), null);
            questionsRecycler.setAdapter(adapter);
        }
        QuestionTasks.getQuestions(getContext(), course.getIdValue(), lessonName, this,
                                   getActivity().getSupportLoaderManager(), exceptionHandler);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.VISIBLE);
            }
        });
        return course.getQuestionsLoader(getActivity(), lessonName);
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

    public interface QuestionCallbacks {
        void onQuestionSelected(int questionPosition);

        void onQuestionsEdit(Set<Question> questions);
        void onQuestionsRemove(Set<Long> questionIds);
        FloatingActionButton getFab();
    }

    private class TouchHelperCallbacks extends ItemTouchHelper.SimpleCallback {

        public TouchHelperCallbacks(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        private Question movedQuestion;
        private int movedToPosition = -1;

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            QuestionHolder srcHolder = (QuestionHolder) viewHolder;
            //NoteHolder destHolder = (NoteHolder)target;
            if(movedToPosition == -1) movedQuestion = srcHolder.question;
            movedToPosition = target.getAdapterPosition();
            /*if(!destHolder.itemView.isSelected()) srcHolder.itemView.setSelected(false);
            srcHolder.bindNote(destHolder.note);
            destHolder.bindNote(movedNote);*/ //doesn't work as expected (swaps everything to movedQuestion)
            return true;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            QuestionHolder holder = (QuestionHolder)viewHolder;
            holder.question.hide(getActivity(), exceptionHandler);
            getActivity().getSupportLoaderManager().restartLoader(NoteTasks.LOADER_ID, null, QuestionListFragment.this);
            snackbar = Snackbar.make(questionsRecycler, R.string.notes_hidden, Snackbar.LENGTH_SHORT);
            ((TextView)snackbar.getView().findViewById(android.support.design.R.id.snackbar_text))
                    .setTextColor(getActivity().getResources().getColor(R.color.white));
            snackbar.show();
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if(movedToPosition > -1) {
                movedQuestion.reorder(getActivity(), movedToPosition + 1, exceptionHandler); //1-based ordering
                getActivity().getSupportLoaderManager()
                             .restartLoader(NoteTasks.LOADER_ID, null, QuestionListFragment.this);
                movedToPosition = -1;
                movedQuestion = null;
            }
        }
    }

    private class QuestionHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        private TextView questionTextView;
        private TextView answerTextView;
        private ImageView thumbnail;
        private View container;

        private Question question;

        public QuestionHolder(View itemView) {
            super(itemView);
            container = itemView;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            questionTextView = (TextView) itemView.findViewById(R.id.item_question_text);
            answerTextView = (TextView) itemView.findViewById(R.id.item_answer_text);
            thumbnail = (ImageView) itemView.findViewById(R.id.item_question_thumbnail);
        }

        public void bindQuestion(Question question) {
            this.question = question;
            questionTextView.setText(question.getQuestion());
            answerTextView.setText(question.getAnswer());
            if (this.question.hasImage()) {
                question.getImage(getContext(),
                                  course.getSubject(),
                                  getResources().getDimensionPixelSize(R.dimen.list_item_thumbnail_size),
                                  exceptionHandler, thumbnail);
            } else {
                thumbnail.setImageBitmap(null);
            }
            if (selected.contains(question)) {
                select(container);
            } else {
                deselect(container);
            }
        }

        @Override
        public void onClick(View v) {
            if (selected.isEmpty()) { callbacks.onQuestionSelected(getAdapterPosition()); } else {
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

    private class QuestionsAdapter extends CursorAdapter<QuestionHolder> {

        public QuestionsAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public QuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_question,
                                               parent,
                                               false);
            return new QuestionHolder(view);
        }

        @Override
        public void onBindViewHolder(QuestionHolder holder, Cursor data) {
            holder.bindQuestion(((QuestionTable.QuestionCursor) data).getQuestion());
        }
    }
}
