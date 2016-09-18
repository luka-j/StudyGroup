package rs.luka.android.studygroup.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import rs.luka.android.studygroup.R;

/**
 * Created by luka on 18.9.16..
 */
public class InputHelpDialog extends DialogFragment {
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v    = getActivity().getLayoutInflater()
                                 .inflate(R.layout.dialog_input_help, null, false);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        return builder.customView(v, true)
                      .title(R.string.input_help_title)
                      .positiveText(R.string.ok)
                      .show();
    }
}
