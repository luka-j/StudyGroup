package rs.luka.android.studygroup.exceptions;

/**
 * Created by luka on 3.1.16..
 */
public class NotLoggedInException extends MarkerException {
    public NotLoggedInException() {super();}
    public NotLoggedInException(Class origin, String message) {super(origin, message);}
}
