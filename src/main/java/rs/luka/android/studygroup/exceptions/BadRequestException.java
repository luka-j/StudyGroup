package rs.luka.android.studygroup.exceptions;

/**
 * Created by luka on 3.1.16..
 */
public class BadRequestException extends MarkerException {
    public BadRequestException() {super();}
    public BadRequestException(Class origin, String message) {super(origin, message);}
}
