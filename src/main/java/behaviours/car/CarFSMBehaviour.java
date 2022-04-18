package behaviours.car;

import graph.GraphUtils;
import graph.vertex.Point;
import graph.vertex.Semaphore;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.util.Set;

public class CarFSMBehaviour extends FSMBehaviour {

    public static String LISTENING_STATE = "LISTENING";

    private Point currentLocation;

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
        // TODO
//        this.registerFirstState(new ListeningState(), LISTENING_STATE);
        this.myAgent.addBehaviour(new ListeningState());
    }

    class ListeningState extends OneShotBehaviour {

        @Override
        public void action() {
            // register in service
            ServiceUtils.register(this.myAgent, ServiceUtils.CAR_RIDE);
            System.out.println("Car " + this.myAgent.getLocalName() + " is listening");

            this.myAgent.addBehaviour(new CarRideContractNetResponderBehaviour(this.myAgent, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
        }
    }
}
