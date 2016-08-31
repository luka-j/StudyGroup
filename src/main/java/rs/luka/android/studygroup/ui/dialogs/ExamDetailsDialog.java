package rs.luka.android.studygroup.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Exam;


/**
 * Created by luka on 30.7.15..
 */
public class ExamDetailsDialog extends DialogFragment {
    private static final String ARG_EXAM = "exam";
    private Callbacks callbacks;

    public static ExamDetailsDialog newInstance(Exam exam) {
        ExamDetailsDialog f    = new ExamDetailsDialog();
        Bundle            args = new Bundle(1);
        args.putParcelable(ARG_EXAM, exam);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Exam exam = getArguments().getParcelable(ARG_EXAM);
        View       v    = getActivity().getLayoutInflater()
                                       .inflate(R.layout.dialog_exam_details, null, false);
        ((TextView) v.findViewById(R.id.property_subject)).setText(exam.getSubject());
        ((TextView) v.findViewById(R.id.property_class)).setText(exam.getKlassName());
        ((TextView) v.findViewById(R.id.property_date)).setText(exam.getDate(getActivity()));
        ((TextView) v.findViewById(R.id.property_lessons)).setText(exam.getLesson());
        ((TextView) v.findViewById(R.id.property_teacher)).setText(exam.getTeacher());
        ((TextView) v.findViewById(R.id.property_type)).setText(exam.getType());
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        return builder.customView(v, true)
                      .title(exam.getTitle())
                      .neutralText(R.string.lesson)
                      .negativeText(R.string.close)
                      .callback(new MaterialDialog.ButtonCallback() {
                          @Override
                          public void onNeutral(MaterialDialog dialog) {
                              callbacks.onShowLesson(exam);
                          }
                      })
                      .show();
    }

    public interface Callbacks {
        void onShowLesson(Exam exam);
    }
}
