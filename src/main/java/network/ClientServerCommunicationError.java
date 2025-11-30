package network;
/**
 * Exception thrown when the client receives an error response from the server
 * or when a communication failure occurs during a client–server request cycle.
 * <p>
 * This is typically used to wrap server-side error messages contained in JSON
 * responses for easier propagation and handling at higher levels of the application.
 * </p>
 */
public class ClientServerCommunicationError extends RuntimeException {
    /**
     * Constructs a new communication error with the specified message.
     *
     * @param message the error message returned by the server or describing the failure
     */
    public ClientServerCommunicationError(String message) {
        super(message);
    }
}
