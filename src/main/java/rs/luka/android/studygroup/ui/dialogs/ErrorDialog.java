package rs.luka.android.studygroup.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
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
        if(title == null) title = "I've forgotten a title (please tell me!)";
        if(content == null) content = "I've forgotten to put text here. If I've forgotten the title too, "
                                      + "something is definitely wrong.";
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
    }

    public ErrorDialog registerCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
        return this;
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
                              if(callbacks!=null) callbacks.onErrorDialogClosed();
                          }
                      })
                      .show();
    }

    public interface Callbacks {
        void onErrorDialogClosed();
    }
}
