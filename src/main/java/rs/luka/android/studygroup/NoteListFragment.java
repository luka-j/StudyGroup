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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.networkcontroller.CoursesManager;

/**
 * Created by luka on 11.7.15..
 */
public class NoteListFragment extends Fragment {

    private RecyclerView notesRecycler;
    private NoteCallbacks callbacks;
    private NotesAdapter adapter;
    private UUID courseId;
    private String lessonName;

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

        notesRecycler = (RecyclerView) view.findViewById(R.id.notes_recycler);
        notesRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
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
        List<Note> notes = CoursesManager.getNotes(courseId, lessonName);

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

    private class NoteHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView titleTextView;
        private LinearLayout layout;

        private Note note;

        public NoteHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            titleTextView = (TextView) itemView.findViewById(R.id.item_note_text);
            layout = (LinearLayout) itemView.findViewById(R.id.item_note_layout);
        }

        public void bindNote(Note note) {
            this.note = note;
            titleTextView.setText(note.getText());
            if (note.getImageUrl() != null) {
                ImageView imgView = new ImageView(getActivity());
                imgView.setImageBitmap(CoursesManager.getNoteImage(note.getId()));
                layout.addView(imgView, 0);
            }
        }

        @Override
        public void onClick(View v) {
            callbacks.onNoteSelected(note);
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
    }
}
