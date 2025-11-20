package network;

public class LogInError extends RuntimeException {
    public LogInError(String message) {
        super(message);
    }
}
