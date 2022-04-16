package behaviours;

import agents.HumanAgent;
import graph.CityGraph;
import graph.edge.Edge;
import graph.edge.RoadEdge;
import graph.vertex.Point;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

public class FSMHumanBehaviour extends FSMBehaviour {
    static String STATE_EVAL = "EVAL";
    static String STATE_CAR = "CAR";
    static String STATE_TRC = "TRAVEL_CAR";
    static String STATE_TRD = "TRAVEL_DEFAULT";
    static String STATE_DST = "DST";

    static int EVENT_DEF = 0;
    static int EVENT_CAR = 1;
    static int EVENT_DST = 2;

    private int currentLocationIndex = 0;
    private Graph<Point, DefaultWeightedEdge> graph;
    private GraphPath<Point, DefaultWeightedEdge> path;

    public FSMHumanBehaviour(Agent a, Graph<Point, DefaultWeightedEdge> graph, String src, String dst) {
        super(a);
        this.graph = graph;
        this.path = CityGraph.getPathFromAtoB(graph, src, dst);

        System.out.println(myAgent.getLocalName() + ": Path: " + CityGraph.printPath(graph, path));

        this.registerFirstState(new EvaluatePath(), STATE_EVAL);
        this.registerLastState(new Destination(), STATE_DST);

        this.registerState(new TravelDefault(), STATE_TRD);
        this.registerState(new StartCarShare(), STATE_CAR);
        this.registerState(new TravelCar(), STATE_TRC);

        this.registerTransition(STATE_EVAL, STATE_CAR, EVENT_CAR);
        this.registerTransition(STATE_EVAL, STATE_DST, EVENT_DST);
        this.registerTransition(STATE_EVAL, STATE_TRD, EVENT_DEF);
        this.registerDefaultTransition(STATE_TRD, STATE_EVAL);
        this.registerDefaultTransition(STATE_CAR, STATE_TRC);
        this.registerTransition(STATE_TRC, STATE_TRC, EVENT_CAR);
        this.registerTransition(STATE_TRC, STATE_EVAL, EVENT_DEF);
        this.registerTransition(STATE_TRC, STATE_DST, EVENT_DST);
    }

    class EvaluatePath extends OneShotBehaviour {
        private int exitValue;

        @Override
        public void action() {
            if (currentLocationIndex == path.getLength()) {
                exitValue = FSMHumanBehaviour.EVENT_DST;
                return;
            }

            DefaultWeightedEdge edge = path.getEdgeList().get(currentLocationIndex);

            if (edge instanceof RoadEdge) {
                exitValue = FSMHumanBehaviour.EVENT_CAR;
            } else {
                exitValue = FSMHumanBehaviour.EVENT_DEF;
            }
        }

        @Override
        public int onEnd() {
            return exitValue;
        }
    }

    static class Destination extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println(myAgent.getLocalName() + ": Completed path!");
        }
    }

    class TravelDefault extends OneShotBehaviour {
        @Override
        public void action() {
            Point pt1 = path.getVertexList().get(currentLocationIndex);
            Point pt2 = path.getVertexList().get(currentLocationIndex + 1);
            Edge edge = (Edge) path.getEdgeList().get(currentLocationIndex++);

            String msg = String.format("Moving from [%s] to [%s] by %s", pt1, pt2, edge);

            System.out.println(myAgent.getLocalName() + ":" + msg);
            ((HumanAgent) myAgent).informMovement(msg);
        }
    }

    class TravelCar extends OneShotBehaviour {
        private int exitValue;

        @Override
        public void action() {
            Point pt1 = path.getVertexList().get(currentLocationIndex);
            Point pt2 = path.getVertexList().get(currentLocationIndex + 1);
            Edge edge = (Edge) path.getEdgeList().get(currentLocationIndex++);

            String msg = String.format("Moving from [%s] to [%s] by %s", pt1, pt2, edge);

            System.out.println(myAgent.getLocalName() + ":" + msg);
            ((HumanAgent) myAgent).informMovement(msg);

            if (currentLocationIndex == path.getLength()) {
                exitValue = FSMHumanBehaviour.EVENT_DST;
                return;
            }

            edge = (Edge) path.getEdgeList().get(currentLocationIndex);

            if (edge instanceof RoadEdge) {
                exitValue = FSMHumanBehaviour.EVENT_CAR;
            } else {
                exitValue = FSMHumanBehaviour.EVENT_DEF;
            }
        }

        @Override
        public int onEnd() {
            return exitValue;
        }
    }

    class StartCarShare extends OneShotBehaviour {
        @Override
        public void action() {
            System.out.println(myAgent.getLocalName() + ": Start Car Share...");
        }
    }
}
