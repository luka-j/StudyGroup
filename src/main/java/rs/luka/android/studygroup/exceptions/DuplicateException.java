package rs.luka.android.studygroup.exceptions;

/**
 * Created by luka on 3.1.16..
 */
public class DuplicateException extends MarkerException {
    public DuplicateException() {super();}
    public DuplicateException(Class origin, String message) {super(origin, message);}
}
