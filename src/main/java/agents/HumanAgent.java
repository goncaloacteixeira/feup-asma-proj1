package agents;

import behaviours.BroadcastBehaviour;
import behaviours.FSMHumanBehaviour;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.FileNotFoundException;

public class HumanAgent extends Agent {
    private final String broadcastService;
    private String srcPoint;
    private String dstPoint;
    private boolean initiator;


    public HumanAgent() {
        this.broadcastService = "human-broadcast-service";
    }

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.srcPoint = (String) args[0];
        this.dstPoint = (String) args[1];
        this.initiator = (Boolean) args[2];

        String broadcastMessage = String.format("I will go from %s to %s", srcPoint, dstPoint);

        Graph<Point, DefaultWeightedEdge> graph = null;
        try {
            graph = GraphUtils.importGraph("citygraph.dot");
            addBehaviour(new BroadcastBehaviour(this, ACLMessage.INFORM, broadcastMessage, this.broadcastService));

            FSMHumanBehaviour humanBehaviour = new FSMHumanBehaviour(this, graph, srcPoint, dstPoint, initiator);

            addBehaviour(humanBehaviour);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void informMovement(String message) {
        addBehaviour(new BroadcastBehaviour(this, ACLMessage.INFORM, message, this.broadcastService));
    }

    @Override
    protected void takeDown() {
        System.out.printf("%s: Went from %s to %s%n", getLocalName(), srcPoint, dstPoint);
    }
}
