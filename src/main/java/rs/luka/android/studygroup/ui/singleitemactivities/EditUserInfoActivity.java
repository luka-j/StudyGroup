package rs.luka.android.studygroup.ui.singleitemactivities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.File;
import java.io.IOException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.io.LocalImages;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.network.UserManager;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.dialogs.InputDialog;

/**
 * Created by luka on 5.2.16..
 */
public class EditUserInfoActivity extends AppCompatActivity implements InputDialog.Callbacks,
                                                                       Network.NetworkCallbacks<String> {

    private static final String DIALOG_CURRENT_PASS = "currentPass";
    private static final String DIALOG_NEW_PASS     = "newPass";
    private static final int INTENT_IMAGE           = 0;
    private static final int REQUEST_CHECK_PASS     = 1;
    private static final int REQUEST_CHANGE_PASS    = 2;
    private static final String TAG                 = "EditUserInfoActivity";

    private String pass;

    private Toolbar                 toolbar;
    private TextInputLayout         emailTil;
    private TextInputLayout         usernameTil;
    private EditText                emailInput;
    private EditText                usernameInput;
    private ImageView               image;
    private File                    imageFile;
    private NetworkExceptionHandler handler;
    private CircularProgressView    progressView;
    private CardView                edit;
    private CardView                changePass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);
        handler = new NetworkExceptionHandler.DefaultHandler(this) {
            @Override
            public void finishedSuccessfully() {
                super.finishedSuccessfully();
                EditUserInfoActivity.this.onBackPressed();
            }
            @Override
            public void handleOffline() {
                InfoDialog.newInstance(getString(R.string.error_offline_edit_title),
                                       getString(R.string.error_offline_edit_text))
                          .show(getSupportFragmentManager(), "");
                Network.Status.setOffline();
                progressView.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
            }
            @Override
            public void finishedUnsuccessfully() {
                progressView.setVisibility(View.GONE);
                edit.setVisibility(View.VISIBLE);
            }
        };

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailTil = (TextInputLayout) findViewById(R.id.edit_user_email_til);
        usernameTil = (TextInputLayout)findViewById(R.id.edit_user_username_til);
        emailInput = (EditText) findViewById(R.id.edit_user_email_input);
        usernameInput = (EditText) findViewById(R.id.edit_user_username_input);
        image = (ImageView) findViewById(R.id.edit_user_image);
        progressView = (CircularProgressView) findViewById(R.id.edit_user_cpv);
        edit = (CardView) findViewById(R.id.button_add);
        changePass = (CardView) findViewById(R.id.button_change_password);

        User me = User.getLoggedInUser();
        if(me.hasImage()) {
            me.getImage(this, getResources().getDimensionPixelSize(R.dimen.addview_image_size), handler, image);
        }
        emailInput.setText(User.getMyEmail());
        usernameInput.setText(me.getName());
        usernameInput.setSelection(me.getName().length());
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera  = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent gallery = new Intent(Intent.ACTION_PICK);
                gallery.setType("image/*");
                imageFile = new File(LocalImages.APP_IMAGE_DIR + "users/me.jpg");
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                Intent chooserIntent = Intent.createChooser(camera,
                                                            getString(R.string.select_image));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{gallery});
                startActivityForResult(chooserIntent, INTENT_IMAGE);
            }
        });
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputDialog.newInstance(R.string.enter_current_password, null, R.string.ok, R.string.cancel, null, R.string.password)
                .show(getSupportFragmentManager(), DIALOG_CURRENT_PASS);
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doEdit();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == INTENT_IMAGE) {
                if (data
                    != null) { //ako je data==null, fotografija je napravljena kamerom, nije iz galerije
                    imageFile = new File(Utils.getRealPathFromUri(this, data.getData()));
                }
                image.setImageBitmap(LocalImages.loadImage(imageFile,
                                                           getResources().getDimensionPixelSize(R.dimen.addview_image_size)));
            }
        }
    }

    private void doEdit() {
        boolean hasErrors = false;
        if(emailInput.getText().length() > Limits.USER_EMAIL_MAX_LENGTH) {
            emailTil.setError(getString(R.string.error_too_long));
            hasErrors = true;
        } else emailTil.setError(null);
        if(usernameInput.getText().length() > Limits.USER_NAME_MAX_LENGTH) {
            usernameTil.setError(getString(R.string.error_too_long));
            hasErrors = true;
        } else usernameTil.setError(null);
        if(!Utils.isEmailValid(emailInput.getText())) {
            emailTil.setError(getString(R.string.error_invalid_email));
            hasErrors = true;
        } else emailTil.setError(null);
        if(!hasErrors) {
            DataManager.setMyProfile(usernameInput.getText().toString(), emailInput.getText().toString(), imageFile, handler);
            edit.setVisibility(View.GONE);
            progressView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFinishedInput(DialogFragment dialog, String s) {
        switch (dialog.getTag()) {
            case DIALOG_CURRENT_PASS:
                UserManager.checkPassword(REQUEST_CHECK_PASS, s, this);
                pass = s;
                break;
            case DIALOG_NEW_PASS:
                UserManager.changePassword(REQUEST_CHANGE_PASS, pass, s, this);
                break;
        }
    }

    @Override
    public void onRequestCompleted(int id, Network.Response<String> response) {
        switch (id) {
            case REQUEST_CHECK_PASS:
                if(response.responseCode == Network.Response.RESPONSE_OK) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            InputDialog.newInstance(R.string.enter_new_pass, null, R.string.submit, R.string.cancel,
                                                    null, R.string.password)
                                    .show(getSupportFragmentManager(), DIALOG_NEW_PASS);
                        }
                    });
                } else if( response.responseCode == Network.Response.RESPONSE_UNAUTHORIZED
                           && "wrong password".equals(response.errorMessage)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            InfoDialog.newInstance(getString(R.string.error_wrong_password_title),
                                                   getString(R.string.error_wrong_password_text))
                                    .show(getSupportFragmentManager(), "");
                        }
                    });
                } else {
                    response.handleErrorCode(handler);
                }
                break;
            case REQUEST_CHANGE_PASS:
                if(response.responseCode == Network.Response.RESPONSE_OK) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            InfoDialog.newInstance(getString(R.string.info_pass_changed_title),
                                                   getString(R.string.info_pass_changed_text))
                                    .show(getSupportFragmentManager(), "");
                        }
                    });
                } else {
                    response.handleErrorCode(handler);
                }
                break;
        }
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        if(ex instanceof IOException)
            handler.handleIOException((IOException)ex);
        else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog.newInstance(getString(R.string.error_unknown_ex_title),
                                           getString(R.string.error_unknown_ex_text))
                              .show(getSupportFragmentManager(), "");
                }
            });
            Log.e(TAG, "Unknown Throwable caught", ex);
        }
    }
}
