package rs.luka.android.studygroup.activities.singleitemactivities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Date;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.Utils;
import rs.luka.android.studygroup.activities.recyclers.LessonActivity;
import rs.luka.android.studygroup.io.Adder;
import rs.luka.android.studygroup.io.Limits;

/**
 * Created by luka on 14.7.15..
 */
public class AddQuestionFragment extends Fragment {

    private static final int IDEAL_IMAGE_DIMENSION = 300;
    private static final int INTENT_IMAGE = 0;
    private static final File imageDir = new File(Environment.getExternalStorageDirectory().toString() + "/DCIM/StudyGroup/");
    private EditText lesson;
    private EditText answer;
    private EditText question;
    private TextInputLayout lessonTil;
    private TextInputLayout questionTil;
    private CardView add;
    private ImageView image;
    private File imageFile;

    public static AddQuestionFragment newInstance(String lesson, String course) {
        AddQuestionFragment f = new AddQuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(LessonActivity.EXTRA_CURRENT_LESSON, lesson);
        args.putSerializable(LessonActivity.EXTRA_CURRENT_COURSE, course);
        f.setArguments(args);
        return f;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_question, container, false);

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        lesson = (EditText) view.findViewById(R.id.add_question_lesson_input);
        answer = (EditText) view.findViewById(R.id.add_question_answer_input);
        question = (EditText) view.findViewById(R.id.add_question_text_input);
        lessonTil = (TextInputLayout) view.findViewById(R.id.add_question_lesson_til);
        questionTil = (TextInputLayout) view.findViewById(R.id.add_question_text_til);
        add = (CardView) view.findViewById(R.id.button_add);
        image = (ImageView) view.findViewById(R.id.add_question_image);

        lesson.setText(getArguments().getString(LessonActivity.EXTRA_CURRENT_LESSON));
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
                if (!imageDir.isDirectory())
                    imageDir.mkdir();
                imageFile = new File(imageDir, getArguments().getString(LessonActivity.EXTRA_CURRENT_COURSE) +
                        " - " + getArguments().getString(LessonActivity.EXTRA_CURRENT_LESSON) + " "
                        + new Date().getTime() + ".jpg");
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                Intent chooserIntent = Intent.createChooser(camera, getString(R.string.select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{gallery});
                startActivityForResult(chooserIntent, INTENT_IMAGE);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == INTENT_IMAGE) {
                if (data != null) { //ako je data==null, fotografija je napravljena kamerom, nije iz galerije
                    imageFile = new File(Utils.getRealPathFromURI(getActivity(), data.getData()));
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
        } else lessonTil.setError(null);
        if (questionStr.isEmpty()) {
            questionTil.setError(getString(R.string.error_empty));
            error = true;
        } else questionTil.setError(null);
        if (!error) {
            Adder.addQuestion(lessonStr, questionStr, answerStr, imageFile);
            getActivity().onBackPressed();
        }
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
