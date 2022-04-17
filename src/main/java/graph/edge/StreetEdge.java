package graph.edge;

import graph.Colorable;

public class StreetEdge extends Edge {
    @Override
    public String toString() {
        return String.format("STREET(%.02f)", super.getWeight());
    }

    @Override
    public String getType() {
        return "street";
    }

    @Override
    public String getColor() {
        return Colorable.STREET_COLOR;
    }
}
