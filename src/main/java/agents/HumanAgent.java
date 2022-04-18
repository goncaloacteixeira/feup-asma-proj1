package agents;

import behaviours.BroadcastBehaviour;
import behaviours.human.FSMHumanBehaviour;
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
    private HumanPreferences settings;

    public HumanAgent() {
        this.broadcastService = "human-broadcast-service";
    }

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.srcPoint = (String) args[0];               // Source Point for Travel
        this.dstPoint = (String) args[1];               // Destiny Point for Travel
        this.settings = (HumanPreferences) args[2];     // Preferences (weights and initiators)

        try {
            // for each agent we need to import a new graph since weights vary from agent to agent
            Graph<Point, DefaultWeightedEdge> graph = GraphUtils.importGraph("citygraph.dot", settings.streetWeight, settings.roadWeight, settings.subwayWeight);

            // add Finite State Machine Behaviour
            addBehaviour(new FSMHumanBehaviour(this, graph, srcPoint, dstPoint, settings));
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
