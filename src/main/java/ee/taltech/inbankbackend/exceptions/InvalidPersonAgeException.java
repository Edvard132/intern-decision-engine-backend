package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when person is not adult or person age and requested loan period is over expected lifetime.
 */
public class InvalidPersonAgeException extends Throwable{

    private final String message;
    private final Throwable cause;

    public InvalidPersonAgeException(String message) {
        this(message, null);
    }

    public InvalidPersonAgeException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
