package graph;

import java.io.Serializable;

public class RoadPathPoints implements Serializable {
    public final String srcPoint;
    public final String dstPoint;

    public RoadPathPoints(String srcPoint, String dstPoint) {
        this.srcPoint = srcPoint;
        this.dstPoint = dstPoint;
    }

    @Override
    public String toString() {
        return String.format("ROAD PATH FROM [%s] TO [%s]", srcPoint, dstPoint);
    }
}
