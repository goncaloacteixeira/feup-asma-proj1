package agents;

import behaviours.FSMCarBehaviour;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.Agent;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.FileNotFoundException;

public class CarAgent extends Agent {

    private String carName;

    private int carNumber;

    private int carCapacity;

    public CarAgent() {
    }

    @Override
    public void setup() {
        Object[] args = this.getArguments();

        this.carCapacity = (int) args[0];

        try {
            Graph<Point, DefaultWeightedEdge> graph = GraphUtils.importDefaultGraph();
            this.addBehaviour(new FSMCarBehaviour(this, graph));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
