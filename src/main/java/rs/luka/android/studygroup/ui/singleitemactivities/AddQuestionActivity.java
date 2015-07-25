package rs.luka.android.studygroup.ui.singleitemactivities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.Date;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.Utils;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.ui.recyclers.LessonActivity;

/**
 * Created by luka on 14.7.15..
 */
public class AddQuestionActivity extends AppCompatActivity {

    private static final int  IDEAL_IMAGE_DIMENSION = 300;
    private static final int  INTENT_IMAGE          = 0;
    private static final File imageDir              = new File(
            Environment.getExternalStorageDirectory().toString() + "/DCIM/StudyGroup/");
    private RelativeLayout  content;
    private EditText        lesson;
    private EditText        answer;
    private EditText        question;
    private TextInputLayout lessonTil;
    private TextInputLayout questionTil;
    private LinearLayout    buttonsLayout;
    private CardView        add;
    private TextView        nextText;
    private CardView        next;
    private CardView        done;
    private ImageView       image;
    private File            imageFile;
    private Course          course;
    private boolean         editing;
    private List<Question>  questions;
    private int currentQuestion = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        course = getIntent().getParcelableExtra(LessonActivity.EXTRA_CURRENT_COURSE);
        questions = getIntent().getParcelableArrayListExtra(LessonActivity.EXTRA_SELECTED_QUESTIONS);
        editing = questions != null;

        setContentView(R.layout.activity_add_question);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        lesson = (EditText) findViewById(R.id.add_question_lesson_input);
        answer = (EditText) findViewById(R.id.add_question_answer_input);
        question = (EditText) findViewById(R.id.add_question_text_input);
        lessonTil = (TextInputLayout) findViewById(R.id.add_question_lesson_til);
        questionTil = (TextInputLayout) findViewById(R.id.add_question_text_til);
        add = (CardView) findViewById(R.id.button_add);
        image = (ImageView) findViewById(R.id.add_question_image);
        if (editing) {
            LayoutInflater inflater = LayoutInflater.from(this);
            buttonsLayout = (LinearLayout) inflater.inflate(R.layout.buttons_next_done, null, false);
            RelativeLayout.LayoutParams params
                    = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                                                      RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.addRule(RelativeLayout.BELOW, R.id.add_question_image);
            params.setMargins(0, 14, 0, 0);
            content = (RelativeLayout) findViewById(R.id.add_question_content);
            content.removeView(add);
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
            setFieldsForEditing();
        }

        lesson.setText(getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_LESSON));
        question.requestFocus();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSubmit();
            }
        });
        answer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                if (!imageDir.isDirectory()) { imageDir.mkdir(); }
                imageFile = new File(imageDir,
                                     course.getSubject()
                                     + " - "
                                     + getIntent().getStringExtra(LessonActivity.EXTRA_CURRENT_LESSON)
                                     + " "
                                     + new Date().getTime() + ".jpg");
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                Intent chooserIntent = Intent.createChooser(camera,
                                                            getString(R.string.select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{gallery});
                startActivityForResult(chooserIntent, INTENT_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == INTENT_IMAGE) {
                if (data
                    != null) { //ako je data==null, fotografija je napravljena kamerom, nije iz galerije
                    imageFile = new File(Utils.getRealPathFromURI(this, data.getData()));
                }
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opts);
                opts.inJustDecodeBounds = false;
                int larger = opts.outHeight > opts.outWidth ? opts.outHeight : opts.outWidth;
                opts.inSampleSize = larger / IDEAL_IMAGE_DIMENSION;
                opts.inPreferQualityOverSpeed = false;
                image.setImageBitmap(BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opts));
            }
        }
    }

    private void setFieldsForEditing() {
        question.setText(questions.get(currentQuestion).getQuestion());
        question.setSelection(question.getText().length());
        answer.setText(questions.get(currentQuestion).getAnswer());
        if (questions.get(currentQuestion).hasImage()) {
            image.setImageBitmap(questions.get(currentQuestion).getImage());
        }
    }

    private void doSubmit() {
        boolean error = false;
        String lessonStr = lesson.getText().toString(),
                questionStr = question.getText().toString(),
                answerStr = answer.getText().toString();
        if (lessonStr.isEmpty()) {
            lessonTil.setError(getString(R.string.error_empty));
            error = true;
        } else if (lessonStr.length() > Limits.LESSON_MAX_LENGTH) {
            lessonTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { lessonTil.setError(null); }
        if (questionStr.isEmpty()) {
            questionTil.setError(getString(R.string.error_empty));
            error = true;
        } else { questionTil.setError(null); }
        if (!error) {
            if (editing) {
                currentQuestion++;
                if (currentQuestion < questions.size()) { setFieldsForEditing(); }
                if (currentQuestion <= questions.size()) {
                    questions.get(currentQuestion - 1)
                             .edit(lessonStr, questionStr, answerStr, imageFile);
                }
                if (currentQuestion == questions.size() - 1) { mergeButtons(); }
                if (currentQuestion == questions.size()) { onBackPressed(); }
            } else {
                course.addQuestion(lessonStr, questionStr, answerStr, imageFile);
                onBackPressed();
            }
        }
    }

    private void mergeButtons() {
        nextText.setText(R.string.done);
        buttonsLayout.removeView(done);
        buttonsLayout.invalidate();
    }

    private void done() {
        doSubmit();
        if (currentQuestion < questions.size()) { onBackPressed(); }
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
