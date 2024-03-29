package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.net.SocketException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.io.network.UserManager;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;

/**
 * Created by luka on 25.7.15..
 */
public class LoginActivity extends AppCompatActivity implements Network.NetworkCallbacks<String>, InfoDialog.Callbacks {
    public static final int REQUEST_LOGIN = 0;
    public static final int REQUEST_REFRESH = 1;

    private static final String TAG_DIALOG_ERROR   = "studygroup.dialog.errordialog";
    private static final String TAG_DIALOG_NO_NETWORK = "studygroup.LoginActivity.errorNetwork";
    private boolean requestInProgress              = false;

    private CardView login;
    private TextView register;
    private TextInputLayout emailTil;
    private TextInputLayout passwordTil;
    private EditText email;
    private EditText password;
    private CircularProgressView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences userPrefs = getSharedPreferences(User.PREFS_NAME, MODE_PRIVATE);
        if(User.hasSavedToken()) {
            UserManager.refreshToken(REQUEST_REFRESH,
                                     userPrefs.getString(User.PREFS_KEY_TOKEN, null),
                                     this);
        }

        login = (CardView) findViewById(R.id.button_login);
        register = (TextView) findViewById(R.id.button_register);
        emailTil = (TextInputLayout) findViewById(R.id.login_email_til);
        email = (EditText) findViewById(R.id.login_email_input);
        passwordTil = (TextInputLayout) findViewById(R.id.login_password_til);
        password = (EditText) findViewById(R.id.login_password_input);
        progressView = (CircularProgressView) findViewById(R.id.login_cpv);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!requestInProgress)
                    login();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void login() {
        requestInProgress = true;
        UserManager.login(REQUEST_LOGIN,
                          email.getText().toString(),
                          password.getText().toString(),
                          this);
        login.setVisibility(View.GONE);
        register.setVisibility(View.GONE);
        forceShow(progressView);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void onInfoDialogClosed(InfoDialog dialog) {
        if(dialog.getTag().equals(TAG_DIALOG_ERROR))
            password.setText("");
        else if(dialog.getTag().equals(TAG_DIALOG_NO_NETWORK))
            if(User.hasSavedToken())
                startActivity(new Intent(LoginActivity.this, LoadingActivity.class));
    }

    @Override
    public void onRequestCompleted(final int id, final Network.Response<String> response) {
        runOnUiThread(() -> {
            if(id == REQUEST_LOGIN) {
                switch (response.responseCode) {
                    case Network.Response.RESPONSE_OK:
                        User.instantiateUser(response.responseData, LoginActivity.this);
                        startActivity(new Intent(LoginActivity.this, LoadingActivity.class));
                        break;
                    case Network.Response.RESPONSE_UNAUTHORIZED:
                        InfoDialog.newInstance(getString(R.string.wrong_creds_title),
                                               getString(R.string.wrong_creds))
                                  .registerCallbacks(LoginActivity.this)
                                  .show(getFragmentManager(), TAG_DIALOG_ERROR);
                        reshowButtons();
                        break;
                    default:
                        InfoDialog.newInstance(getString(R.string.unexpected_server_error),
                                               response.getDefaultErrorMessage(LoginActivity.this))
                                  .registerCallbacks(LoginActivity.this)
                                  .show(getFragmentManager(), TAG_DIALOG_ERROR);
                        reshowButtons();
                }
            } else if(id == REQUEST_REFRESH) {
                switch (response.responseCode) {
                    case Network.Response.RESPONSE_OK:
                        User.instantiateUser(response.responseData, LoginActivity.this);
                        startActivity(new Intent(LoginActivity.this, LoadingActivity.class));
                        break;
                    case Network.Response.RESPONSE_UNAUTHORIZED: break; //proceed to login
                    default: ; // todo ?
                }
            }
        });
        requestInProgress = false;
    }

    private void forceShow(View v) {
        v.setVisibility(View.VISIBLE);
        v.bringToFront();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            v.getParent().requestLayout();
            if(v.getParent() instanceof View)
                ((View)v.getParent()).invalidate();
        }
    }
    private void reshowButtons() {
        runOnUiThread(() -> {
            progressView.setVisibility(View.GONE);
            forceShow(login);
            forceShow(register);
        });
    }

    @Override
    public void onExceptionThrown(int id, final Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        runOnUiThread(() -> {
            if(ex instanceof SocketException && !Network.Status.checkNetworkStatus(LoginActivity.this)) {
                InfoDialog.newInstance(getString(R.string.error_offline_title),
                                       getString(R.string.error_offline_text))
                          .registerCallbacks(LoginActivity.this)
                          .show(getFragmentManager(), TAG_DIALOG_NO_NETWORK);
            } else if (ex instanceof SocketException) {
                InfoDialog.newInstance(getString(R.string.error_login_socketex_title),
                                       getString(R.string.error_login_socketex_text))
                        .registerCallbacks(LoginActivity.this)
                        .show(getFragmentManager(), TAG_DIALOG_NO_NETWORK);
            } else {
                InfoDialog.newInstance(getString(R.string.error_login_unknownex_title),
                                       getString(R.string.error_login_unknownex_text))
                        .registerCallbacks(LoginActivity.this)
                        .show(getFragmentManager(), TAG_DIALOG_NO_NETWORK);
            }
            User.setOfflineUser(getSharedPreferences(User.PREFS_NAME, MODE_PRIVATE));
            ex.printStackTrace();
            requestInProgress = false;
        });
    }
}
