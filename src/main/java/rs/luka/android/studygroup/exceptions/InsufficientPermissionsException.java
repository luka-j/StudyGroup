package rs.luka.android.studygroup.exceptions;

/**
 * Created by luka on 3.1.16..
 */
public class InsufficientPermissionsException extends MarkerException {
    public InsufficientPermissionsException() {super();}

    public InsufficientPermissionsException(Class origin, String message) {super(origin, message);}
}
