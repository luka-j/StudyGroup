package rs.luka.android.studygroup.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.DateFormat;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Date;

import rs.luka.android.studygroup.R;

/**
 * Created by luka on 21.10.16..
 */
public class AnnouncementDialog extends DialogFragment {
    public static final String ARG_TEXT = "aText";
    public static final String ARG_YEARS = "aYears";
    public static final String ARG_DATE = "aDate";

    public static AnnouncementDialog newInstance(String text, String years, long date) {
        if(text == null) text = "Looks like I have lost announcement text somewhere (please tell me!)";
        AnnouncementDialog dialog = new AnnouncementDialog();
        Bundle     args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putString(ARG_YEARS, years.substring(1, years.length()-1).replace(",", ", "));
        args.putLong(ARG_DATE, date);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        Bundle args = getArguments();
        String                 text    = args.getString(ARG_TEXT);
        final Date             date    = new Date(args.getLong(ARG_DATE));
        Resources r = getResources();
        return builder.title(r.getString(R.string.announcement_title,
                                         r.getString(R.string.year_no,
                                                     args.getString(ARG_YEARS))))
                      .positiveText(R.string.ok)
                      .autoDismiss(true)
                      .content("(" + DateFormat.getDateFormat(getActivity()).format(date) + ")   " + text)
                      .show();
    }

}
