package graph;

import graph.vertex.Point;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class GraphPointWrapper implements Serializable {
    private final String dotGraph;
    private final String srcPoint;
    private final String dstPoint;

    public GraphPointWrapper(String dotGraph, String srcPoint, String dstPoint) {
        this.dotGraph = dotGraph;
        this.srcPoint = srcPoint;
        this.dstPoint = dstPoint;
    }

    public String getDotGraph() {
        return dotGraph;
    }

    public String getSrcPoint() {
        return srcPoint;
    }

    public String getDstPoint() {
        return dstPoint;
    }

    public Graph<Point, DefaultWeightedEdge> getGraph() {
        return GraphUtils.getFromDOT(new ByteArrayInputStream(dotGraph.getBytes(StandardCharsets.UTF_8)));
    }
}
