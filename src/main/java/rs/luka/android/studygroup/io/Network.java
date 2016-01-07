package rs.luka.android.studygroup.io;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.exceptions.NotLoggedInException;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.network.UserManager;

/**
 * Created by luka on 1.1.16..
 */
public class Network {
    public static final Map<String, String> emptyMap = Collections.unmodifiableMap(new HashMap<String, String>(0));

    private static URL                  DOMAIN; //catch in static block complains if this is made final
    public static final String          NOTES     = "notes/";
    public static final String          QUESTIONS = "questions/";
    public static final String          EXAMS     = "exams/";

    static {
        try {
            DOMAIN = new URL("http://192.168.0.15:9000/");
        } catch (MalformedURLException e) {
            DOMAIN = null; //wtf Java?
            e.printStackTrace();
        }
    }

    private static final   ExecutorService executor  = Executors.newCachedThreadPool();

    public static URL getDomain() {
        return DOMAIN;
    }

    public interface NetworkCallbacks {
        void onRequestCompleted(int id, Response response);
        void onExceptionThrown(int id, Throwable ex);
    }

    public static class Response {
        public static final int RESPONSE_OK           = 200;
        public static final int RESPONSE_CREATED      = 201;
        public static final int RESPONSE_BAD_REQUEST  = 400;
        public static final int RESPONSE_UNAUTHORIZED = 401;
        public static final int RESPONSE_FORBIDDEN    = 403;
        public static final int RESPONSE_NOT_FOUND    = 404;
        public static final int RESPONSE_DUPLICATE    = 409;
        public static final int RESPONSE_GONE         = 410;
        public static final int RESPONSE_SERVER_ERROR = 500;

        private final Request request;
        public final int responseCode;
        public final String responseMessage;

        public Response(Request request, int responseCode, String responseMessage) {
            this.request = request;
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
        }
        public boolean isError() {
            return isError(responseCode);
        }
        public static boolean isError(int code) {
            return code >=RESPONSE_BAD_REQUEST;
        }

        public String getDefaultErrorMessage(Context context) {
            switch(responseCode) {
                case RESPONSE_OK: return context.getString(R.string.error_default_ok);
                case RESPONSE_CREATED: return context.getString(R.string.error_default_created);
                case RESPONSE_BAD_REQUEST: return context.getString(R.string.error_bad_request_text);
                case RESPONSE_UNAUTHORIZED: return context.getString(R.string.error_default_unauthorized);
                case RESPONSE_FORBIDDEN: return context.getString(R.string.error_insufficient_permissions_text);
                case RESPONSE_NOT_FOUND: return context.getString(R.string.error_not_found_text);
                case RESPONSE_DUPLICATE: return context.getString(R.string.error_duplicate_text);
                case RESPONSE_GONE: return context.getString(R.string.error_default_gone);
                case RESPONSE_SERVER_ERROR: return context.getString(R.string.error_default_server_error);
                default: return context.getString(R.string.error_default_unknown);
            }
        }

        public Response handleException(NetworkExceptionHandler handler) {
            switch (responseCode) {
                case RESPONSE_UNAUTHORIZED:
                    try {
                        UserManager.handleTokenError(this);
                        request.changeToken(User.getToken());
                        Response handled = request.call();
                        handled.handleException(handler);
                        return handled;
                    } catch (NotLoggedInException ex) {
                        handler.handleUserNotLoggedIn();
                    } catch (IOException e) {
                        handler.handleIOException(e);
                    }
                    break;
                case RESPONSE_FORBIDDEN:
                    handler.handleInsufficientPermissions();
                    return this;
                case RESPONSE_SERVER_ERROR:
                    handler.handleServerError();
                    return this;
                case RESPONSE_NOT_FOUND:
                    handler.handleNotFound(RESPONSE_NOT_FOUND);
                    return this;
                case RESPONSE_GONE:
                    handler.handleNotFound(RESPONSE_GONE);
                    return this;
                case RESPONSE_DUPLICATE:
                    handler.handleDuplicate();
                    return this;
                case RESPONSE_BAD_REQUEST:
                    handler.handleBadRequest();
                    return this;
            }
            return this;
        }
    }

    private static class Request implements Callable<Response> {
        private static final String VERB_POST = "POST";
        private static final String VERB_GET = "GET";
        private static final String VERB_PUT = "PUT";
        private static final String VERB_DELETE = "DELETE";

        private int                 requestId;
        private URL                 url;
        private String              token;
        private Map<String, String> data;
        private String              httpVerb;
        private NetworkCallbacks    callback;

        private Request(int requestId, URL url, String token, Map<String, String> data, String httpVerb,
                        NetworkCallbacks callback) {
            this.requestId = requestId;
            this.url = url;
            this.token = token;
            this.data = data;
            this.httpVerb = httpVerb;
            this.callback = callback;
        }

        @Override
        public Response call() throws IOException {
            try {
                //System.out.println("Making request to " + httpVerb + " " + url.toString());
                HttpURLConnection conn;
                conn = (HttpURLConnection) url.openConnection();
                StringBuilder urlParams = new StringBuilder();
                for (Map.Entry<String, String> param : data.entrySet()) {
                    urlParams.append(URLEncoder.encode(param.getKey(), "UTF-8")).append('=')
                             .append(URLEncoder.encode(param.getValue(), "UTF-8")).append('&');
                }

                conn.setRequestMethod(httpVerb);
                if(token != null) {
                    conn.setRequestProperty("Authorization", token);
                }
                //conn.setRequestProperty( "Accept-Encoding", "" );
                if (urlParams.length() > 0) {
                    conn.setDoOutput(true);
                    urlParams.deleteCharAt(urlParams.length() - 1); //trailing &
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(urlParams.length()));
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(urlParams.toString());
                    writer.close();
                }
                conn.connect();

                int resp = conn.getResponseCode();
                if (Response.isError(resp)) {
                    char[] errorMsg = new char[256];
                    Arrays.fill(errorMsg, '\0');
                    Reader     errorReader = new InputStreamReader(conn.getErrorStream());
                    errorReader.read(errorMsg);
                    errorReader.close();
                    conn.disconnect();
                    int end = 0;
                    while(end < 256 && errorMsg[end]!='\0') end++;
                    Response response = new Response(this, resp, String.valueOf(errorMsg).substring(0, end));
                    if (callback != null) {
                        callback.onRequestCompleted(requestId, response);
                    }
                    return response;
                } else {
                    BufferedReader reader      = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String         responseMsg = reader.readLine();
                    reader.close();
                    Response response = new Response(this, resp, responseMsg);
                    conn.disconnect();
                    if (callback != null)
                        callback.onRequestCompleted(requestId, response);
                    return response;
                }
            } catch (Throwable ex) {
                if (callback != null) {
                    callback.onExceptionThrown(requestId, ex);
                    return null;
                } else {
                    throw ex;
                }
            }
        }

        private void changeToken(String newToken) {
            String path = url.getPath().split("/", 3)[2];
            try {
                url = new URL(DOMAIN, newToken + "/" + path);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void requestDataAsync(int requestId, URL url, Map<String, String> data, NetworkCallbacks callback, String verb) {
        executor.submit(new Request(requestId, url, User.getToken(), data, verb, callback));
    }
    private static Response requestDataBlocking (int requestId, URL url, Map<String, String> data, long timeout,
                                             TimeUnit unit, String verb)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        Future<Response> task = executor.submit(new Request(requestId, url, User.getToken(), data, verb, null));
        try {
            return task.get(timeout, unit);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return null;
        } catch (ExecutionException ex) {
            if(ex.getCause() instanceof FileNotFoundException)
                throw (FileNotFoundException)ex.getCause();
            else if(ex.getCause() instanceof IOException)
                throw (IOException)ex.getCause();
            else
                throw ex;
        }
    }

    public static void postDataAsync(int requestId, URL url, Map<String, String> data, NetworkCallbacks callback) {
        requestDataAsync(requestId, url, data, callback, Request.VERB_POST);
    }

    public static Response postDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, Request.VERB_POST);
    }

    public static Response requestPostData(URL url, Map<String, String> param) throws IOException {
        return new Request(0, url, User.getToken(), param, Request.VERB_POST, null).call();
    }

    public static void getDataAsync(int requestId, URL url, NetworkCallbacks callback) {
        requestDataAsync(requestId, url, emptyMap, callback, Request.VERB_GET);
    }

    public static Response getDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, Request.VERB_GET);
    }

    public static Response requestGetData(URL url) throws IOException {
        return new Request(0, url, User.getToken(), emptyMap, Request.VERB_GET, null).call();
    }

    public static void putDataAsync(int requestId, URL url, Map<String, String> data, NetworkCallbacks callback) {
        requestDataAsync(requestId, url, data, callback, Request.VERB_PUT);
    }

    public static Response putDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, Request.VERB_PUT);
    }

    public static Response requestPutData(URL url, Map<String, String> param) throws IOException {
        return new Request(0, url, User.getToken(), param, Request.VERB_PUT, null).call();
    }

    public static void deleteDataAsync(int requestId, URL url, Map<String, String> data, NetworkCallbacks callback) {
        requestDataAsync(requestId, url, data, callback, Request.VERB_DELETE);
    }

    public static Response deleteDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, Request.VERB_DELETE);
    }

}
