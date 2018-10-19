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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
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
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.ui.Showcase;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.dialogs.InputHelpDialog;
import rs.luka.android.studygroup.ui.recyclers.LessonActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Created by luka on 14.7.15..
 */
public class AddQuestionActivity extends AppCompatActivity {
    private static final String TAG = "AddQuestionActivity";
    private NetworkExceptionHandler exceptionHandler;

    public static final String EXTRA_IS_PRIVATE = "isFromExam";

    public static final String STATE_IMAGE_FILE_PATH = "stateimg";
    private static final int  INTENT_IMAGE          = 0;
    private LinearLayout         content;
    private Toolbar              toolbar;
    private EditText             lesson;
    private EditText             answer;
    private EditText             question;
    private TextInputLayout      lessonTil;
    private TextInputLayout      questionTil;
    private LinearLayout         buttonsLayout;
    private CardView             add;
    private TextView             nextText;
    private CardView             next;
    private CardView             done;
    private ImageView            image;
    private CheckBox             privBox;
    private CircularProgressView progressView;

    private File           imageFile;
    private Course         course;
    private boolean        editing;
    private boolean        isPrivate;
    private List<Question> questions;
    private String         currentLessonText;
    private String         lessonStr;
    private int currentQuestion = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initExceptionHandler();
        initData();

        setContentView(R.layout.activity_add_question);
        initToolbar();
        initViews();

        if (editing && questions.size() > 1) { //kreiranje dva dugmeta
            createEditButtons();
        }
        if (editing) {
            getSupportActionBar().setTitle(lessonStr);
            setFieldsForEditing();
        }
        if (isPrivate) privBox.setChecked(true);
        if (editing || isPrivate) privBox.setEnabled(false); //todo make editing privacy possible (server-side, history)

        initTextListeners();
        initMediaListeners();

        if(!MaterialShowcaseView.hasAlreadyFired(this, "add-item")) {
            Utils.simulateBackButton();
            new Showcase(this).showSequence("add-item", new View[]{lessonTil, toolbar},
                                            new int[]{R.string.tut_additem_lesson, R.string.tut_additem_specialchars});
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help_button, menu);
        return true;
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
            }
        }
    }

    private void setFieldsForEditing() {
        question.setText(questions.get(currentQuestion).getQuestion());
        question.setSelection(question.getText().length());
        answer.setText(questions.get(currentQuestion).getAnswer());
        if (questions.get(currentQuestion).hasImage()) {
            questions.get(currentQuestion).getImage(this,
                                                    course.getSubject(),
                                                    getResources().getDimensionPixelOffset(R.dimen.addview_image_size),
                                                    exceptionHandler, image);
        }
    }

    private void doSubmit() {
        boolean error = false;
        String lessonStr;
        lessonStr = lesson.getText().toString();
        String questionStr = question.getText().toString(),
                answerStr = answer.getText().toString();
        if (lessonStr.isEmpty()) {
            lessonTil.setError(getString(R.string.error_empty));
            error = true;
        } else if (lessonStr.length() > Limits.LESSON_MAX_LENGTH) {
            lessonTil.setError(getString(R.string.error_too_long));
            error = true;
        } else if(lessonStr.contains("/")) {
            lessonTil.setError(getString(R.string.error_slash_in_lesson_name));
            error=true;
        } else { lessonTil.setError(null); }
        if (questionStr.isEmpty()) {
            questionTil.setError(getString(R.string.error_empty));
            error = true;
        } else { questionTil.setError(null); }
        if (!error) {
            this.lessonStr = lessonStr;
            if (editing) {
                currentQuestion++;
                questions.get(currentQuestion - 1)
                         .edit(this, lessonStr, questionStr, answerStr, imageFile, exceptionHandler);
                if(buttonsLayout!=null)buttonsLayout.setVisibility(View.GONE);
                else add.setVisibility(View.GONE);
            } else {
                course.addQuestion(this, lessonStr, questionStr, answerStr, imageFile, privBox.isChecked(), exceptionHandler);
                add.setVisibility(View.GONE);
            }
            progressView.setVisibility(View.VISIBLE);
        }
    }

    private void setUpNext() {
        if(editing) {
            progressView.setVisibility(View.GONE);
            if(buttonsLayout!=null) buttonsLayout.setVisibility(View.VISIBLE);
            if (currentQuestion < questions.size()) { setFieldsForEditing(); }
            if (currentQuestion == questions.size() - 1) { mergeButtons(); }
            if (currentQuestion == questions.size()) {
                setResultData(lessonStr);
                onBackPressed();
            }
        } else {
            setResultData(lessonStr);
            onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(imageFile != null)
            outState.putString(STATE_IMAGE_FILE_PATH, imageFile.getAbsolutePath());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.getString(STATE_IMAGE_FILE_PATH) != null) {
            imageFile = new File(savedInstanceState.getString(STATE_IMAGE_FILE_PATH));
            image.setImageBitmap(LocalImages.loadImage(imageFile,
                                                       getResources().getDimensionPixelOffset(R.dimen.addview_image_size)));
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
        if (currentQuestion < questions.size()) {
            setResultData(lesson.getText().toString());
            onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.menu_item_show_help:
                new InputHelpDialog().show(getFragmentManager(), "");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void initExceptionHandler() {
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
                          .show(getFragmentManager(), "");
                Network.Status.setOffline();
            }
            @Override
            public void finishedUnsuccessfully() {
                progressView.setVisibility(View.GONE);
                if(buttonsLayout!=null) buttonsLayout.setVisibility(View.VISIBLE);
                else add.setVisibility(View.VISIBLE);
            }
            @Override
            public void handleSocketException(SocketException ex) {
                InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_socketex_title),
                                                           hostActivity.getString(R.string.error_socketex_text));
                if(hostActivity instanceof InfoDialog.Callbacks)
                    dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                dialog.show(hostActivity.getFragmentManager(), TAG_DIALOG);
                Log.e(TAG, "Unexpected SocketException", ex);
                Network.Status.setOffline();
            }
        };
    }

    private void initData() {
        course = getIntent().getParcelableExtra(LessonActivity.EXTRA_CURRENT_COURSE);
        questions = getIntent().getParcelableArrayListExtra(LessonActivity.EXTRA_SELECTED_QUESTIONS);
        editing = questions != null;
        isPrivate = getIntent().getBooleanExtra(EXTRA_IS_PRIVATE, false);
        currentLessonText = getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_LESSON);
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }
    }

    private void initViews() {
        lesson = (EditText) findViewById(R.id.add_question_lesson_input);
        answer = (EditText) findViewById(R.id.add_question_answer_input);
        question = (EditText) findViewById(R.id.add_question_text_input);
        lessonTil = (TextInputLayout) findViewById(R.id.add_question_lesson_til);
        questionTil = (TextInputLayout) findViewById(R.id.add_question_text_til);
        add = (CardView) findViewById(R.id.button_add);
        image = (ImageView) findViewById(R.id.add_question_image);
        privBox = (CheckBox) findViewById(R.id.private_cb);
        progressView = (CircularProgressView) findViewById(R.id.add_question_cpv);
        lesson.setText(currentLessonText);
        if (!currentLessonText.isEmpty()) { question.requestFocus(); }
    }

    private void createEditButtons() {
        LayoutInflater inflater = LayoutInflater.from(this);
        buttonsLayout = (LinearLayout) inflater.inflate(R.layout.buttons_next_done, null, false);
        LinearLayout.LayoutParams params
                = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        params.setMargins(0, 14, 0, 10);
        content = (LinearLayout) findViewById(R.id.add_question_content);
        content.removeView(add);
        content.addView(buttonsLayout, params);
        next = (CardView) buttonsLayout.findViewById(R.id.button_next);
        nextText = (TextView) next.getChildAt(0);
        done = (CardView) buttonsLayout.findViewById(R.id.button_done);
        next.setOnClickListener(v -> doSubmit());
        done.setOnClickListener(v -> done());
    }

    private void initTextListeners() {
        add.setOnClickListener(v -> doSubmit());
        answer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doSubmit();
                return true;
            }
            return false;
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
        image.setOnClickListener(v -> {
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
        });
    }
}
