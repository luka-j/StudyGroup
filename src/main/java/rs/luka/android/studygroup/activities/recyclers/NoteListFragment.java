package rs.luka.android.studygroup.activities.recyclers;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.Snackbar;
import rs.luka.android.studygroup.io.Hider;
import rs.luka.android.studygroup.io.Retriever;
import rs.luka.android.studygroup.model.Note;

/**
 * Created by luka on 11.7.15..
 */
public class NoteListFragment extends Fragment {

    private Set<Note> selected = new HashSet<>();
    private ActionMode actionMode;
    private RecyclerView notesRecycler;
    private NoteCallbacks callbacks;
    private NotesAdapter adapter;
    private ActionMode.Callback selectItems = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_note, menu);
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

    public static NoteListFragment newInstance(UUID courseId, String lessonName) {
        NoteListFragment f = new NoteListFragment();
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
        callbacks = (NoteCallbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_list, container, false);

        Activity ac = getActivity();
        notesRecycler = (RecyclerView) view.findViewById(R.id.notes_recycler);
        notesRecycler.setLayoutManager(new LinearLayoutManager(ac));
        //notesRecycler.addOnScrollListener(new HideShowListener(ac.findViewById(R.id.lesson_container)));
        updateUI();

        new ItemTouchHelper(new TouchHelperCallbacks(0, ItemTouchHelper.RIGHT))
                .attachToRecyclerView(notesRecycler);

        swipe = (SwipeRefreshLayout) view.findViewById(R.id.notes_swipe);
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
                List<Note> newNotes = Retriever.getNotes(courseId, lessonName);
                newNotes.add(new Note(UUID.randomUUID(), "Nova beleška 1", null));
                newNotes.add(new Note(UUID.randomUUID(), "Nova beleška 2", null));
                adapter.setNotes(newNotes);
                adapter.notifyDataSetChanged();
                stopRefreshing();
            }
        }, 1800);
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
        List<Note> notes = Retriever.getNotes(courseId, lessonName);

        if (adapter == null) {
            adapter = new NotesAdapter(notes);
            notesRecycler.setAdapter(adapter);
        } else {
            adapter.setNotes(notes);
            adapter.notifyDataSetChanged();
        }
    }

    public interface NoteCallbacks {
        void onNoteSelected(Note note);
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
            final Note note     = ((NoteHolder) viewHolder).note;
            final int  position = viewHolder.getAdapterPosition();
            adapter.removeNote(position);
            Snackbar.make(notesRecycler, R.string.course_hidden, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            adapter.addNote(note, position);
                            Hider.showNote(courseId, note.getId());
                        }
                    })
                    .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                    .colorTheFuckingTextToWhite(getActivity())
                    .doStuffThatGoogleDidntFuckingDoProperly(getActivity(),
                                                             ((LessonActivity) getActivity()).getFab())
                    .show();
            // ((TextView)(coordinator.findViewById(android.support.design.R.id.snackbar_text)))
            //       .setTextColor(getActivity().getResources().getColor(R.color.white)); //fuck you Google
            // doesn't actually work
            Hider.hideNote(courseId, note.getId());
        }
    }

    private class NoteHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        private TextView titleTextView;
        private LinearLayout layout;
        private View container;

        private Note note;

        public NoteHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            container = itemView;
            titleTextView = (TextView) itemView.findViewById(R.id.item_note_text);
            layout = (LinearLayout) itemView.findViewById(R.id.item_note_layout);
        }

        public void bindNote(Note note) {
            this.note = note;
            titleTextView.setText(note.getText());
            if (note.getImageUrl() != null) {
                ImageView imgView = new ImageView(getActivity());
                imgView.setImageBitmap(Retriever.getNoteImage(note.getId()));
                layout.addView(imgView, 0);
            }
            if (selected.contains(note)) {
                select(container);
            } else {
                deselect(container);
            }
        }

        @Override
        public void onClick(View v) {
            if (selected.isEmpty()) { callbacks.onNoteSelected(note); } else {
                if (selected.contains(note)) {
                    selected.remove(note);
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
            selected.add(note);
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

    private class NotesAdapter extends RecyclerView.Adapter<NoteHolder> {

        private List<Note> notes;

        public NotesAdapter(List<Note> notes) {
            this.notes = notes;
        }

        @Override
        public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_note, parent, false);
            return new NoteHolder(view);
        }

        @Override
        public void onBindViewHolder(NoteHolder holder, int position) {
            Note note = notes.get(position);
            holder.bindNote(note);
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }

        public void setNotes(List<Note> notes) {
            this.notes = notes;
        }

        public void removeNote(int position) {
            notes.remove(position);
            this.notifyItemRemoved(position);
            this.notifyItemRangeChanged(position, notes.size());
        }

        public void addNote(Note note, int position) {
            notes.add(position, note);
            this.notifyItemInserted(position);
            this.notifyItemRangeChanged(position, notes.size());
        }
    }
}
