package network;
/**
 * Exception thrown when user login fails or server rejects authentication.
 */
public class LogInError extends RuntimeException {
    /**
     * Creates a new LogInError with the given message.
     *
     * @param message the explanation of the login failure
     */
    public LogInError(String message) {
        super(message);
    }
}
