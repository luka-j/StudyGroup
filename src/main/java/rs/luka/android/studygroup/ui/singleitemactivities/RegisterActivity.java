package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.Limits;
import rs.luka.android.studygroup.io.Network;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.network.UserManager;
import rs.luka.android.studygroup.ui.dialogs.ErrorDialog;
import rs.luka.android.studygroup.ui.recyclers.RootActivity;

/**
 * Created by luka on 30.12.15..
 */
public class RegisterActivity extends AppCompatActivity implements Network.NetworkCallback, ErrorDialog.Callbacks {
    private static final String TAG_DIALOG_UNEXPECTED_ERROR = "studygroup.dialog.unexpectederror";
    private static final int REQUEST_REGISTER = 0;
    private static final int REQUEST_LOGIN = 1;

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
        usernameTil = (TextInputLayout) findViewById(R.id.register_email_til);
        passwordTil = (TextInputLayout) findViewById(R.id.register_password_til);
        register = (CardView) findViewById(R.id.button_register);
        email = (EditText) findViewById(R.id.register_email_input);
        username = (EditText) findViewById(R.id.register_username_input);
        password = (EditText) findViewById(R.id.register_password_input);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!requestInProgress)
                    register();
            }
        });
    }

    private void register() {
        requestInProgress = true;
        if( email.getText().length() > Limits.USER_EMAIL_MAX_LENGTH) {
            emailTil.setError(getString(R.string.error_too_long));
        } else emailTil.setError(null);
        if(username.getText().length() > Limits.USER_NAME_MAX_LENGTH) {
            usernameTil.setError(getString(R.string.error_too_long));
        } else usernameTil.setError(null);
        if(password.getText().length() > Limits.USER_PASSWORD_MAX_LENGTH) {
            passwordTil.setError(getString(R.string.error_too_long));
        } else passwordTil.setError(null);
        UserManager.register(REQUEST_REGISTER,
                             email.getText().toString(),
                             username.getText().toString(),
                             password.getText().toString(),
                             this);
    }

    @Override
    public void onRequestCompleted(final int id, final Network.Response response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                emailTil.setError(null);
                if (id == REQUEST_REGISTER) {
                    switch (response.responseCode) {
                        case Network.Response.RESPONSE_CREATED:
                            User.instantiateUser(response.responseMessage,
                                                 RegisterActivity.this.getSharedPreferences(User.PREFS_NAME, MODE_PRIVATE));
                            startActivity(new Intent(RegisterActivity.this, RootActivity.class));
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
                            ErrorDialog.newInstance(getString(R.string.unexpected_server_error),
                                                    response.getDefaultErrorMessage(RegisterActivity.this))
                                       .show(RegisterActivity.this.getFragmentManager(), TAG_DIALOG_UNEXPECTED_ERROR);
                    }
                } else if (id == REQUEST_LOGIN) {
                    if(response.responseCode == Network.Response.RESPONSE_OK) {
                        User.instantiateUser(response.responseMessage,
                                             RegisterActivity.this.getSharedPreferences(User.PREFS_NAME, MODE_PRIVATE));
                        startActivity(new Intent(RegisterActivity.this, RootActivity.class).addFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    } else {
                        ErrorDialog.newInstance(getString(R.string.unexpected_server_error),
                                                response.getDefaultErrorMessage(RegisterActivity.this))
                                   .show(RegisterActivity.this.getFragmentManager(), TAG_DIALOG_UNEXPECTED_ERROR);
                    }
                }
            }
        });
        requestInProgress = false; //todo what if chained register->login request (requestInProgress stays false)
    }

    @Override
    public void onExceptionThrown(int id, Exception ex) {
        //todo generic exception handling
        ex.printStackTrace();
        requestInProgress = false;
    }

    @Override
    public void onErrorDialogClosed() {
    }
}
