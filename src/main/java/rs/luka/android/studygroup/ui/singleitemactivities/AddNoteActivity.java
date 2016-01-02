package rs.luka.android.studygroup.ui.singleitemactivities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.ui.recyclers.LessonActivity;

/**
 * Created by luka on 14.7.15..
 */
public class AddNoteActivity extends AppCompatActivity {

    public static final String STATE_IMAGE_FILE_PATH = "stateImg";
    public static final String STATE_AUDIO_FILE_PATH = "stateAudio";
    private static final int  INTENT_IMAGE          = 0;
    private static final int  INTENT_AUDIO          = 1;
    private RelativeLayout  content;
    private EditText        text;
    private EditText        lesson;
    private TextInputLayout textTil;
    private TextInputLayout lessonTil;
    private LinearLayout    buttonsLayout;
    private CardView        submit;
    private CardView        next;
    private TextView        nextText;
    private CardView        done;
    private ImageView       image;
    private ImageView       audio;
    private File            imageFile;
    private File            audioFile;
    private Course          course;
    private List<Note>      notes;
    private int currentNote = 0;
    private boolean editing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        course = getIntent().getParcelableExtra(LessonActivity.EXTRA_CURRENT_COURSE);
        notes = getIntent().getParcelableArrayListExtra(LessonActivity.EXTRA_SELECTED_NOTES);
        editing = notes != null;

        setContentView(R.layout.activity_add_note);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        text = (EditText) findViewById(R.id.add_note_text_input);
        textTil = (TextInputLayout) findViewById(R.id.add_note_text_til);
        lesson = (EditText) findViewById(R.id.add_note_lesson_input);
        lessonTil = (TextInputLayout) findViewById(R.id.add_note_lesson_til);
        submit = (CardView) findViewById(R.id.button_add);
        if (editing && notes.size() > 1) { //kreiranje dva dugmeta
            LayoutInflater inflater = LayoutInflater.from(this);
            buttonsLayout = (LinearLayout) inflater.inflate(R.layout.buttons_next_done, null, false);
            RelativeLayout.LayoutParams params
                    = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                      RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.addRule(RelativeLayout.BELOW, R.id.add_note_image);
            params.setMargins(0, 14, 0, 10);
            content = (RelativeLayout) findViewById(R.id.add_note_content);
            content.removeView(submit);
            content.addView(buttonsLayout, params);
            next = (CardView) buttonsLayout.findViewById(R.id.button_next);
            nextText = (TextView) next.getChildAt(0);
            done = (CardView) buttonsLayout.findViewById(R.id.button_done);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doSubmit();
                }
            });
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    done();
                }
            });
        }
        image = (ImageView) findViewById(R.id.add_note_image);
        audio = (ImageView) findViewById(R.id.add_note_audio);
        if (editing) {
            setFieldsForEditing();
        }

        final String lessonText = getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_LESSON);
        lesson.setText(lessonText);
        if (!lessonText.isEmpty()) { textTil.requestFocus(); }

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSubmit();
            }
        });
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    doSubmit();
                    return true;
                }
                return false;
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File courseImageDir = new File(LocalImages.APP_IMAGE_DIR, course.getSubject());
                if (!LocalImages.APP_IMAGE_DIR.isDirectory()) { LocalImages.APP_IMAGE_DIR.mkdir(); }
                if (!courseImageDir.isDirectory()) courseImageDir.mkdir();
                imageFile = new File(courseImageDir, lesson.getText().toString() + ".temp");
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                Intent chooserIntent = Intent.createChooser(camera,
                                                            getString(R.string.select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{gallery});
                startActivityForResult(chooserIntent, INTENT_IMAGE);
            }
        });
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent audio
                        = new Intent(Intent.ACTION_GET_CONTENT); //todo this shit returns some awkward URI I can't parse
                audio.setType("audio/*");
                //Intent chooserIntent = Intent.createChooser(audio,
                //                                          getString(R.string.select_audio));
                startActivityForResult(audio, INTENT_AUDIO);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageFile != null) { outState.putString(STATE_IMAGE_FILE_PATH, imageFile.getAbsolutePath()); }
        if (audioFile != null) { outState.putString(STATE_AUDIO_FILE_PATH, audioFile.getAbsolutePath()); }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getString(STATE_IMAGE_FILE_PATH) != null) {
            imageFile = new File(savedInstanceState.getString(STATE_IMAGE_FILE_PATH));
            image.setImageBitmap(LocalImages.loadImage(imageFile,
                                                       getResources().getDimensionPixelOffset(R.dimen.addview_image_size)));
        }
        if (savedInstanceState.getString(STATE_AUDIO_FILE_PATH) != null) {
            audioFile = new File(savedInstanceState.getString(STATE_AUDIO_FILE_PATH));
            audio.setVisibility(View.INVISIBLE); // TODO: 19.9.15. proper
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == INTENT_IMAGE) {
                if (data != null) { //ako je data==null, fotografija je napravljena kamerom, nije iz galerije
                    imageFile = new File(Utils.getRealPathFromUri(this, data.getData()));
                }
                image.setImageBitmap(LocalImages.loadImage(imageFile,
                                                           getResources().getDimensionPixelOffset(R.dimen.addview_image_size)));
            } else if (requestCode == INTENT_AUDIO && data != null) {
                audioFile = new File(Utils.getRealPathFromUri(this, data.getData()));
                audio.setVisibility(View.INVISIBLE); // TODO: 19.9.15. proper
            }
        }
    }

    private void setFieldsForEditing() {
        text.setText(notes.get(currentNote).getText());
        text.setSelection(text.getText().length());
        if (notes.get(currentNote).hasImage()) {
            image.setImageBitmap(notes.get(currentNote)
                                      .getImage(course.getSubject(),
                                                getResources().getDimensionPixelOffset(R.dimen.addview_image_size)));
        }
    }

    private void doSubmit() {
        boolean error = false;
        String lessonStr = lesson.getText().toString(),
                noteStr = text.getText().toString();
        if (lessonStr.isEmpty()) {
            lessonTil.setError(getString(R.string.error_empty));
            error = true;
        } else if (lessonStr.length() > Limits.LESSON_MAX_LENGTH) {
            lessonTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { lessonTil.setError(null); }
        if (noteStr.isEmpty()) {
            textTil.setError(getString(R.string.error_empty));
            error = true;
        } else { textTil.setError(null); }
        if (!error) {
            if (editing) {
                currentNote++;
                if (currentNote < notes.size()) { setFieldsForEditing(); }
                if (currentNote <= notes.size()) {
                    notes.get(currentNote - 1).edit(this, lessonStr, noteStr, imageFile, audioFile);
                }
                if (currentNote == notes.size() - 1) { mergeButtons(); }
                if (currentNote == notes.size()) {
                    setResultData(lessonStr);
                    onBackPressed();
                }
            } else {
                course.addNote(this, lessonStr, noteStr, imageFile, audioFile);
                setResultData(lessonStr);
                onBackPressed();
            }
        }
    }

    private void setResultData(String lessonStr) {
        Intent lessonData = new Intent();
        lessonData.putExtra(LessonActivity.EXTRA_LESSON, lessonStr);
        setResult(RESULT_OK, lessonData);
    }

    private void mergeButtons() {
        nextText.setText(R.string.done);
        buttonsLayout.removeView(done);
        buttonsLayout.invalidate();
    }

    private void done() {
        doSubmit();
        if (currentNote < notes.size()) { onBackPressed(); }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
