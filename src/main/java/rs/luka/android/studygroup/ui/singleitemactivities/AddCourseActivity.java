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
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.net.SocketException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.recyclers.CourseActivity;
import rs.luka.android.studygroup.ui.recyclers.GroupActivity;

/**
 * Created by luka on 13.7.15..
 */
public class AddCourseActivity extends AppCompatActivity {

    public static final  String EXTRA_COURSE          = CourseActivity.EXTRA_COURSE;
    public static final  String EXTRA_GROUP           = GroupActivity.EXTRA_GROUP;
    public static final  String EXTRA_MY_PERMISSION   = "permission";
    private static final String STATE_IMAGE_FILE_PATH = "stImage";
    private static final int    INTENT_IMAGE          = 0;
    private static final String TAG                   = "AddCourseActivity";

    private EditText        subject;
    private TextInputLayout subjectTil;
    private EditText        teacher;
    private TextInputLayout teacherTil;
    private EditText        year;
    private TextInputLayout yearTil;
    private CardView        add;
    private ImageView       image;
    private LinearLayout privateContainer;
    private CheckBox     privateBox;
    private CircularProgressView progressView;
    private File imageFile = new File(LocalImages.APP_IMAGE_DIR, "courseimg.temp");
    private Group           group;
    private Course          course;
    private boolean         editing;
    private int myPermission;

    private NetworkExceptionHandler exceptionHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        exceptionHandler = new NetworkExceptionHandler.DefaultHandler(this) {
            @Override
            public void finishedSuccessfully() {
                super.finishedSuccessfully();
                AddCourseActivity.this.onBackPressed();
            }
            @Override
            public void handleOffline() {
                InfoDialog.newInstance(getString(R.string.error_offline_edit_title),
                                       getString(R.string.error_offline_edit_text))
                        .show(getSupportFragmentManager(), "");
                Network.Status.setOffline();
                progressView.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
            }
            @Override
            public void finishedUnsuccessfully() {
                super.finishedUnsuccessfully();
                progressView.setVisibility(View.GONE);
                add.setVisibility(View.VISIBLE);
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
        group = getIntent().getParcelableExtra(EXTRA_GROUP);
        course = getIntent().getParcelableExtra(EXTRA_COURSE);
        editing = course != null;
        myPermission = getIntent().getIntExtra(EXTRA_MY_PERMISSION, Group.PERM_WRITE);

        setContentView(R.layout.activity_add_course);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (NavUtils.getParentActivityIntent(this) != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        subject = (EditText) findViewById(R.id.add_course_name_input);
        subjectTil = (TextInputLayout) findViewById(R.id.add_course_name_til);
        teacher = (EditText) findViewById(R.id.add_course_prof_input);
        teacherTil = (TextInputLayout) findViewById(R.id.add_course_prof_til);
        year = (EditText) findViewById(R.id.add_course_year_input);
        yearTil = (TextInputLayout) findViewById(R.id.add_course_year_til);
        add = (CardView) findViewById(R.id.button_add);
        progressView = (CircularProgressView) findViewById(R.id.add_course_cpv);
        image = (ImageView) findViewById(R.id.add_course_image);
        privateContainer = (LinearLayout) findViewById(R.id.private_checkbox_container);
        privateBox = (CheckBox) findViewById(R.id.private_cb);
        if (editing) {
            subject.setText(course.getSubject());
            teacher.setText(course.getTeacher());
            if (course.getYear() != null) {
                year.setText(String.valueOf(course.getYear()));
            }
            if (course.hasImage()) {
                course.getImage(this,
                                getResources().getDimensionPixelOffset(R.dimen.addview_image_size),
                                exceptionHandler, image);
            }
            subject.setSelection(subject.getText().length());
        }
        if(myPermission < Group.PERM_MODIFY || editing) {
            privateContainer.setVisibility(View.GONE);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == INTENT_IMAGE) {
                if (data
                    != null) { //ako je data==null, fotografija je napravljena kamerom, nije iz galerije
                    imageFile = new File(Utils.getRealPathFromUri(this, data.getData()));
                }
                image.setImageBitmap(LocalImages.loadImage(imageFile,
                                                           getResources().getDimensionPixelSize(R.dimen.addview_image_size)));
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
        } else if (subjectText.length() >= Limits.COURSE_NAME_MAX_LENGTH) {
            subjectTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { subjectTil.setError(null); }
        if (teacherText.length() >= Limits.COURSE_TEACHER_MAX_LENGTH) {
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
                course.edit(this, subjectText, teacherText, yearText, imageFile, exceptionHandler);
            } else {
                if (imageFile != null && imageFile.exists()) {
                    group.addCourse(this, subjectText, teacherText, yearText, imageFile, privateBox.isChecked(), exceptionHandler);
                } else {
                    group.addCourse(this, subjectText, teacherText, yearText, null, privateBox.isChecked(), exceptionHandler);
                }
            }
            add.setVisibility(View.GONE);
            progressView.setVisibility(View.VISIBLE);
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
