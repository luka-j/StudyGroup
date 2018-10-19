package rs.luka.android.studygroup.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.model.Group;

/**
 * Created by luka on 21.10.16..
 */
public class AddAnnouncementDialog extends DialogFragment implements Network.NetworkCallbacks<String> {
    private static final String ARG_GROUP = "aGroup";
    private static final String TAG       = "dialogs.AddAnnouncement";

    private NetworkExceptionHandler exceptionHandler;
    private Activity attachedTo;
    private Group group;

    public static AddAnnouncementDialog newInstance(Group group) {
        AddAnnouncementDialog dialog    = new AddAnnouncementDialog();
        Bundle       args = new Bundle(1);
        args.putParcelable(ARG_GROUP, group);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachedTo = activity;
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler((AppCompatActivity) activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        group = getArguments().getParcelable(ARG_GROUP);
        final List<Integer>    years   = group.getCourseYears();
        final List<String>     yearStrings = new ArrayList<>(years.size());
        for(Integer y : years) yearStrings.add(getResources().getString(R.string.year_no, y));

        final View           v         = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_announcement, null, false);
        LinearLayout         root      = (LinearLayout) v.findViewById(R.id.add_announcement_root);
        final List<CheckBox> yearBoxes = new ArrayList<>(years.size());
        for(String str : yearStrings) {
            CheckBox yearBox = new CheckBox(getActivity());
            yearBox.setText(str);
            yearBoxes.add(yearBox);
            root.addView(yearBox);
        }

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        return builder.customView(v, true)
                      .autoDismiss(false)
                      .title(getString(R.string.add_announcement_title))
                      .positiveText(R.string.create)
                      .negativeText(R.string.cancel)
                      .onPositive(new MaterialDialog.SingleButtonCallback() {
                          @Override
                          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                              EditText     input          = (EditText) v.findViewById(R.id.add_announcement_text);
                              String text = input.getText().toString();
                              Set<Integer> selectedYears = new HashSet<>(yearBoxes.size());
                              for(int i=0; i<yearBoxes.size(); i++)
                                  if(yearBoxes.get(i).isChecked())
                                      selectedYears.add(years.get(i));
                              try {
                                  group.addAnnouncement(0, text, selectedYears, AddAnnouncementDialog.this);
                              } catch (IOException e) {
                                  exceptionHandler.handleIOException(e);
                              }
                          }
                      })
                      .onNegative(new MaterialDialog.SingleButtonCallback() {
                          @Override
                          public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                              dismiss();
                          }
                      }).show();
    }

    @Override
    public void onRequestCompleted(int id, Network.Response<String> response) {
        if(id == 0) {
            if(response.responseCode == Network.Response.RESPONSE_CREATED) {
                attachedTo.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(attachedTo, R.string.toast_announcement_created, Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
            } else {
                Network.Response<String> handled = response.handleErrorCode(exceptionHandler);
                if(handled != response) onRequestCompleted(0, handled);
            }
        } else {
            Log.w(TAG, "unknown request id");
        }
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        if(ex instanceof IOException)
            exceptionHandler.handleIOException((IOException)ex);
        else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog.newInstance(getString(R.string.error_unknown_ex_title),
                                           getString(R.string.error_unknown_ex_text))
                              .show(((AppCompatActivity)attachedTo).getFragmentManager(), "");
                }
            });
            Log.e(TAG, "Unknown Throwable caught", ex);
        }
    }
}
