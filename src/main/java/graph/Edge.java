package graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public abstract class Edge extends DefaultWeightedEdge implements Colorable {
    abstract String getType();
}
