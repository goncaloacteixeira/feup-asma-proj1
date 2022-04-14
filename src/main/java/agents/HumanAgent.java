package agents;

import behaviours.BroadcastBehaviour;
import behaviours.TravelBehaviour;
import graph.CityGraph;
import graph.vertex.Point;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class HumanAgent extends Agent {
    private final String broadcastService;
    private String srcPoint;
    private String dstPoint;


    public HumanAgent() {
        this.broadcastService = "human-broadcast-service";
    }

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.srcPoint = (String) args[0];
        this.dstPoint = (String) args[1];

        String broadcastMessage = String.format("I will go from %s to %s", srcPoint, dstPoint);
        Graph<Point, DefaultWeightedEdge> graph = CityGraph.importCustomWeights("citygraph.dot", 0, Integer.MAX_VALUE, 0);

        addBehaviour(new BroadcastBehaviour(this, ACLMessage.INFORM, broadcastMessage, this.broadcastService));
        addBehaviour(new TravelBehaviour(this, graph, srcPoint, dstPoint));
    }

    public void informMovement(String message) {
        addBehaviour(new BroadcastBehaviour(this, ACLMessage.INFORM, message, this.broadcastService));
    }

    @Override
    protected void takeDown() {
        System.out.printf("%s: Went from %s to %s%n", getLocalName(), srcPoint, dstPoint);
    }
}
