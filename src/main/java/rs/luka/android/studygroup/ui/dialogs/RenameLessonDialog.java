package rs.luka.android.studygroup.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.ui.recyclers.CourseActivity;

/**
 * Created by luka on 23.7.15..
 */
public class RenameLessonDialog extends DialogFragment {
    Callbacks callbacks;

    public static RenameLessonDialog newInstance(String lesson) {
        RenameLessonDialog f    = new RenameLessonDialog();
        Bundle             args = new Bundle();
        args.putString(CourseActivity.EXTRA_LESSON_NAME, lesson);
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
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        //TextInputLayout til = new TextInputLayout(getActivity());
        final EditText input = new EditText(getActivity());
        input.setText(getArguments().getString(CourseActivity.EXTRA_LESSON_NAME));
        input.requestFocus();
        input.setSelection(input.getText().length());
        //til.addView(input);
        return builder.title(R.string.rename_lesson)
                      .positiveText(R.string.rename)
                      .negativeText(R.string.cancel)
                .input(getString(R.string.rename_lesson),
                       getArguments().getString(CourseActivity.EXTRA_LESSON_NAME),
                       false,
                       new MaterialDialog.InputCallback() {
                           @Override
                           public void onInput(MaterialDialog materialDialog,
                                               CharSequence charSequence) {
                               callbacks.onRenamed(charSequence.toString());
                           }
                       })
                      .show();
    }

    public interface Callbacks {
        void onRenamed(String s);
    }
}
