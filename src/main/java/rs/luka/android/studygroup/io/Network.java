package rs.luka.android.studygroup.io;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.CharBuffer;
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

/**
 * Created by luka on 1.1.16..
 */
public class Network {
    public static final Map<String, String> emptyMap = Collections.unmodifiableMap(new HashMap<String, String>(0));

    public static final String          DOMAIN    = "http://192.168.0.15:9000/";
    public static final String          GROUPS    = "groups/";
    public static final String          COURSES   = "courses/";
    public static final String          NOTES     = "notes/";
    public static final String          QUESTIONS = "questions/";
    public static final String          EXAMS     = "exams/";

    private static final   ExecutorService executor  = Executors.newCachedThreadPool();

    public interface NetworkCallback {
        void onRequestCompleted(int id, Response response);
        void onExceptionThrown(int id, Exception ex);
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

        public final int responseCode;
        public final String responseMessage;

        public Response(int responseCode, String responseMessage) {
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
                case RESPONSE_BAD_REQUEST: return context.getString(R.string.error_default_bad_request);
                case RESPONSE_UNAUTHORIZED: return context.getString(R.string.error_default_unauthorized);
                case RESPONSE_FORBIDDEN: return context.getString(R.string.error_default_forbidden);
                case RESPONSE_NOT_FOUND: return context.getString(R.string.error_default_not_found);
                case RESPONSE_DUPLICATE: return context.getString(R.string.error_default_conflict);
                case RESPONSE_GONE: return context.getString(R.string.error_default_gone);
                case RESPONSE_SERVER_ERROR: return context.getString(R.string.error_default_server_error);
                default: return context.getString(R.string.error_default_unknown);
            }
        }
    }

    private static class Request implements Callable<Response> {
        private static final String VERB_POST = "POST";
        private static final String VERB_GET = "GET";
        private static final String VERB_PUT = "PUT";
        private static final String VERB_DELETE = "DELETE";

        private int                 requestId;
        private URL                 url;
        private Map<String, String> data;
        private String              httpVerb;
        private NetworkCallback     callback;

        private Request(int requestId, URL url, Map<String, String> data, String httpVerb, NetworkCallback callback) {
            this.requestId = requestId;
            this.url = url;
            this.data = data;
            this.httpVerb = httpVerb;
            this.callback = callback;
        }

        @Override
        public Response call() throws Exception {
            try {
                HttpURLConnection conn;
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                StringBuilder urlParams = new StringBuilder();
                for (Map.Entry<String, String> param : data.entrySet()) {
                    urlParams.append(URLEncoder.encode(param.getKey(), "UTF-8")).append('=')
                             .append(URLEncoder.encode(param.getValue(), "UTF-8")).append('&');
                }
                urlParams.deleteCharAt(urlParams.length() - 1); //trailing &
                conn.setRequestMethod(httpVerb);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(urlParams.length()));
                conn.connect();
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(urlParams.toString());
                writer.flush();
                writer.close();

                int resp = conn.getResponseCode();
                if (Response.isError(resp)) {
                    CharBuffer errorMsg    = CharBuffer.allocate(128);
                    Reader     errorReader = new InputStreamReader(conn.getErrorStream());
                    errorReader.read(errorMsg);
                    errorReader.close();
                    conn.disconnect();
                    Response response = new Response(resp, errorMsg.toString());
                    if (callback != null) {
                        callback.onRequestCompleted(requestId, response);
                    }
                    return response;
                } else {
                    Object content = conn.getContent();
                    conn.disconnect();
                    Response response = new Response(resp, content.toString());
                    if (callback != null)
                        callback.onRequestCompleted(requestId, response);
                    return response;
                }
            } catch (Exception ex) {
                if(callback != null) {
                    callback.onExceptionThrown(requestId, ex);
                    return null;
                } else {
                    throw ex;
                }
            }
        }
    }

    private static void requestDataAsync(int requestId, URL url, Map<String, String> data, NetworkCallback callback, String verb) {
        executor.submit(new Request(requestId, url, data, verb, callback));
    }
    private static Response requestDataBlocking (int requestId, URL url, Map<String, String> data, long timeout,
                                             TimeUnit unit, String verb)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        Future<Response> task = executor.submit(new Request(requestId, url, data, verb, null));
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

    public static void postDataAsync(int requestId, URL url, Map<String, String> data, NetworkCallback callback) {
        requestDataAsync(requestId, url, data, callback, Request.VERB_POST);
    }

    public static Response postDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, Request.VERB_POST);
    }

    public static void getDataAsync(int requestId, URL url, Map<String, String> data, NetworkCallback callback) {
        requestDataAsync(requestId, url, data, callback, Request.VERB_GET);
    }

    public static Response getDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, Request.VERB_GET);
    }

    public static void putDataAsync(int requestId, URL url, Map<String, String> data, NetworkCallback callback) {
        requestDataAsync(requestId, url, data, callback, Request.VERB_PUT);
    }

    public static Response putDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, Request.VERB_PUT);
    }

    public static void deleteDataAsync(int requestId, URL url, Map<String, String> data, NetworkCallback callback) {
        requestDataAsync(requestId, url, data, callback, Request.VERB_DELETE);
    }

    public static Response deleteDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, Request.VERB_DELETE);
    }
}
