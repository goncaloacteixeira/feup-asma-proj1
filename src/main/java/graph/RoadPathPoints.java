package graph;

import lombok.Getter;

import java.io.Serializable;

public record RoadPathPoints(@Getter String srcPoint, @Getter String dstPoint) implements Serializable {

    @Override
    public String toString() {
        return String.format("ROAD PATH FROM [%s] TO [%s]", srcPoint, dstPoint);
    }
}
