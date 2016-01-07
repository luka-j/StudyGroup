package rs.luka.android.studygroup.exceptions;

/**
 * Created by luka on 3.1.16..
 */
public class MarkerException extends Exception {
    public MarkerException() {super();}
    public MarkerException(Class origin) {super("Origin: " + origin.getCanonicalName());}
    public MarkerException(Class origin, String message) {super("Origin: " + origin.getCanonicalName() +
                                                               "\nMessage: " + message);}
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
