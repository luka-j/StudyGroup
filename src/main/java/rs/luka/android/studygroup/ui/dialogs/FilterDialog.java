package rs.luka.android.studygroup.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.misc.Utils;

/**
 * Created by luka on 29.7.15..
 */
public class FilterDialog extends DialogFragment {
    private static final String ARG_ITEMS = "aItems";
    private static final String ARG_PRESELECTED = "aSelected";
    private Callbacks callbacks;

    public static FilterDialog newInstance(String[] items, int[] selected) {
        FilterDialog f    = new FilterDialog();
        Bundle       args = new Bundle(1);
        args.putStringArray(ARG_ITEMS, items);
        args.putIntArray(ARG_PRESELECTED, selected);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[]         items   = getArguments().getStringArray(ARG_ITEMS);
        final int[]            selected = getArguments().getIntArray(ARG_PRESELECTED);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        return builder.title(getString(R.string.filter_courses))
                      .positiveText(R.string.filter)
                      .negativeText(R.string.cancel)
                      .items(items)
                      .itemsCallbackMultiChoice(Utils.intToIntegerArray(selected),
                                                new MaterialDialog.ListCallbackMultiChoice() {
                                                    @Override
                                                    public boolean onSelection(
                                                            MaterialDialog materialDialog,
                                                            Integer[] integers,
                                                            CharSequence[] charSequences) {
                                                        callbacks.onFiltered(integers);
                                                        return true;
                                                    }
                                                }).show();
    }

    public interface Callbacks {
        void onFiltered(Integer[] selected);
    }
}
