package graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class SubwayEdge extends Edge {
    @Override
    public String toString() {
        return String.format("SUBWAY(%.02f)", super.getWeight());
    }

    @Override
    public String getType() {
        return "subway";
    }

    @Override
    public String getColor() {
        return Colorable.SUBWAY_COLOR;
    }
}
