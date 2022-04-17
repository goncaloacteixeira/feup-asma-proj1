package graph.exceptions;

public class NoRoadsException extends Exception {
    public NoRoadsException() {
        super("No roads available for that path!");
    }
}
