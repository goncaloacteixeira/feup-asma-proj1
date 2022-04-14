package graph;

import org.jgrapht.graph.DefaultWeightedEdge;

public class RoadEdge extends Edge {
    @Override
    public String toString() {
        return String.format("ROAD(%.02f)", super.getWeight());
    }

    @Override
    public String getType() {
        return "road";
    }

    @Override
    public String getColor() {
        return Colorable.ROAD_COLOR;
    }
}
