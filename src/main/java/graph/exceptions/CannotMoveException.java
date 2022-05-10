package graph.exceptions;

/**
 * Exception to be thrown when a specific move inside the graph is not possible.
 */
public class CannotMoveException extends Exception {

    public CannotMoveException(String message) {
        super(message);
    }
}
