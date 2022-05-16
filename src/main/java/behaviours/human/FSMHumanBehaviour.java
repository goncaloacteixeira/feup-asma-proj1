package behaviours.human;

import agents.HumanResults;
import agents.HumanAgent;
import agents.HumanPreferences;
import graph.GraphUtils;
import graph.edge.Edge;
import graph.vertex.Point;
import jade.core.behaviours.FSMBehaviour;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

public class FSMHumanBehaviour extends FSMBehaviour {
    static String STATE_EVAL = "EVAL";
    static String STATE_CAR = "CAR";
    static String STATE_TRC = "TRAVEL_CAR";
    static String STATE_RID = "ASK_RIDE";
    static String STATE_WAI = "WAIT_RIDE";
    static String STATE_TRD = "TRAVEL_DEFAULT";
    static String STATE_DST = "DST";
    static String STATE_CNI = "INITIATOR";
    static String STATE_CNR = "RESPONDER";
    static String STATE_LEC = "LEAVE_CAR";

    static int EVENT_DEF = 0;
    static int EVENT_CAR = 1;
    static int EVENT_DST = 2;
    static int EVENT_INITIATE = 3;
    static int EVENT_RESPOND = 4;
    static int EVENT_FAIL = 5;
    static int EVENT_CAR_END = 6; // for when the car travel reaches the destination
    static int EVENT_FOUND_SHARE = 7;
    static int EVENT_FOUND_CAR = 8;

    protected int currentLocationIndex = 0;
    protected Graph<Point, DefaultWeightedEdge> graph;
    protected Graph<Point, DefaultWeightedEdge> original;
    protected GraphPath<Point, DefaultWeightedEdge> path;
    protected HumanPreferences preferences;

    /**
     * The name of the service associated with the car that is being used by this human.
     * <p>
     * Messages between the car and all passengers using are to be sent through this service.
     */
    @Getter
    @Setter
    private String currentCarService;

    public FSMHumanBehaviour(HumanAgent agent, Graph<Point, DefaultWeightedEdge> graph, Graph<Point, DefaultWeightedEdge> original, String src, String dst, HumanPreferences preferences) {
        super(agent);
        this.graph = graph;
        this.original = original;
        this.path = GraphUtils.getPathFromAtoB(graph, src, dst);
        this.preferences = preferences;

        double cost = GraphUtils.calculateCostForHuman(graph, path, (HumanAgent) myAgent);
        System.out.printf("%s: Path: %s with edges %s (Cost: %.02f)\n", myAgent.getLocalName(), path.getVertexList(), path, path.getWeight());

        agent.setResults(new HumanResults(myAgent.getLocalName(), path.getVertexList().toString(), cost, agent.getSettings().isCarShareInitiator()));
        // agent.informResults(new PathStart(myAgent.getLocalName(), path.getVertexList().toString(), cost, ((HumanAgent) myAgent).getSettings().isCarShareInitiator()));

        this.registerFirstState(new EvaluatePathBehaviour(this), STATE_EVAL);
        this.registerLastState(new DestinationBehaviour(this), STATE_DST);

        this.registerState(new TravelDefaultBehaviour(this), STATE_TRD);
        this.registerState(new StartCarShareBehaviour(this), STATE_CAR);
        this.registerState(new TravelCarBehaviour(this), STATE_TRC);
        this.registerState(new CNIHelperBehaviour(this), STATE_CNI);
        this.registerState(new CNRHelperBehaviour(this), STATE_CNR);
        this.registerState(new AskCarRideBehaviour(this), STATE_RID);
        this.registerState(new WaitCarRideBehaviour(this), STATE_WAI);
        this.registerState(new LeaveCarBehaviour(this), STATE_LEC);

        this.registerTransition(STATE_EVAL, STATE_CAR, EVENT_CAR);
        this.registerTransition(STATE_EVAL, STATE_DST, EVENT_DST);
        this.registerTransition(STATE_EVAL, STATE_TRD, EVENT_DEF);
        this.registerDefaultTransition(STATE_TRD, STATE_EVAL);

        this.registerTransition(STATE_CAR, STATE_CNI, EVENT_INITIATE);
        this.registerTransition(STATE_CAR, STATE_CNR, EVENT_RESPOND);
        this.registerDefaultTransition(STATE_CNI, STATE_RID); // after taking care of car sharing, ask for car ride
        this.registerDefaultTransition(STATE_CNR, STATE_RID); // if it doesn't get any share, go ask for the car themselves
        this.registerTransition(STATE_CNR, STATE_WAI, EVENT_FOUND_SHARE); // if it did find a share, wait for the car to arrive
        this.registerDefaultTransition(STATE_RID, STATE_RID);
        this.registerTransition(STATE_RID, STATE_WAI, EVENT_FOUND_CAR);
        this.registerTransition(STATE_WAI, STATE_EVAL, EVENT_FAIL); // if there is a problem with the car ride, go back to eval
        this.registerDefaultTransition(STATE_WAI, STATE_TRC);

        this.registerTransition(STATE_TRC, STATE_TRC, EVENT_CAR);
        this.registerTransition(STATE_TRC, STATE_LEC, EVENT_CAR_END);
        this.registerTransition(STATE_LEC, STATE_EVAL, EVENT_DEF);
        this.registerTransition(STATE_LEC, STATE_DST, EVENT_DST);
    }

    /**
     * Method to get string about traveling operations
     *
     * @return travel information
     */
    public String informTravel() {
        Point pt1 = this.path.getVertexList().get(this.currentLocationIndex);
        Point pt2 = this.path.getVertexList().get(this.currentLocationIndex + 1);
        Edge edge = (Edge) this.path.getEdgeList().get(this.currentLocationIndex);

        return String.format("%s: Moving from [%s] to [%s] by %s", this.myAgent.getLocalName(), pt1, pt2, edge);
    }
}
