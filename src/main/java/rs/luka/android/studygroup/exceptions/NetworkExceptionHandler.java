package rs.luka.android.studygroup.exceptions;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.dialogs.ErrorDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.LoginActivity;

/**
 * Created by luka on 3.1.16..
 */
public interface NetworkExceptionHandler {


    void handleUserNotLoggedIn();
    void handleInsufficientPermissions();
    void handleServerError();
    void handleNotFound(int code);
    void handleDuplicate();
    void handleBadRequest();
    void handleJsonException();
    void handleIOException(IOException ex);
    void finished();



    class DefaultHandler implements NetworkExceptionHandler {
        protected boolean hasErrors = false;
        private AppCompatActivity hostActivity;
        public DefaultHandler(AppCompatActivity host) {
            hostActivity = host;
        }

        @Override
        public void handleUserNotLoggedIn() {
            User.clearToken();
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog.newInstance(hostActivity.getString(R.string.error_session_expired_title),
                                            hostActivity.getString(R.string.error_session_expired_text))
                               .show(hostActivity.getSupportFragmentManager(), "studygroup.dialog.error");
                    hostActivity.startActivity(new Intent(hostActivity, LoginActivity.class));
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleInsufficientPermissions() {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog.newInstance(hostActivity.getString(R.string.error_insufficient_permissions_title),
                                            hostActivity.getString(R.string.error_insufficient_permissions_text))
                               .show(hostActivity.getSupportFragmentManager(), "studygroup.dialog.error");
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleServerError() {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog.newInstance(hostActivity.getString(R.string.error_server_error_title),
                                            hostActivity.getString(R.string.error_server_error_text))
                               .show(hostActivity.getSupportFragmentManager(), "studygroup.dialog.error");
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleNotFound(final int code) {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog.newInstance(String.valueOf(code) + " " +hostActivity.getString(R.string.error_not_found_title),
                                            hostActivity.getString(R.string.error_not_found_text))
                               .show(hostActivity.getSupportFragmentManager(), "studygroup.dialog.error");
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleDuplicate() {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog.newInstance(hostActivity.getString(R.string.error_duplicate_title),
                                            hostActivity.getString(R.string.error_duplicate_text))
                               .show(hostActivity.getSupportFragmentManager(), "studygroup.dialog.error");
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleBadRequest() {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog.newInstance(hostActivity.getString(R.string.error_bad_request_title),
                                            hostActivity.getString(R.string.error_bad_request_text))
                               .show(hostActivity.getSupportFragmentManager(), "studygroup.dialog.error");
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleJsonException() {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog.newInstance(hostActivity.getString(R.string.error_json_title),
                                            hostActivity.getString(R.string.error_json_text))
                               .show(hostActivity.getSupportFragmentManager(), "studygroup.dialog.error");
                }
            });
            hasErrors=true;
        }

        @Override
        public void finished() {
            if(!hasErrors)
                hostActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finishedSuccessfully();
                    }
                });
            hasErrors=false;
        }

        public void finishedSuccessfully() {}

        @Override
        public void handleIOException(IOException ex) {
            ex.printStackTrace(); //todo
        }
    }
}
