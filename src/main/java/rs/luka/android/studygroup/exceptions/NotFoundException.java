package rs.luka.android.studygroup.exceptions;

/**
 * Created by luka on 3.1.16..
 */
public class NotFoundException extends MarkerException {
    public NotFoundException() {super();}
    public NotFoundException(Class origin, String message) {super(origin, message);}
}
