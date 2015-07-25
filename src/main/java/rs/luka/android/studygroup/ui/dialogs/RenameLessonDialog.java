package rs.luka.android.studygroup.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //TextInputLayout til = new TextInputLayout(getActivity());
        final EditText input = new EditText(getActivity());
        input.setText(getArguments().getString(CourseActivity.EXTRA_LESSON_NAME));
        input.requestFocus();
        input.setSelection(input.getText().length());
        InputMethodManager imm
                = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_FORCED); //TODO: fix
        //til.addView(input);
        return builder.setView(input)
                      .setTitle(R.string.rename_lesson)
                      .setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              callbacks.onRenamed(input.getText().toString());
                          }
                      })
                      .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              callbacks.onCancelDialog();
                          }
                      }).create();
    }

    public interface Callbacks {
        void onRenamed(String s);

        void onCancelDialog();
    }
}
