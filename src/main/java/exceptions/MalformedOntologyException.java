package exceptions;

/**
 * Created by daniel on 22.08.14.
 */
public class MalformedOntologyException extends Exception {
    public MalformedOntologyException() {
        super();
    }

    public MalformedOntologyException(String message) {
        super(message);
    }
}
