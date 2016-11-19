package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.io.network.UserManager;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;

/**
 * Created by luka on 30.12.15..
 */
public class RegisterActivity extends AppCompatActivity implements Network.NetworkCallbacks<String> {
    private static final String TAG_DIALOG_UNEXPECTED_ERROR = "studygroup.dialog.unexpectederror";
    private static final int REQUEST_REGISTER               = 0;
    private static final int REQUEST_LOGIN                  = 1;
    private static final String TAG                         = "RegisterActivity";

    private boolean requestInProgress = false;
    private CardView register;
    private TextInputLayout emailTil;
    private TextInputLayout usernameTil;
    private TextInputLayout passwordTil;
    private EditText email;
    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailTil = (TextInputLayout) findViewById(R.id.register_email_til);
        usernameTil = (TextInputLayout) findViewById(R.id.register_username_til);
        passwordTil = (TextInputLayout) findViewById(R.id.register_password_til);
        register = (CardView) findViewById(R.id.button_register);
        email = (EditText) findViewById(R.id.register_email_input);
        username = (EditText) findViewById(R.id.register_username_input);
        password = (EditText) findViewById(R.id.register_password_input);
        ((TextView)findViewById(R.id.register_legal)).setMovementMethod(LinkMovementMethod.getInstance());

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!requestInProgress)
                    register();
            }
        });
    }

    private void register() {
        boolean hasErrors = false;
        if( email.getText().length() > Limits.USER_EMAIL_MAX_LENGTH) {
            emailTil.setError(getString(R.string.error_too_long));
            hasErrors = true;
        } else emailTil.setError(null);
        if(username.getText().length() > Limits.USER_NAME_MAX_LENGTH) {
            usernameTil.setError(getString(R.string.error_too_long));
            hasErrors = true;
        } else usernameTil.setError(null);
        if(password.getText().length() > Limits.USER_PASSWORD_MAX_LENGTH) {
            passwordTil.setError(getString(R.string.error_too_long));
            hasErrors = true;
        } else passwordTil.setError(null);
        if(!Utils.isEmailValid(email.getText())) {
            emailTil.setError(getString(R.string.error_invalid_email));
            hasErrors = true;
        } else emailTil.setError(null);
        if(!hasErrors) {
            requestInProgress = true;
            UserManager.register(REQUEST_REGISTER,
                                 email.getText().toString(),
                                 username.getText().toString(),
                                 password.getText().toString(),
                                 this);
        }
    }

    @Override
    public void onRequestCompleted(final int id, final Network.Response<String> response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emailTil.setError(null);
                if (id == REQUEST_REGISTER) {
                    switch (response.responseCode) {
                        case Network.Response.RESPONSE_CREATED:
                            User.instantiateUser(response.responseData,
                                                 RegisterActivity.this.getSharedPreferences(User.PREFS_NAME, MODE_PRIVATE));
                            startActivity(new Intent(RegisterActivity.this, LoadingActivity.class));
                            break;
                        case Network.Response.RESPONSE_DUPLICATE:
                            emailTil.setError(getString(R.string.email_duplicate));
                            break;
                        case Network.Response.RESPONSE_UNAUTHORIZED:
                            UserManager.login(REQUEST_LOGIN,
                                              email.getText().toString(),
                                              password.getText().toString(),
                                              RegisterActivity.this);
                            break;
                        default:
                            InfoDialog.newInstance                                                       (getString(R.string.unexpected_server_error),
                                                   response.getDefaultErrorMessage(RegisterActivity.this))
                                      .show(RegisterActivity.this.getSupportFragmentManager(), TAG_DIALOG_UNEXPECTED_ERROR);
                    }
                } else if (id == REQUEST_LOGIN) {
                    if(response.responseCode == Network.Response.RESPONSE_OK) {
                        User.instantiateUser(response.responseData,
                                             RegisterActivity.this.getSharedPreferences(User.PREFS_NAME, MODE_PRIVATE));
                        startActivity(new Intent(RegisterActivity.this, LoadingActivity.class).addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    } else {
                        InfoDialog.newInstance                                                       (getString(R.string.unexpected_server_error),
                                               response.getDefaultErrorMessage(RegisterActivity.this))
                                  .show(RegisterActivity.this.getSupportFragmentManager(), TAG_DIALOG_UNEXPECTED_ERROR);
                    }
                }
            }
        });
        requestInProgress = false; //todo what if chained register->login request (requestInProgress stays false)
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        NetworkExceptionHandler handler = new NetworkExceptionHandler.DefaultHandler(this) {
            @Override
            public void handleOffline() {
                InfoDialog.newInstance(getString(R.string.error_offline_register_title),
                                       getString(R.string.error_offline_register_text))
                        .show(getSupportFragmentManager(), "");
            }
        };
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
        requestInProgress = false;
    }
}
