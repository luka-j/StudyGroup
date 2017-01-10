package rs.luka.android.studygroup.io.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.exceptions.NotLoggedInException;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.User;

/**
 * Created by luka on 1.1.16..
 */
public class Network {
    static final String API_VERSION = "v1/";

    /**
     * debug flag: set to false for using server on LAN
     */
    public static final boolean USE_REMOTE_SERVER = true;

    private static final int BUFFER_SIZE = 51_200; //50kb

    private static URL                  DOMAIN; //catch in static block complains if this is made final

    static {
        try {
            if(USE_REMOTE_SERVER)
                DOMAIN = new URL("http://api.notekeep.me/");
            else
                DOMAIN = new URL("http://192.168.0.15:9000/");
        } catch (MalformedURLException e) {
            DOMAIN = null; //wtf Java?
            e.printStackTrace();
        }
    }

    public static URL getDomain() {
        return DOMAIN;
    }

    public interface NetworkCallbacks<T> {
        void onRequestCompleted(int id, Response<T> response);
        void onExceptionThrown(int id, Throwable ex);
    }

    public static class Response<T> {
        public static final int RESPONSE_OK                 = 200;
        public static final int RESPONSE_CREATED            = 201;
        public static final int RESPONSE_ACCEPTED           = 202;
        public static final int NOT_MODIFIED                = 304;
        public static final int RESPONSE_BAD_REQUEST        = 400;
        public static final int RESPONSE_UNAUTHORIZED       = 401;
        public static final int RESPONSE_FORBIDDEN          = 403;
        public static final int RESPONSE_NOT_FOUND          = 404;
        public static final int RESPONSE_DUPLICATE          = 409;
        public static final int RESPONSE_GONE               = 410;
        public static final int RESPONSE_ENTITY_TOO_LARGE   = 413;
        public static final int RESPONSE_TOO_MANY_REQUESTS  = 429;
        public static final int RESPONSE_SERVER_ERROR       = 500;
        public static final int RESPONSE_BAD_GATEWAY        = 502;
        public static final int RESPONSE_SERVER_DOWN        = 503;
        public static final int RESPONSE_GATEWAY_TIMEOUT    = 504;
        public static final int RESPONSE_SERVER_UNREACHABLE = 521;
        public final  int     responseCode;
        public final  T       responseData;
        public final String   errorMessage;
        private final Request<T> request;

        private Response(Request<T> request, int responseCode, @Nullable T responseData, @Nullable String errorMessage) {
            this.request = request;
            this.responseCode = responseCode;
            this.responseData = responseData;
            this.errorMessage = errorMessage;
        }

        public static boolean isError(int code) {
            return code >=RESPONSE_BAD_REQUEST;
        }

        public boolean isError() {
            return isError(responseCode);
        }

        public String getDefaultErrorMessage(Context context) {
            switch(responseCode) {
                case RESPONSE_OK: return context.getString(R.string.error_default_ok);
                case RESPONSE_CREATED: return context.getString(R.string.error_default_created);
                case RESPONSE_ACCEPTED: return context.getString(R.string.error_default_accepted);
                case RESPONSE_BAD_REQUEST: return context.getString(R.string.error_bad_request_text);
                case RESPONSE_UNAUTHORIZED: return context.getString(R.string.error_default_unauthorized);
                case RESPONSE_FORBIDDEN: return context.getString(R.string.error_insufficient_permissions_text);
                case RESPONSE_NOT_FOUND: return context.getString(R.string.error_not_found_text);
                case RESPONSE_DUPLICATE: return context.getString(R.string.error_duplicate_text);
                case RESPONSE_GONE: return context.getString(R.string.error_default_gone);
                case RESPONSE_ENTITY_TOO_LARGE: return context.getString(R.string.error_default_entity_too_large);
                case RESPONSE_TOO_MANY_REQUESTS: return context.getString(R.string.error_default_too_many_requests);
                case RESPONSE_SERVER_ERROR: return context.getString(R.string.error_default_server_error);
                case RESPONSE_BAD_GATEWAY: return context.getString(R.string.error_default_bad_gateway);
                case RESPONSE_GATEWAY_TIMEOUT: return context.getString(R.string.error_default_gateway_timeout);
                case RESPONSE_SERVER_DOWN: return context.getString(R.string.error_default_server_down);
                case RESPONSE_SERVER_UNREACHABLE: return  context.getString(R.string.error_default_server_unreachable);
                default: return context.getString(R.string.error_default_unknown);
            }
        }

        public Response<T> handleErrorCode(NetworkExceptionHandler handler) {
            switch (responseCode) {
                case RESPONSE_UNAUTHORIZED:
                    if("Expired".equals(errorMessage)) {
                        try {
                            UserManager.handleTokenError(this, handler);
                            request.swapToken(User.getInstanceToken());
                            Response<T> handled = request.call();
                            handled.handleErrorCode(handler);
                            return handled;
                        } catch (NotLoggedInException ex) {
                            handler.handleUserNotLoggedIn();
                        } catch (IOException e) {
                            handler.handleIOException(e);
                        }
                    } else if("Invalid".equals(errorMessage)) {
                        handler.handleUserNotLoggedIn();
                    } else {
                        handler.handleUnauthorized(errorMessage);
                    }
                    break;
                case RESPONSE_FORBIDDEN:
                    handler.handleInsufficientPermissions(errorMessage);
                    return this;
                case RESPONSE_SERVER_ERROR:
                    handler.handleServerError(errorMessage);
                    return this;
                case RESPONSE_NOT_FOUND:
                    handler.handleNotFound(RESPONSE_NOT_FOUND);
                    Log.e("Network", "404: " + request.url.toString());
                    return this;
                case RESPONSE_GONE:
                    handler.handleNotFound(RESPONSE_GONE);
                    return this;
                case RESPONSE_ENTITY_TOO_LARGE:
                    handler.handleEntityTooLarge();
                    return this;
                case RESPONSE_DUPLICATE:
                    handler.handleDuplicate();
                    return this;
                case RESPONSE_BAD_REQUEST:
                    handler.handleBadRequest(errorMessage);
                    return this;
                case RESPONSE_TOO_MANY_REQUESTS:
                    handler.handleRateLimited(""); //todo read retry-after header
                    return this;
                case RESPONSE_SERVER_UNREACHABLE:
                    handler.handleUnreachable();
                    return this;
                case RESPONSE_BAD_GATEWAY:
                    handler.handleBadGateway();
                    return this;
                case RESPONSE_SERVER_DOWN:
                    if("Maintenance".equals(errorMessage)) handler.handleMaintenance(""); //todo read retry-after header
                    else handler.handleUnreachable();
                    return this;
                case RESPONSE_GATEWAY_TIMEOUT:
                    handler.handleGatewayTimeout();
                    return this;
                default:
                    handler.handleUnknownHttpCode(responseCode, responseData == null ? errorMessage : responseData.toString());
                    return this;
            }
            return this;
        }
    }


    protected static abstract class Request<T> implements Callable<Response<T>> {
        private int                 requestId;
        private URL                 url;
        private String              token;
        private String              httpVerb;
        private NetworkCallbacks<T> callback;

        private Request(int requestId, URL url, String token, String httpVerb,
                             NetworkCallbacks<T> callback) {
            this.requestId = requestId;
            if(!USE_REMOTE_SERVER) {
                try {
                    this.url = new URL(url.getProtocol(),
                                       url.getHost(),
                                       url.getPort(),
                                       "api" + url.getFile());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            else this.url = url;
            this.token = token;
            this.httpVerb = httpVerb;
            this.callback = callback;
        }

        @Override
        public Response<T> call() throws IOException {
            HttpURLConnection conn;
            conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setRequestMethod(httpVerb);
                if (token != null) {
                    conn.setRequestProperty("Authorization", token);
                }
                uploadData(conn);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (Response.isError(responseCode)) {
                    char[] errorMsg = new char[512];
                    Arrays.fill(errorMsg, '\0');
                    Reader errorReader = new InputStreamReader(conn.getErrorStream());
                    errorReader.read(errorMsg);
                    errorReader.close();
                    conn.disconnect();
                    int end = 0;
                    while (end < 256 && errorMsg[end] != '\0') end++;
                    final Response<T> response = new Response<>(this,
                                                                responseCode,
                                                                null, //no response body, error
                                                                String.valueOf(errorMsg).substring(0, end));
                    if (callback != null) {
                        callback.onRequestCompleted(requestId, response);
                    }
                    return response;
                } else {
                    String encoding = conn.getContentEncoding();
                    final Response<T> response = new Response<>(this,
                                                                responseCode,
                                                                getData(Utils.wrapStream(encoding, conn.getInputStream())),
                                                                null); //no error message, everything's ok
                    conn.disconnect();
                    if (callback != null) {
                        callback.onRequestCompleted(requestId, response);
                    }
                    return response;
                }
            } catch (final Throwable ex) {
                if (callback != null) {
                    callback.onExceptionThrown(requestId, ex);
                    return null;
                } else {
                    throw ex;
                }
            }
        }

        private void swapToken(String newToken) {
            this.token = newToken;
        }

        protected abstract T getData(InputStream stream) throws IOException;
        protected abstract void uploadData(URLConnection connection) throws IOException;
    }

    protected static class StringRequest extends Request<String> {

        private Map<String, String> data;

        public StringRequest(int requestId, URL url, String token, Map<String, String> data, String httpVerb,
                             NetworkCallbacks<String> callback) {
            super(requestId, url, token, httpVerb, callback);
            this.data = data;
        }

        @Override
        protected String getData(InputStream stream) throws IOException {
            BufferedReader reader      = new BufferedReader(new InputStreamReader(stream));
            String         responseMsg = reader.readLine();
            reader.close();
            return responseMsg;
        }

        @Override
        protected void uploadData(URLConnection conn) throws IOException {
            StringBuilder urlParams = new StringBuilder(data.size()*16);
            for (Map.Entry<String, String> param : data.entrySet()) {
                    urlParams.append(URLEncoder.encode(param.getKey(), "UTF-8")).append('=')
                            .append(URLEncoder.encode(param.getValue(), "UTF-8")).append('&');
            }
            if (urlParams.length() > 0) {
                conn.setDoOutput(true);
                urlParams.deleteCharAt(urlParams.length() - 1); //trailing &
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(urlParams.length()));
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(urlParams.toString());
                writer.close();
            }
        }
    }

    protected static class FileRequest extends Request<File> {

        private File saveTo;
        private File data;

        public FileRequest(int requestId, URL url, String token, File data, String httpVerb,
                           NetworkCallbacks<File> callback, File saveTo) {
            super(requestId, url, token, httpVerb, callback);
            this.saveTo = saveTo;
            this.data = data;
        }

        @Override
        protected File getData(InputStream stream) throws IOException {
            if(saveTo != null) {
                if(!saveTo.exists() && !saveTo.createNewFile()) throw new IOException("Cannot create new file");
                FileOutputStream out  = new FileOutputStream(saveTo);
                byte[]           buff = new byte[BUFFER_SIZE];
                int              readBytes;
                while ((readBytes = stream.read(buff)) != -1) {
                    out.write(buff, 0, readBytes);
                }
                out.close();
                stream.close();
            }
            return saveTo;
        }

        @Override
        protected void uploadData(URLConnection connection) throws IOException {
            if(data != null) {
                connection.setRequestProperty("Content-Type", "application/octet-stream");
                byte[] buff = new byte[BUFFER_SIZE];
                int readBytes;
                OutputStream out = connection.getOutputStream();
                FileInputStream in = new FileInputStream(data);
                while((readBytes = in.read(buff)) != -1) {
                    out.write(buff, 0, readBytes);
                }
                out.close(); in.close();
            }
        }
    }

    public static class Status {
        private static boolean online = true;
        public static boolean isOnline() {return online;}
        public static void setOnline() {online=true;}
        public static void setOffline() {online=false;}
        public static boolean checkNetworkStatus(Context context) {
            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }
}