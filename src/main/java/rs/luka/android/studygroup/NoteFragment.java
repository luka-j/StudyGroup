package rs.luka.android.studygroup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.networkcontroller.CoursesManager;

/**
 * Created by luka on 12.7.15..
 */
public class NoteFragment extends Fragment {
    public static final String EXTRA_NOTE = "note";

    private Note note;
    private TextView text;
    private ImageView image;
    private TextView history;

    public static NoteFragment newInstance(Note note) {
        NoteFragment f = new NoteFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_NOTE, note);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        note = (Note) getArguments().getSerializable(EXTRA_NOTE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note, container, false);

        text = (TextView) view.findViewById(R.id.note_text);
        image = (ImageView) view.findViewById(R.id.note_image);
        history = (TextView) view.findViewById(R.id.note_history);

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        updateUI();
        return view;
    }

    private void updateUI() {
        text.setText(note.getText());
        if (note.getImageUrl() != null)
            image.setImageBitmap(CoursesManager.getNoteImage(note.getId()));
        history.setText(CoursesManager.getNoteHistory(note.getId(), getActivity()));
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
}