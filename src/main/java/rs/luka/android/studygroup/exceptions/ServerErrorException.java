package rs.luka.android.studygroup.exceptions;

/**
 * Created by luka on 3.1.16..
 */
public class ServerErrorException extends  MarkerException {
    public ServerErrorException() {super();}
    public ServerErrorException(Class origin, String message) {super(origin, message);}
}
