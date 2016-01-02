package rs.luka.android.studygroup.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import rs.luka.android.studygroup.R;

/**
 * Created by luka on 2.1.16..
 */
public class ErrorDialog extends DialogFragment {
    private Callbacks callbacks;
    public static final String ARG_ERROR_TITLE = "studygroup.dialog.errortile";
    public static final String ARG_ERROR_MESSAGE = "studygroup.dialog.errormsg";

    public static ErrorDialog newInstance(String title, String content) {
        ErrorDialog f    = new ErrorDialog();
        Bundle             args = new Bundle();
        args.putString(ARG_ERROR_TITLE, title);
        args.putString(ARG_ERROR_MESSAGE, content);
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
        input.requestFocus();
        input.setSelection(input.getText().length());
        //til.addView(input);
        return builder.title(getArguments().getString(ARG_ERROR_TITLE))
                      .content(getArguments().getString(ARG_ERROR_MESSAGE))
                      .positiveText(R.string.ok)
                      .autoDismiss(true)
                      .onPositive(new MaterialDialog.SingleButtonCallback() {
                          @Override
                          public void onClick(@NonNull MaterialDialog materialDialog,
                                              @NonNull DialogAction dialogAction) {
                              callbacks.onErrorDialogClosed();
                          }
                      })
                      .show();
    }

    public interface Callbacks {
        void onErrorDialogClosed();
    }
}
