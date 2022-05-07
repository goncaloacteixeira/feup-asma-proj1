package behaviours.human;

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
import utils.ServiceUtils;

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

    static int EVENT_DEF = 0;
    static int EVENT_CAR = 1;
    static int EVENT_DST = 2;
    static int EVENT_INITIATE = 3;
    static int EVENT_RESPOND = 4;
    static int EVENT_FAIL = 5;

    public final static String CAR_SHARE_INIT_SERVICE = "car-share-initiators";
    public final static String CAR_SHARE_RESP_SERVICE = "car-share-responders";

    protected int currentLocationIndex = 0;
    protected Graph<Point, DefaultWeightedEdge> graph;
    protected GraphPath<Point, DefaultWeightedEdge> path;
    protected HumanPreferences preferences;

    /**
     * The name of the service associated with the car that is being used by this human.
     *
     * Messages between the car and all passengers using are to be sent through this service.
     */
    @Getter
    @Setter
    private String currentCarService;

    public FSMHumanBehaviour(HumanAgent a, Graph<Point, DefaultWeightedEdge> graph, String src, String dst, HumanPreferences preferences) {
        super(a);
        this.graph = graph;
        this.path = GraphUtils.getPathFromAtoB(graph, src, dst);
        this.preferences = preferences;

        // Humans either init car share or respond to car sharing when they start a new road travel
        ServiceUtils.register(myAgent, preferences.isCarShareInitiator() ? CAR_SHARE_INIT_SERVICE : CAR_SHARE_RESP_SERVICE);

        System.out.printf("%s: Path: %s (Cost: %.02f)\n", myAgent.getLocalName(), path.getVertexList(), path.getWeight());

        this.registerFirstState(new EvaluatePathBehaviour(this), STATE_EVAL);
        this.registerLastState(new DestinationBehaviour(this), STATE_DST);

        this.registerState(new TravelDefaultBehaviour(this), STATE_TRD);
        this.registerState(new StartCarShareBehaviour(this), STATE_CAR);
        this.registerState(new TravelCarBehaviour(this), STATE_TRC);
        this.registerState(new CNIHelperBehaviour(this), STATE_CNI);
        this.registerState(new CNRHelperBehaviour(this), STATE_CNR);
        this.registerState(new AskCarRideBehaviour(this), STATE_RID);
        this.registerState(new WaitCarRideBehaviour(this), STATE_WAI);

        this.registerTransition(STATE_EVAL, STATE_CAR, EVENT_CAR);
        this.registerTransition(STATE_EVAL, STATE_DST, EVENT_DST);
        this.registerTransition(STATE_EVAL, STATE_TRD, EVENT_DEF);
        this.registerDefaultTransition(STATE_TRD, STATE_EVAL);

        this.registerTransition(STATE_CAR, STATE_CNI, EVENT_INITIATE);
        this.registerTransition(STATE_CAR, STATE_CNR, EVENT_RESPOND);
        this.registerDefaultTransition(STATE_CNI, STATE_RID); // after taking care of car sharing, ask for car ride
        this.registerDefaultTransition(STATE_CNR, STATE_WAI); // after getting a share, waits for the car ride
        this.registerDefaultTransition(STATE_RID, STATE_WAI);
        this.registerTransition(STATE_WAI, STATE_EVAL, EVENT_FAIL); // if there is a problem with the car ride, go back to eval
        this.registerDefaultTransition(STATE_WAI, STATE_TRC);

        this.registerTransition(STATE_TRC, STATE_TRC, EVENT_CAR);
        this.registerTransition(STATE_TRC, STATE_EVAL, EVENT_DEF);
        this.registerTransition(STATE_TRC, STATE_DST, EVENT_DST);
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
