package graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public abstract class Edge extends DefaultWeightedEdge implements Colorable {
    public abstract String getType();
}
