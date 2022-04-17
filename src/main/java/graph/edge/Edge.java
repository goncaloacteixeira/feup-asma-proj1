package graph.edge;

import graph.Colorable;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Objects;

public abstract class Edge extends DefaultWeightedEdge implements Colorable {
    public abstract String getType();

    @Override
    public boolean equals(Object obj) {
        return Objects.equals(this.getType(), ((Edge) obj).getType()) && this.getWeight() == ((Edge) obj).getWeight();
    }
}
