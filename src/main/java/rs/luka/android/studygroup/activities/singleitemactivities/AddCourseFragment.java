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

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.Utils;
import rs.luka.android.studygroup.io.Adder;
import rs.luka.android.studygroup.io.Limits;

/**
 * Created by luka on 13.7.15..
 */
public class AddCourseFragment extends Fragment {

    private static final int IDEAL_IMAGE_DIMENSION = 300;
    private static final int INTENT_IMAGE = 0;
    private static final File imageDir = new File(Environment.getExternalStorageDirectory().toString() + "/DCIM/StudyGroup/");

    private EditText subject;
    private TextInputLayout subjectTil;
    private EditText teacher;
    private TextInputLayout teacherTil;
    private EditText year;
    private TextInputLayout yearTil;
    private CardView add;
    private ImageView image;
    private File imageFile;


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
        image = (ImageView) view.findViewById(R.id.add_course_image);

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
                if (!imageDir.isDirectory())
                    imageDir.mkdir();
                imageFile = new File(imageDir, "course " + subject + " image.jpg");
                if (imageFile.exists())
                    imageFile.delete();
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
        } else subjectTil.setError(null);
        if (teacherText.length() > 128) {
            teacherTil.setError(getString(R.string.error_too_long));
            error = true;
        } else teacherTil.setError(null);
        if (!yearText.isEmpty() && Integer.parseInt(yearText) > Limits.COURSE_YEAR_MAX) {
            yearTil.setError(getString(R.string.error_number_too_large, getString(R.string.year)));
            error = true;
        } else if (!yearText.isEmpty() && Integer.parseInt(yearText) < Limits.COURSE_YEAR_MIN) {
            yearTil.setError(getString(R.string.error_negative_number, getString(R.string.year)));
            error = true;
        } else yearTil.setError(null);
        if (!error) {
            if (imageFile.exists()) {
                Adder.addCourse(subjectText, teacherText, yearText, imageFile);
            } else {
                Adder.addCourse(subjectText, teacherText, yearText, null);
            }
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
