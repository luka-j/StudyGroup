package rs.luka.android.studygroup.io.network;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rs.luka.android.studygroup.model.User;

/**
 * Created by luka on 22.1.16..
 */
public class NetworkRequests {

    public static final  Map<String, String> emptyMap = Collections.emptyMap();
    private static final String VERB_POST = "POST";
    private static final String VERB_GET = "GET";
    private static final String VERB_PUT = "PUT";
    private static final String VERB_DELETE = "DELETE";
    private static final ExecutorService     executor = Executors.newCachedThreadPool();

    private static void requestDataAsync(int requestId, URL url, Map<String, String> data, Network.NetworkCallbacks<String> callback, String verb) {
        executor.submit(new Network.StringRequest(requestId, url, User.getInstanceToken(), data, verb, callback));
    }

    private static void requestFileAsync(int requestId, URL url, File data, File saveTo,
                                         Network.NetworkCallbacks<File> callbacks, String verb) {
        executor.submit(new Network.FileRequest(requestId, url, User.getInstanceToken(), data, verb, callbacks, saveTo));
    }

    private static Network.Response<String> requestDataBlocking(int requestId, URL url, Map<String, String> data, long timeout,
                                                        TimeUnit unit, String verb)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        Future<Network.Response<String>> task = executor.submit(new Network.StringRequest(requestId, url, User.getInstanceToken(), data, verb, null));
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

    public static void postDataAsync(int requestId, URL url, Map<String, String> data, Network.NetworkCallbacks<String> callback) {
        requestDataAsync(requestId, url, data, callback, VERB_POST);
    }

    public static Network.Response<String> postDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, VERB_POST);
    }

    public static Network.Response<String> requestPostData(URL url, Map<String, String> param) throws IOException {
        return new Network.StringRequest(-1, url, User.getInstanceToken(), param, VERB_POST, null).call();
    }

    public static void getDataAsync(int requestId, URL url, Network.NetworkCallbacks<String> callback) {
        requestDataAsync(requestId, url, emptyMap, callback, VERB_GET);
    }

    public static Network.Response<String> getDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, VERB_GET);
    }

    public static Network.Response<String> requestGetData(URL url) throws IOException {
        return new Network.StringRequest(-1, url, User.getInstanceToken(), emptyMap, VERB_GET, null).call();
    }

    public static void putDataAsync(int requestId, URL url, Map<String, String> data, Network.NetworkCallbacks<String> callback) {
        requestDataAsync(requestId, url, data, callback, VERB_PUT);
    }

    public static Network.Response<String> putDataBlocking(int requestId, URL url, Map<String, String> data, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, data, timeout, unit, VERB_PUT);
    }

    public static Network.Response<String> requestPutData(URL url, Map<String, String> param) throws IOException {
        return new Network.StringRequest(-1, url, User.getInstanceToken(), param, VERB_PUT, null).call();
    }

    public static void deleteDataAsync(int requestId, URL url, Network.NetworkCallbacks<String> callback) {
        requestDataAsync(requestId, url, emptyMap, callback, VERB_DELETE);
    }

    public static Network.Response<String> deleteDataBlocking(int requestId, URL url, long timeout, TimeUnit unit)
            throws ExecutionException, TimeoutException, FileNotFoundException, IOException {
        return requestDataBlocking(requestId, url, emptyMap, timeout, unit, VERB_DELETE);
    }

    public static Network.Response<File> requestGetFile(URL url, File saveTo) throws IOException {
        return new Network.FileRequest(-1, url, User.getInstanceToken(), null, VERB_GET, null, saveTo).call();
    }
    public static Network.Response<File> requestPutFile(URL url, File data) throws IOException {
        return new Network.FileRequest(-1, url, User.getInstanceToken(), data, VERB_PUT, null, null).call();
    }

}
