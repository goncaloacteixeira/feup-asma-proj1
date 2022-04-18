package agents;

import behaviours.car.CarRideContractNetInitiatorBehaviour;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.FileNotFoundException;

public class DeleteMePleaseAgent extends Agent {

    // FIXME
    // delete met
    // i'm only here to make car ride requests to test

    @Override
    public void setup() {
        Graph<Point, DefaultWeightedEdge> graph = null;
        try {
            graph = GraphUtils.importDefaultGraph();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        var semaphores = GraphUtils.getSemaphores(graph);

        var pla1 = semaphores.stream().findFirst().get();
        var pla2 = semaphores.stream().skip(1).findFirst().get();

        this.addBehaviour(new CarRideContractNetInitiatorBehaviour(this, new ACLMessage(ACLMessage.CFP), pla1, pla2));
    }
}
