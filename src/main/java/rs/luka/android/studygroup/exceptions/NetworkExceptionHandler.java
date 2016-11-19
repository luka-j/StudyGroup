package rs.luka.android.studygroup.exceptions;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.net.SocketException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
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
        private static final String LOGGING_TAG = "net.defaulthandler";
        protected static final String TAG_DIALOG = "studygroup.dialog.error";
        protected boolean hasErrors = false;
        protected AppCompatActivity hostActivity;

        public DefaultHandler(AppCompatActivity host) {
            hostActivity = host;
        }

        @Override
        public void handleUserNotLoggedIn() {
            User.clearToken();
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_session_expired_title),
                                           hostActivity.getString(R.string.error_session_expired_text));
                    if(hostActivity instanceof InfoDialog.Callbacks)
                        dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                    dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
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
                    InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_insufficient_permissions_title),
                                           hostActivity.getString(R.string.error_insufficient_permissions_text));
                    if(hostActivity instanceof InfoDialog.Callbacks)
                        dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                    dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleServerError() {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_server_error_title),
                                           hostActivity.getString(R.string.error_server_error_text));
                    if(hostActivity instanceof InfoDialog.Callbacks)
                        dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                    dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleNotFound(final int code) {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog dialog = InfoDialog.newInstance(String.valueOf(code) + " " + hostActivity.getString(R.string.error_not_found_title),
                                           hostActivity.getString(R.string.error_not_found_text));
                    if(hostActivity instanceof InfoDialog.Callbacks)
                        dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                    dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleDuplicate() {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_duplicate_title),
                                                       hostActivity.getString(R.string.error_duplicate_text));
                    if(hostActivity instanceof InfoDialog.Callbacks)
                        dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                    dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleBadRequest() {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_bad_request_title),
                                           hostActivity.getString(R.string.error_bad_request_text));
                    if(hostActivity instanceof InfoDialog.Callbacks)
                        dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                    dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                }
            });
            hasErrors=true;
        }

        @Override
        public void handleJsonException() {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_json_title),
                                           hostActivity.getString(R.string.error_json_text));
                    if(hostActivity instanceof InfoDialog.Callbacks)
                        dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                    dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                }
            });
            hasErrors=true;
            finishedUnsuccessfully();
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
            else
                hostActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finishedUnsuccessfully();
                    }
                });
            hasErrors=false;
        }

        public void finishedSuccessfully() {
            if(!Network.Status.isOnline()) {
                Network.Status.setOnline();
            }
        }

        public void finishedUnsuccessfully() {
            ;
        }

        @Override
        public void handleIOException(final IOException ex) {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(ex instanceof SocketException) {
                        if(Network.Status.checkNetworkStatus(hostActivity))
                            handleSocketException((SocketException)ex);
                        else
                            handleOffline();
                    } else if(ex instanceof FileIOException) {
                        handleFileException((FileIOException)ex);
                    } else {
                        handleUnknownIOException(ex);
                    }
                }
            });
            hasErrors = true;
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finishedUnsuccessfully();
                }
            });
        }

        public void handleFileException(FileIOException ex) {
            InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_fileex_title),
                                   hostActivity.getString(R.string.error_fileex_text));
            if(hostActivity instanceof InfoDialog.Callbacks)
                dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
            dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
            Log.e(LOGGING_TAG, "Unexpected FileIOException", ex);
        }

        public void handleOffline() {
            if(Network.Status.isOnline()) {
                InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_changed_offline_title),
                                       hostActivity.getString(R.string.error_changed_offline_text));
                if(hostActivity instanceof InfoDialog.Callbacks)
                    dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                Network.Status.setOffline();
            }
        }

        public void handleSocketException(SocketException ex) {
            if(Network.Status.isOnline()) { //prevents this dialog from popping up multiple times. Should it?
                InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_socketex_title),
                                       hostActivity.getString(R.string.error_socketex_text));
                if(hostActivity instanceof InfoDialog.Callbacks)
                    dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                Log.e(LOGGING_TAG, "Unexpected SocketException", ex);
                Network.Status.setOffline(); //todo ?
            }
        }

        public void handleUnknownIOException(IOException ex) {
            InfoDialog dialog = InfoDialog.newInstance(hostActivity.getString(R.string.error_unknown_ioex_title),
                                   hostActivity.getString(R.string.error_unknown_ioex_text));
            if(hostActivity instanceof InfoDialog.Callbacks)
                dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
            dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
            Log.e(LOGGING_TAG, "Unexpected unknown IOException", ex);
        }
    }
}
