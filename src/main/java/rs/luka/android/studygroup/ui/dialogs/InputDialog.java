package rs.luka.android.studygroup.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import rs.luka.android.studygroup.ui.recyclers.CourseActivity;

/**
 * Created by luka on 23.7.15.
 */
public class InputDialog extends DialogFragment {
    private Callbacks callbacks;
    private static final String ARG_TITLE = "aTitle";
    private static final String ARG_POSITIVE = "aPositive";
    private static final String ARG_NEGATIVE = "aNegative";
    private static final String ARG_TEXT = "aText";
    private static final String ARG_INITIAL = "aInitialText";
    private static final String ARG_HINT = "aHint";

    public static InputDialog newInstance(@StringRes int title, String text, @StringRes int positiveText,
                                          @StringRes int negativeText, String initialText, @StringRes int hint) {
        InputDialog f    = new InputDialog();
        Bundle      args = new Bundle();
        args.putInt(ARG_TITLE, title);
        args.putInt(ARG_POSITIVE, positiveText);
        args.putInt(ARG_NEGATIVE, negativeText);
        args.putString(ARG_INITIAL, initialText);
        args.putString(ARG_TEXT, text);
        args.putInt(ARG_HINT, hint);
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
        Bundle args = getArguments();
        String text = args.getString(ARG_TEXT);
        int hintId = args.getInt(ARG_HINT);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        //TextInputLayout til = new TextInputLayout(getActivity());
        final EditText input = new EditText(getActivity());
        input.setText(getArguments().getString(CourseActivity.EXTRA_LESSON_NAME));
        input.requestFocus();
        input.setSelection(input.getText().length());
        //til.addView(input);
        builder.title(args.getInt(ARG_TITLE))
                      .positiveText(args.getInt(ARG_POSITIVE))
                      .negativeText(args.getInt(ARG_NEGATIVE))
                .input(hintId == 0? getString(args.getInt(ARG_TITLE)) : getString(hintId),
                       args.getString(ARG_INITIAL),
                       false,
                       new MaterialDialog.InputCallback() {
                           @Override
                           public void onInput(MaterialDialog materialDialog,
                                               CharSequence charSequence) {
                               callbacks.onFinishedInput(InputDialog.this, charSequence.toString());
                           }
                       });
        if(text != null)
            builder.content(text);
        return builder.show();
    }

    public interface Callbacks {
        void onFinishedInput(DialogFragment dialog, String s);
    }
}
