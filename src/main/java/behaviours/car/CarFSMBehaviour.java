package behaviours.car;

import graph.RoadPathPoints;
import graph.vertex.Point;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

public class CarFSMBehaviour extends FSMBehaviour {

    public static final String STATE_LISTENING = "LISTENING";
    public static final String STATE_MOVING = "MOVING";
    public static final String STATE_TRANSPORTING = "TRANSPORTING";
    public static final String STATE_END = "END";

    public static final int EVENT_PROPOSAL_ACCEPTED = 1;
    public static final int EVENT_TRAVEL_END = 2;

    @Getter
    @Setter
    private RoadPathPoints roadPathPoints;

    @Getter
    @Setter
    private GraphPath<Point, DefaultWeightedEdge> currentPath;

    @Getter
    @Setter
    private AID currentHuman;

    public CarFSMBehaviour(Agent a) {
        super(a);

        this.registerStates();
    }

    private void registerStates() {
        this.registerFirstState(new CarListeningBehaviour(this.myAgent, this), STATE_LISTENING);
        this.registerLastState(new CarEndBehaviour(), STATE_END);

        this.registerState(new CarMoveBehaviour(this), STATE_MOVING);
        this.registerState(new CarTransportBehaviour(this), STATE_TRANSPORTING);

        this.registerTransition(STATE_LISTENING, STATE_MOVING, EVENT_PROPOSAL_ACCEPTED);
        this.registerDefaultTransition(STATE_LISTENING, STATE_END);
        this.registerDefaultTransition(STATE_MOVING, STATE_TRANSPORTING);
        this.registerDefaultTransition(STATE_TRANSPORTING, STATE_END);
        this.registerTransition(STATE_TRANSPORTING, STATE_LISTENING, EVENT_TRAVEL_END);
    }
}
