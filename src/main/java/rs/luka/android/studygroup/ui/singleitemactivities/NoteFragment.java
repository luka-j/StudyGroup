package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Intent;
import android.net.Uri;
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

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Note;

/**
 * Created by luka on 12.7.15..
 */
public class NoteFragment extends Fragment {
    public static final  String ARG_NOTE          = "anote";
    public static final  String ARG_COURSE_NAME   = "acourse";
    private static final int    IMAGE_IDEAL_DIMEN = 700;
    private Note     note;
    private String   courseName;
    private TextView text;
    private ImageView image;
    private TextView audio;
    private TextView history;

    public static NoteFragment newInstance(String courseName, Note note) {
        NoteFragment f    = new NoteFragment();
        Bundle       args = new Bundle();
        args.putParcelable(ARG_NOTE, note);
        args.putString(ARG_COURSE_NAME, courseName);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        note = getArguments().getParcelable(ARG_NOTE);
        courseName = getArguments().getString(ARG_COURSE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View              view = inflater.inflate(R.layout.fragment_note, container, false);
        AppCompatActivity ac   = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        text = (TextView) view.findViewById(R.id.note_text);
        image = (ImageView) view.findViewById(R.id.note_image);
        audio = (TextView) view.findViewById(R.id.play_audio);
        history = (TextView) view.findViewById(R.id.note_history);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), FullscreenImageActivity.class);
                i.putExtra(FullscreenImageActivity.EXTRA_IMAGE_PATH, note.getImagePath(courseName));
                startActivity(i);
            }
        });
        if (note.hasAudio(courseName)) {
            audio.setVisibility(View.VISIBLE);
            audio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri    uri    = note.getAudioPath(courseName);
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(uri,
                                          "audio/mp3"); // TODO: 19.9.15. change to audio/* when supporting multiple formats
                    // onda ce prikazivati chooser/default player, ovako je 'inline'
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }

        updateUI();
        return view;
    }

    private void updateUI() {
        text.setText(note.getText());
        if (note.hasImage(courseName)) {
            image.setImageBitmap(note.getImage(courseName, IMAGE_IDEAL_DIMEN));
        }
        history.setText(note.getHistory(getActivity()));
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
