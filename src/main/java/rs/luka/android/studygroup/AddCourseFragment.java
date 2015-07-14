package rs.luka.android.studygroup;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import rs.luka.android.studygroup.networkcontroller.Adder;

/**
 * Created by luka on 13.7.15..
 */
public class AddCourseFragment extends Fragment {

    private EditText subject;
    private TextInputLayout subjectTil;
    private EditText teacher;
    private TextInputLayout teacherTil;
    private EditText year;
    private TextInputLayout yearTil;
    private CardView add;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_course, container, false);

        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true); //because reasons
        }

        subject = (EditText) view.findViewById(R.id.add_course_name_input);
        subjectTil = (TextInputLayout) view.findViewById(R.id.add_course_name_til);
        teacher = (EditText) view.findViewById(R.id.add_course_prof_input);
        teacherTil = (TextInputLayout) view.findViewById(R.id.add_course_prof_til);
        year = (EditText) view.findViewById(R.id.add_course_year_input);
        yearTil = (TextInputLayout) view.findViewById(R.id.add_course_year_til);
        add = (CardView) view.findViewById(R.id.button_add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean error = false;
                String subjectText = subject.getText().toString(),
                        teacherText = teacher.getText().toString(),
                        yearText = year.getText().toString();
                if (subjectText.isEmpty()) {
                    subjectTil.setError(getString(R.string.error_empty));
                    error = true;
                } else subjectTil.setError(null);
                if (subjectText.length() > 128) {
                    subjectTil.setError(getString(R.string.error_too_long));
                    error = true;
                } else subjectTil.setError(null);
                if (teacherText.length() > 128) {
                    teacherTil.setError(getString(R.string.error_too_long));
                    error = true;
                } else teacherTil.setError(null);
                if (!yearText.isEmpty() && Integer.parseInt(yearText) > 255) {
                    yearTil.setError(getString(R.string.error_number_too_large, getString(R.string.year)));
                    error = true;
                } else yearTil.setError(null);
                if (!yearText.isEmpty() && Integer.parseInt(yearText) < 0) {
                    yearTil.setError(getString(R.string.error_negative_number, getString(R.string.year)));
                    error = true;
                } else yearTil.setError(null);
                if (!error) {
                    Adder.addCourse(subjectText, teacherText, yearText);
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
            }
        });
        return view;
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
