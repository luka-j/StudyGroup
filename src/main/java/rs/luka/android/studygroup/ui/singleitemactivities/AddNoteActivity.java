package rs.luka.android.studygroup.ui.singleitemactivities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.net.SocketException;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.dialogs.InputHelpDialog;
import rs.luka.android.studygroup.ui.recyclers.LessonActivity;

/**
 * Created by luka on 14.7.15..
 */
public class AddNoteActivity extends AppCompatActivity {

    private static final String TAG = "AddNoteActivity";
    private NetworkExceptionHandler exceptionHandler;

    public static final String EXTRA_IS_EXAM = "isExam";
    public static final String STATE_IMAGE_FILE_PATH = "stateImg";
    public static final String STATE_AUDIO_FILE_PATH = "stateAudio";
    private static final int  INTENT_IMAGE          = 0;
    private static final int  INTENT_AUDIO          = 1;
    private RelativeLayout       content;
    private EditText             text;
    private EditText             lesson;
    private TextInputLayout      textTil;
    private TextInputLayout      lessonTil;
    private LinearLayout         buttonsLayout;
    private CardView             submit;
    private CardView             next;
    private TextView             nextText;
    private CardView             done;
    private CircularProgressView progressView;
    private ImageView            image;
    private ImageView            audio;
    private CheckBox             privBox;


    private File       imageFile;
    private File       audioFile;
    private Course     course;
    private List<Note> notes;
    private String     lessonStr;
    private String     currentLessonText;
    private int currentNote = 0;
    private boolean editing;
    private boolean isPrivate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initExceptionHandler();
        initData();
        setContentView(R.layout.activity_add_note);
        initToolbar();
        initViews();
        if (editing && notes.size() > 1) { //kreiranje dva dugmeta
            createEditButtons();
        }
        if (editing)              {
            getSupportActionBar().setTitle(lessonStr);
            setFieldsForEditing();
        }
        if (isPrivate)            privBox.setChecked(true);
        if (editing || isPrivate) privBox.setEnabled(false); //todo make editing privacy possible (server-side, history)

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSubmit();
            }
        });
        initTextListeners();
        initMediaListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
        return true;
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
            audio.setColorFilter(getResources().getColor(R.color.color_primary));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == INTENT_IMAGE) {
                if (data != null && data.getData() != null) { //ako je data==null, fotografija je napravljena kamerom, nije iz galerije
                                    //u Marshmallow-u i kasnijim je data != null, ali je data.getData() == null
                    imageFile = new File(Utils.getRealPathFromUri(this, data.getData()));
                }
                image.setImageBitmap(LocalImages.loadImage(imageFile,
                                                           getResources().getDimensionPixelOffset(R.dimen.addview_image_size)));
            } else if (requestCode == INTENT_AUDIO && data != null) {
                audioFile = new File(Utils.getRealPathFromUri(this, data.getData()));
                audio.setColorFilter(getResources().getColor(R.color.color_primary));
            }
        }
    }

    private void setFieldsForEditing() {
        text.setText(notes.get(currentNote).getText());
        text.setSelection(text.getText().length());
        if (notes.get(currentNote).hasImage()) {
            notes.get(currentNote).getImage(this,
                                            course.getSubject(),
                                            getResources().getDimensionPixelOffset(R.dimen.addview_image_size),
                                            exceptionHandler, image);
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
            this.lessonStr = lessonStr;
            if (editing) {
                notes.get(currentNote).edit(this, lessonStr, noteStr, imageFile, audioFile, exceptionHandler);
                currentNote++;
                if(buttonsLayout!=null)buttonsLayout.setVisibility(View.GONE);
                else submit.setVisibility(View.GONE);
            } else {
                course.addNote(this, lessonStr, noteStr, imageFile, audioFile, privBox.isChecked(), exceptionHandler);
                submit.setVisibility(View.GONE);
            }
            progressView.setVisibility(View.VISIBLE);
        }
    }

    private void setUpNext() {
        if(editing) {
            progressView.setVisibility(View.GONE);
            if(buttonsLayout!=null) buttonsLayout.setVisibility(View.VISIBLE);
            if (currentNote < notes.size()) { setFieldsForEditing(); }
            if (currentNote == notes.size() - 1) { mergeButtons(); }
            if (currentNote == notes.size()) {
                setResultData(lessonStr);
                onBackPressed();
            }
        } else {
            setResultData(lessonStr);
            onBackPressed();
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
            case R.id.add_item_show_help:
                new InputHelpDialog().show(getFragmentManager(), "");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void initExceptionHandler() {
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler(this) {
            @Override
            public void finishedSuccessfully() {
                super.finishedSuccessfully();
                setUpNext();
            }
            @Override
            public void handleOffline() {
                InfoDialog.newInstance(getString(R.string.error_offline_edit_title),
                                       getString(R.string.error_offline_edit_text))
                          .show(getSupportFragmentManager(), "");
                Network.Status.setOffline();
            }
            @Override
            public void finishedUnsuccessfully() {
                progressView.setVisibility(View.GONE);
                if(buttonsLayout!=null) buttonsLayout.setVisibility(View.VISIBLE);
                else submit.setVisibility(View.VISIBLE);
            }
            public void handleSocketException(SocketException ex) {
                InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_socketex_title),
                                                           hostActivity.getString(R.string.error_socketex_text));
                if(hostActivity instanceof InfoDialog.Callbacks)
                    dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                Log.e(TAG, "Unexpected SocketException", ex);
                Network.Status.setOffline();
            }
        };
    }

    private void initData() {
        course = getIntent().getParcelableExtra(LessonActivity.EXTRA_CURRENT_COURSE);
        notes = getIntent().getParcelableArrayListExtra(LessonActivity.EXTRA_SELECTED_NOTES);
        editing = notes != null;
        isPrivate = getIntent().getBooleanExtra(EXTRA_IS_EXAM, false);
        currentLessonText = getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_LESSON);
    }

    private void initViews() {
        text = (EditText) findViewById(R.id.add_note_text_input);
        textTil = (TextInputLayout) findViewById(R.id.add_note_text_til);
        lesson = (EditText) findViewById(R.id.add_note_lesson_input);
        lessonTil = (TextInputLayout) findViewById(R.id.edit_user_username_til);
        submit = (CardView) findViewById(R.id.button_add);
        progressView = (CircularProgressView) findViewById(R.id.add_note_cpv);
        privBox = (CheckBox) findViewById(R.id.private_cb);
        image = (ImageView) findViewById(R.id.add_note_image);
        audio = (ImageView) findViewById(R.id.add_note_audio);
        lesson.setText(currentLessonText);
        if (!currentLessonText.isEmpty()) { textTil.requestFocus(); }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }
    }

    private void createEditButtons() {
        LayoutInflater inflater = LayoutInflater.from(this);
        buttonsLayout = (LinearLayout) inflater.inflate(R.layout.buttons_next_done, null, false);
        RelativeLayout.LayoutParams params
                = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                  RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        if(Build.VERSION.SDK_INT >= 17) params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.addRule(RelativeLayout.BELOW, R.id.add_note_image);
        params.setMargins(0, 14, 0, 10);
        content = (RelativeLayout) findViewById(R.id.edit_user_content);
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

    private void initTextListeners() {
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
        lesson.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if((editing || isPrivate) && s.equals(currentLessonText))
                    privBox.setEnabled(false);
                else
                    privBox.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initMediaListeners() {
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
}
