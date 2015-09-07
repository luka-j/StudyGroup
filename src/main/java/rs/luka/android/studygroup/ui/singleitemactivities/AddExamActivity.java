package rs.luka.android.studygroup.ui.singleitemactivities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Exam;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.recyclers.RootActivity;
import rs.luka.android.studygroup.ui.recyclers.ScheduleActivity;
import rs.luka.android.studygroup.ui.recyclers.SelectCourseActivity;

public class AddExamActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final  String EXTRA_GROUP           = RootActivity.EXTRA_GROUP;
    private static final String DATA_SELECTED_COURSE  = "dCourse";
    private static final String DATA_SELECTED_DATE    = "dDate";
    private static final int    REQUEST_SELECT_COURSE = 0;

    private Group    group;
    private Course   course;
    private Calendar selectedDate;

    private Toolbar         toolbar;
    private EditText        courseEdit;
    private TextInputLayout courseTil;
    private EditText        classEdit;
    private TextInputLayout classTil;
    private EditText        lessonEdit;
    private TextInputLayout lessonTil;
    private EditText        typeEdit;
    private TextInputLayout typeTil;
    private CardView        date;
    private TextView        dateText;
    private CardView        submit;

    private boolean goBack;
    private boolean editing;
    private Exam    exam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exam);
        exam = getIntent().getParcelableExtra(ScheduleActivity.EXTRA_EXAM);
        editing = exam != null;
        if (editing) {
            course = exam.getCourse();
            selectedDate = exam.getCalendar();
            initViews();
        } else if (savedInstanceState == null || savedInstanceState.getParcelable(DATA_SELECTED_COURSE) == null) {
            group = getIntent().getParcelableExtra(ScheduleActivity.EXTRA_GROUP);
            startActivityForResult(new Intent(this, SelectCourseActivity.class).putExtra(EXTRA_GROUP,
                                                                                         group),
                                   REQUEST_SELECT_COURSE);
        } else {
            course = savedInstanceState.getParcelable(DATA_SELECTED_COURSE);
            if (savedInstanceState.getLong(DATA_SELECTED_DATE) != 0) {
                selectedDate = Calendar.getInstance();
                selectedDate.setTimeInMillis(savedInstanceState.getLong(DATA_SELECTED_DATE));
            }
            initViews();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setContentView(R.layout.activity_add_exam);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SELECT_COURSE) {
                course = data.getParcelableExtra(SelectCourseActivity.EXTRA_COURSE);
                initViews();
            }
        } else {
            goBack = true;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(goBack) {
            onBackPressed();
            goBack = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(course != null)
            outState.putParcelable(DATA_SELECTED_COURSE, course);
        if(selectedDate != null)
            outState.putLong(DATA_SELECTED_DATE, selectedDate.getTimeInMillis());
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.add_exam);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        if(NavUtils.getParentActivityName(this) != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        classEdit = (EditText) findViewById(R.id.add_exam_class_input);
        typeEdit = (EditText) findViewById(R.id.add_exam_type_input);
        courseTil = (TextInputLayout) findViewById(R.id.add_exam_course_til);
        classTil = (TextInputLayout) findViewById(R.id.add_exam_class_til);
        lessonTil = (TextInputLayout) findViewById(R.id.add_exam_lesson_til);
        typeTil = (TextInputLayout) findViewById(R.id.add_exam_type_til);
        date = (CardView) findViewById(R.id.button_date);
        dateText = (TextView) findViewById(R.id.button_date_text);
        submit = (CardView) findViewById(R.id.button_add);
        dateText.setText(DateFormat.getDateInstance().format(selectedDate == null ? new Date()
                                                                                  : new Date(selectedDate.getTimeInMillis())));
        courseEdit = (EditText) findViewById(R.id.add_exam_course_input);
        lessonEdit = (EditText) findViewById(R.id.add_exam_lesson_input);
        lessonEdit.requestFocus();
        courseEdit.setText(course.getSubject());
        classEdit.setText(course.getYear() == null ? "" : course.getYear().toString());
        classEdit.setSelection(classEdit.length());

        if (editing) {
            lessonEdit.setText(exam.getLesson());
            lessonEdit.setSelection(lessonEdit.length());
            classEdit.setText(exam.getKlassName());
            classEdit.setSelection(classEdit.length());
            typeEdit.setText(exam.getType());
            typeEdit.setSelection(typeEdit.length());
        }

        typeEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submit();
                    return true;
                }
                return false;
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        AddExamActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                                                                   );
                dpd.vibrate(true);
                dpd.show(getFragmentManager(), null);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    private void submit() {
        String  classText  = classEdit.getText().toString();
        String  lessonText = lessonEdit.getText().toString();
        String  typeText   = typeEdit.getText().toString();
        boolean error      = false;
        if (classText.length() >= Limits.EXAM_CLASS_MAX_LENGTH) {
            classTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { classTil.setError(null); }
        if (lessonText.length() >= Limits.EXAM_LESSON_MAX_LENGTH) {
            lessonTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { lessonTil.setError(null); }
        if (typeText.length() >= Limits.EXAM_TYPE_MAX_LENGTH) {
            typeTil.setError(getString(R.string.error_too_long));
            error = true;
        } else { typeTil.setError(null); }
        if (!error) {
            if (editing) {
                exam.edit(this, classText, lessonText, typeText, selectedDate.getTime());
            } else {
                course.addExam(this,
                               classEdit.getText().toString(),
                               lessonEdit.getText().toString(),
                               typeEdit.getText().toString(),
                               selectedDate == null ? new GregorianCalendar() : selectedDate);
            }
            onBackPressed();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        selectedDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        dateText.setText(DateFormat.getDateInstance().format(new Date(selectedDate.getTimeInMillis())));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
