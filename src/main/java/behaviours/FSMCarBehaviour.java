package behaviours;

import graph.GraphUtils;
import graph.vertex.Point;
import graph.vertex.Semaphore;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.util.Set;

public class FSMCarBehaviour extends FSMBehaviour {

    public static String LISTENING_STATE = "LISTENING";

    private Point currentLocation;

    public FSMCarBehaviour(Agent a, Graph<Point, DefaultWeightedEdge> graph) {
        super(a);

        // choose current location from random in the graph
        Set<Semaphore> points = GraphUtils.getSemaphores(graph);
        // choose random point from set
        this.currentLocation = points.toArray(new Point[0])[(int) (Math.random() * points.size())];
    }

    public FSMCarBehaviour(Agent a, Graph<Point, DefaultWeightedEdge> graph, Point initialLocation) {
        super(a);

        this.currentLocation = initialLocation;

        this.registerStates();
    }

    private void registerStates() {
        this.registerFirstState(new ListeningState(), LISTENING_STATE);
    }

    class ListeningState extends OneShotBehaviour {

        @Override
        public void action() {
            // register in service
            ServiceUtils.register(this.myAgent, ServiceUtils.CAR);

            // wait for messages
            ACLMessage msg = this.myAgent.receive();

            if (msg != null) {
                System.out.printf("%s: %s%n", msg.getSender().getLocalName(), msg.getContent());
            } else {
                this.block();
            }
        }
    }
}
