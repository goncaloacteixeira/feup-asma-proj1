package graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public abstract class Edge extends DefaultWeightedEdge {
    abstract String getType();
}
