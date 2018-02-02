package rs.luka.android.studygroup.exceptions;

import android.content.Intent;
import android.support.annotation.StringRes;
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
 * Handles possible network errors
 * Created by luka on 3.1.16..
 */
public interface NetworkExceptionHandler {


    void handleUserNotLoggedIn();
    void handleInsufficientPermissions(String message);
    void handleServerError(String message);
    void handleNotFound(int code);
    void handleDuplicate();
    void handleBadRequest(String message);
    void handleJsonException();
    void handleMaintenance(String until);
    void handleUnreachable();
    void handleIOException(IOException ex);
    void handleUnauthorized(String errorMessage);
    void handleRateLimited(String retryAfter);
    void handleBadGateway();
    void handleGatewayTimeout();
    void handleEntityTooLarge();
    void handleUnknownHttpCode(int responseCode, String message);
    void finished();


    /**
     * Reference implementation, methods can be overrided as necessary.
     * Uses InfoDialog to display error messages on the hostActivity
     * Provides finishedSuccessfully and finishedUnsucessfully methods, as well
     * as pinning down the IOException cause (file/socket/unknown) 
     */
    class DefaultHandler implements NetworkExceptionHandler {
        private static final String LOGGING_TAG = "net.defaulthandler";
        protected static final String TAG_DIALOG = "studygroup.dialog.error";
        protected boolean hasErrors = false;
        protected AppCompatActivity hostActivity;

        public DefaultHandler(AppCompatActivity host) {
            hostActivity = host;
        }

        private void showErrorDialog(final String title, final String message) {
            hostActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog dialog = InfoDialog.newInstance(title, message);
                    if(hostActivity instanceof InfoDialog.Callbacks)
                        dialog.registerCallbacks((InfoDialog.Callbacks)hostActivity);
                    dialog.show(hostActivity.getSupportFragmentManager(), TAG_DIALOG);
                }
            });
        }
        private void showErrorDialog(final @StringRes int title, final @StringRes int message) {
            showErrorDialog(hostActivity.getString(title), hostActivity.getString(message));
        }

        @Override
        public void handleUserNotLoggedIn() {
            User.clearToken();
            showErrorDialog(R.string.error_session_expired_title, R.string.error_session_expired_text);
            hostActivity.startActivity(new Intent(hostActivity, LoginActivity.class));
            hasErrors=true;
        }

        @Override
        public void handleInsufficientPermissions(String message) {
            showErrorDialog(R.string.error_insufficient_permissions_title, R.string.error_insufficient_permissions_text);
            hasErrors=true;
        }

        @Override
        public void handleServerError(String message) {
            showErrorDialog(R.string.error_server_error_title, R.string.error_server_error_text);
            hasErrors=true;
        }

        @Override
        public void handleNotFound(final int code) {
            showErrorDialog(String.valueOf(code) + " " + hostActivity.getString(R.string.error_not_found_title),
                            hostActivity.getString(R.string.error_not_found_text));
            hasErrors=true;
        }

        @Override
        public void handleDuplicate() {
            showErrorDialog(R.string.error_duplicate_title, R.string.error_duplicate_text);
            hasErrors=true;
        }

        @Override
        public void handleBadRequest(String message) {
            showErrorDialog(R.string.error_bad_request_title, R.string.error_bad_request_text);
            hasErrors=true;
        }

        @Override
        public void handleJsonException() {
            showErrorDialog(R.string.error_json_title, R.string.error_json_text);
            hasErrors=true;
            finishedUnsuccessfully();
        }

        @Override
        public void handleMaintenance(String until) {
            showErrorDialog(R.string.error_maintenance_title, R.string.error_maintenance_text);
            hasErrors=true;
        }

        @Override
        public void handleUnreachable() {
            showErrorDialog(R.string.error_unreachable_title, R.string.error_unreachable_text);
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
            else
                hostActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finishedUnsuccessfully();
                    }
                });
            hasErrors=false;
        }

        @Override
        public void handleUnauthorized(String errorMessage) {
            showErrorDialog(R.string.error_unauthorized_title, R.string.error_unauthorized_text);
            hasErrors = true;
        }

        @Override
        public void handleUnknownHttpCode(int responseCode, String message) {
            showErrorDialog(hostActivity.getString(R.string.error_unknown_http_code_title),
                            hostActivity.getString(R.string.error_unknown_http_code_text, responseCode + ": " + message));
            if(responseCode >= 400) hasErrors = true;
        }

        @Override
        public void handleRateLimited(String retryAfter) {
            showErrorDialog(R.string.error_too_many_requests_title, R.string.error_too_many_requests_text);
            hasErrors = true;
        }

        @Override
        public void handleBadGateway() {
            showErrorDialog(R.string.error_bad_gateway_title, R.string.error_bad_gateway_text);
            hasErrors = true;
        }

        @Override
        public void handleGatewayTimeout() {
            showErrorDialog(R.string.error_gateway_timeout_title, R.string.error_gateway_timeout_text);
            hasErrors = true;
        }

        @Override
        public void handleEntityTooLarge() {
            showErrorDialog(R.string.error_entity_too_large_title, R.string.error_entity_too_large_text);
            hasErrors = true;
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
            showErrorDialog(R.string.error_fileex_title, R.string.error_fileex_text);
            Log.e(LOGGING_TAG, "Unexpected FileIOException", ex);
        }

        public void handleOffline() {
            if(Network.Status.isOnline()) {
                showErrorDialog(R.string.error_changed_offline_title, R.string.error_changed_offline_text);
                Network.Status.setOffline();
            }
        }

        public void handleSocketException(SocketException ex) {
            if(Network.Status.isOnline()) { //prevents this dialog from popping up multiple times. Should it?
                showErrorDialog(R.string.error_socketex_title, R.string.error_socketex_text);
                Log.e(LOGGING_TAG, "Unexpected SocketException", ex);
                Network.Status.setOffline(); //todo ?
            }
        }

        public void handleUnknownIOException(IOException ex) {
            showErrorDialog(R.string.error_unknown_ioex_title, R.string.error_unknown_ioex_text);
            Log.e(LOGGING_TAG, "Unexpected unknown IOException", ex);
        }
    }
}
