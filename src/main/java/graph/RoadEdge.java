package graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class RoadEdge extends DefaultWeightedEdge {
    @Override
    public String toString() {
        return String.format("%.02f", super.getWeight());
    }
}
