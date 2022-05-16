package agents;

import java.io.Serializable;

public record SharedSegment(String path, boolean initiator) implements Serializable {
    @Override
    public String toString() {
        return String.format("Path: %s, Initiator: %s", path, initiator);
    }
}
