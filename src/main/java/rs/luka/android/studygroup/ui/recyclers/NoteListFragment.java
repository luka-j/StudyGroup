package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.util.HashSet;
import java.util.Set;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.ui.CursorAdapter;
import rs.luka.android.studygroup.ui.PoliteSwipeRefreshLayout;
import rs.luka.android.studygroup.ui.Snackbar;

/**
 * Created by luka on 11.7.15..
 */
public class NoteListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private int toolbarHeight;
    private Set<Note> selected = new HashSet<>();
    private ActionMode               actionMode;
    private RecyclerView             notesRecycler;
    private NoteCallbacks            callbacks;
    private NotesAdapter             adapter;
    private Course                   course;
    private String                   lessonName;
    private PoliteSwipeRefreshLayout swipe;
    private CircularProgressView     progress;
    private Snackbar                 snackbar;
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
                    callbacks.onNotesEdit(selected);
                    actionMode.finish();
                    return true;
                case R.id.context_hide:
                    hide();
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

    public static NoteListFragment newInstance(Course course, String lessonName) {
        NoteListFragment f    = new NoteListFragment();
        Bundle           args = new Bundle();
        args.putParcelable(CourseActivity.EXTRA_COURSE, course);
        args.putString(CourseActivity.EXTRA_LESSON_NAME, lessonName);
        f.setArguments(args);
        return f;
    }

    protected void setLessonNameIfEmpty(String lessonName) {
        if (this.lessonName == null || this.lessonName.isEmpty()) {
            this.lessonName = lessonName;
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(lessonName);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        course = getArguments().getParcelable(CourseActivity.EXTRA_COURSE);
        lessonName = getArguments().getString(CourseActivity.EXTRA_LESSON_NAME);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        callbacks = (NoteCallbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        View            view    = inflater.inflate(R.layout.fragment_note_list, container, false);
        final ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        progress = (CircularProgressView) view.findViewById(R.id.progress_view);
        Activity ac = getActivity();
        notesRecycler = (RecyclerView) view.findViewById(R.id.notes_recycler);
        final LinearLayoutManager lm = new LinearLayoutManager(ac);
        notesRecycler.setLayoutManager(lm);
        //notesRecycler.addOnScrollListener(new HideShowListener(ac.findViewById(R.id.lesson_container)));
        setData();

        swipe = (PoliteSwipeRefreshLayout) view.findViewById(R.id.notes_swipe);
        swipe.setOnChildScrollUpListener(new PoliteSwipeRefreshLayout.OnChildScrollUpListener() {
            @Override
            public boolean canChildScrollUp() {
                return lm.findFirstCompletelyVisibleItemPosition() != 0 || toolbar.getHeight() >= toolbarHeight;
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
        DataManager.refreshNotes(this, getActivity().getLoaderManager());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    private void hide() {
        for (Note n : selected) { n.hide(getActivity()); }
        refresh();
        final Set<Note> selected = new HashSet<>(this.selected); //selected se briše kad se actionmode zatvori
        snackbar = Snackbar.make(notesRecycler, R.string.notes_hidden, Snackbar.LENGTH_LONG)
                           .setAction(R.string.undo, new View.OnClickListener() {
                               @Override
                               public void onClick(View v) {
                                   for (Note n : selected) { n.show(getActivity()); }
                                   refresh();
                               }
                           })
                           .setActionTextColor(getActivity().getResources().getColor(R.color.color_accent))
                           .colorTheFuckingTextToWhite(getActivity())
                           .doStuffThatGoogleDidntFuckingDoProperly(getActivity(),
                                                                    ((LessonActivity) getActivity()).getFab());
        snackbar.show();
    }

    protected void dismissSnackbar() {
        if (snackbar != null) { snackbar.dismiss(); }
        snackbar = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //TODO
        //inflater.inflate(R.menu.fragment_group, menu);
    }

    public void setData() {
        if (adapter == null) {
            adapter = new NotesAdapter(getActivity(), null);
            notesRecycler.setAdapter(adapter);
        }
        DataManager.getNotes(this, getActivity().getLoaderManager());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        progress.setVisibility(View.VISIBLE);
        if (course == null) { throw new AssertionError("wtf"); }
        return course.getNotesLoader(getActivity(), lessonName);
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

    public interface NoteCallbacks {
        void onNoteSelected(Note note);

        void onNotesEdit(Set<Note> notes);
    }

    private class NoteHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        private TextView titleTextView;
        private LinearLayout layout;
        private View     container;

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
            if (note.hasImage()) {
                ImageView imgView = new ImageView(getActivity());
                imgView.setImageBitmap(note.getImage(getContext()));
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

    private class NotesAdapter extends CursorAdapter<NoteHolder> {


        public NotesAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_note,
                                               parent,
                                               false);
            return new NoteHolder(view);
        }

        @Override
        public void onBindViewHolder(NoteHolder holder, Cursor data) {
            holder.bindNote(((Database.NoteCursor) data).getNote());
        }

        public void removeNote(int position) {
            //todo
        }

        public void addNote(Note note, int position) {
            //todo
        }
    }
}
