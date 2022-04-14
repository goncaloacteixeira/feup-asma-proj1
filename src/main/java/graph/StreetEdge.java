package graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class StreetEdge extends Edge {
    @Override
    public String toString() {
        return String.format("STREET(%.02f)", super.getWeight());
    }

    @Override
    String getType() {
        return "street";
    }

    @Override
    public String getColor() {
        return Colorable.STREET_COLOR;
    }
}
