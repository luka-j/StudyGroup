package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.backgroundtasks.NoteTasks;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.ui.recyclers.HistoryActivity;

/**
 * Created by luka on 12.7.15..
 */
public class NoteFragment extends Fragment implements NoteTasks.AudioCallbacks {
    private static final int REQUEST_PRELOAD_AUDIO      = 0;
    private static final int REQUEST_GET_AUDIO_FOR_PLAY = 1;

    public static final String ARG_NOTE          = "anote";
    public static final String ARG_COURSE_NAME   = "acourse";
    public static final String ARG_MY_PERMISSION = "aperm";
    private static int  IMAGE_IDEAL_DIMEN        = 720;
    private Note     note;
    private String   courseName;
    private TextView text;
    private ImageView image;
    private TextView audio;
    private CircularProgressView audioCpv;
    private NetworkExceptionHandler exceptionHandler;
    private int permission;

    public static NoteFragment newInstance(String courseName, Note note, int permission) {
        NoteFragment f    = new NoteFragment();
        Bundle       args = new Bundle();
        args.putParcelable(ARG_NOTE, note);
        args.putString(ARG_COURSE_NAME, courseName);
        args.putInt(ARG_MY_PERMISSION, permission);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        note = getArguments().getParcelable(ARG_NOTE);
        courseName = getArguments().getString(ARG_COURSE_NAME);
        permission = getArguments().getInt(ARG_MY_PERMISSION);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        IMAGE_IDEAL_DIMEN = metrics.widthPixels;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler((AppCompatActivity)context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View              view = inflater.inflate(R.layout.fragment_note, container, false);
        AppCompatActivity ac   = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if(note.hasAudio())
            note.getAudio(REQUEST_PRELOAD_AUDIO, courseName, exceptionHandler, this);

        text = (TextView) view.findViewById(R.id.note_text);
        image = (ImageView) view.findViewById(R.id.note_image);
        audio = (TextView) view.findViewById(R.id.play_audio);
        audioCpv = (CircularProgressView) view.findViewById(R.id.play_audio_cpv);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), FullscreenImageActivity.class);
                try {
                    i.putExtra(FullscreenImageActivity.EXTRA_IMAGE_PATH, note.getImagePath(courseName));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        });
        if (note.hasAudio()) {
            audio.setVisibility(View.VISIBLE);
            audio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    note.getAudio(REQUEST_GET_AUDIO_FOR_PLAY, courseName, exceptionHandler, NoteFragment.this);
                    audioCpv.setVisibility(View.VISIBLE);
                }
            });
        }

        updateUI();
        return view;
    }

    private void updateUI() {
        text.setText(note.getText());
        if (note.hasImage()) {
            note.getImage(getContext(), courseName, IMAGE_IDEAL_DIMEN, exceptionHandler, image);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_note, menu);
        if(permission < Group.PERM_WRITE) {
            menu.removeItem(R.id.note_history);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.note_history:
                startActivity(new Intent(getContext(), HistoryActivity.class).putExtra(HistoryActivity.EXTRA_ITEM, note));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAudioReady(int requestId, File audioFile) {
        if(getActivity() != null && requestId == REQUEST_GET_AUDIO_FOR_PLAY) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    audioCpv.setVisibility(View.GONE);
                }
            });
            Uri    uri    = Uri.fromFile(audioFile);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "audio/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }
}
