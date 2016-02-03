package rs.luka.android.studygroup.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import rs.luka.android.studygroup.R;

/**
 * Created by luka on 2.1.16..
 */
public class InfoDialog extends DialogFragment {
    public static final String ARG_ERROR_TITLE = "studygroup.dialog.errortile";
    public static final String ARG_ERROR_MESSAGE = "studygroup.dialog.errormsg";
    private Callbacks callbacks;

    public static InfoDialog newInstance(String title, String content) {
        if(title == null) title = "I've forgotten a title (please tell me!)";
        if(content == null) content = "I've forgotten to put text here. If I've forgotten the title too, "
                                      + "something is definitely wrong.";
        InfoDialog f    = new InfoDialog();
        Bundle     args = new Bundle();
        args.putString(ARG_ERROR_TITLE, title);
        args.putString(ARG_ERROR_MESSAGE, content);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public InfoDialog registerCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        return builder.title(getArguments().getString(ARG_ERROR_TITLE))
                      .content(getArguments().getString(ARG_ERROR_MESSAGE))
                      .positiveText(R.string.ok)
                      .autoDismiss(true)
                      .onPositive(new MaterialDialog.SingleButtonCallback() {
                          @Override
                          public void onClick(@NonNull MaterialDialog materialDialog,
                                              @NonNull DialogAction dialogAction) {
                              if(callbacks!=null) callbacks.onInfoDialogClosed(InfoDialog.this);
                          }
                      })
                      .show();
    }

    public interface Callbacks {
        void onInfoDialogClosed(InfoDialog dialog);
    }
}
