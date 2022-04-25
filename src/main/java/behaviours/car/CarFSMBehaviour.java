package behaviours.car;

import graph.GraphUtils;
import graph.vertex.Point;
import graph.vertex.Semaphore;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.util.Set;

public class CarFSMBehaviour extends FSMBehaviour {

    public static String STATE_LISTENING = "LISTENING";
    public static String STATE_MOVING = "MOVING";
    public static String STATE_END = "END";

    public static int EVENT_PROPOSAL_ACCEPTED = 1;

    @Getter
    @Setter
    private Point currentDestination;

    private Point currentLocation;

    @Getter
    @Setter
    private AID currentHuman;

    public CarFSMBehaviour(Agent a, Graph<Point, DefaultWeightedEdge> graph) {
        super(a);

        // choose current location from random in the graph
        Set<Semaphore> points = GraphUtils.getSemaphores(graph);
        // choose random point from set
        this.currentLocation = points.toArray(new Point[0])[(int) (Math.random() * points.size())];

        this.registerStates();
    }

    public CarFSMBehaviour(Agent a, Graph<Point, DefaultWeightedEdge> graph, Point initialLocation) {
        super(a);

        this.currentLocation = initialLocation;

        this.registerStates();
    }

    private void registerStates() {
        this.registerFirstState(new CarListeningBehaviour(this.myAgent, this), STATE_LISTENING);
        this.registerLastState(new CarEndBehaviour(), STATE_END);

        this.registerState(new CarMoveBehaviour(this), STATE_MOVING);

        this.registerTransition(STATE_LISTENING, STATE_MOVING, EVENT_PROPOSAL_ACCEPTED);
        this.registerDefaultTransition(STATE_LISTENING, STATE_END);
        this.registerDefaultTransition(STATE_MOVING, STATE_END);
        // TODO keep on the travel
    }
}
