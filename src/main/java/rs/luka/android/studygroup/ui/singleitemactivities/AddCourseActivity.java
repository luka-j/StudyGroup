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
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.Utils;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.recyclers.GroupActivity;

/**
 * Created by luka on 13.7.15..
 */
public class AddCourseActivity extends AppCompatActivity {

    private static final int  IDEAL_IMAGE_DIMENSION = 300;
    private static final int  INTENT_IMAGE          = 0;
    private static final File imageDir              = new File(
            Environment.getExternalStorageDirectory().toString() + "/DCIM/StudyGroup/");

    private EditText        subject;
    private TextInputLayout subjectTil;
    private EditText        teacher;
    private TextInputLayout teacherTil;
    private EditText        year;
    private TextInputLayout yearTil;
    private CardView        add;
    private ImageView       image;
    private File            imageFile;
    private Group           group;
    private Course          course;
    private boolean         editing;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        group = getIntent().getParcelableExtra(GroupActivity.EXTRA_GROUP);
        course = getIntent().getParcelableExtra(GroupActivity.EXTRA_COURSE);
        editing = course != null;

        setContentView(R.layout.activity_add_course);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        subject = (EditText) findViewById(R.id.add_course_name_input);
        subjectTil = (TextInputLayout) findViewById(R.id.add_course_name_til);
        teacher = (EditText) findViewById(R.id.add_course_prof_input);
        teacherTil = (TextInputLayout) findViewById(R.id.add_course_prof_til);
        year = (EditText) findViewById(R.id.add_course_year_input);
        yearTil = (TextInputLayout) findViewById(R.id.add_course_year_til);
        add = (CardView) findViewById(R.id.button_add);
        image = (ImageView) findViewById(R.id.add_course_image);
        if (editing) {
            subject.setText(course.getSubject());
            teacher.setText(course.getTeacher());
            if (course.getYear() != null) {
                year.setText(course.getYear().toString());
            }
            if (course.hasImage()) {
                image.setImageBitmap(course.getImage());
            }
            subject.setSelection(subject.getText().length());
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        year.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit();
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
                imageFile = new File(imageDir, subject + " image.jpg");
                if (imageFile.exists()) { imageFile.delete(); }
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

    private void submit() {
        boolean error = false;
        String subjectText = subject.getText().toString(),
                teacherText = teacher.getText().toString(),
                yearText = year.getText().toString();
        if (subjectText.isEmpty()) {
            subjectTil.setError(getString(R.string.error_empty));
            error = true;
        } else if (subjectText.length() > Limits.COURSE_NAME_MAX_LENGTH) {
            subjectTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { subjectTil.setError(null); }
        if (teacherText.length() > Limits.COURSE_TEACHER_MAX_LENGTH) {
            teacherTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { teacherTil.setError(null); }
        if (!yearText.isEmpty() && Integer.parseInt(yearText) > Limits.COURSE_YEAR_MAX) {
            yearTil.setError(getString(R.string.error_number_too_large, getString(R.string.year)));
            error = true;
        } else if (!yearText.isEmpty() && Integer.parseInt(yearText) < Limits.COURSE_YEAR_MIN) {
            yearTil.setError(getString(R.string.error_negative_number, getString(R.string.year)));
            error = true;
        } else { yearTil.setError(null); }
        if (!error) {
            if (editing) {
                course.edit(subjectText, teacherText, yearText, imageFile);
            } else {
                if (imageFile != null && imageFile.exists()) {
                    group.addCourse(subjectText, teacherText, yearText, imageFile);
                } else {
                    group.addCourse(subjectText, teacherText, yearText, null);
                }
            }
            onBackPressed();
        }
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
