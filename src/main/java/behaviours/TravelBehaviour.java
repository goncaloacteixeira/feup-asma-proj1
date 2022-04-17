package behaviours;

import agents.HumanAgent;
import graph.GraphUtils;
import graph.edge.Edge;
import graph.vertex.Point;
import jade.core.behaviours.Behaviour;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

public class TravelBehaviour extends Behaviour {
    private boolean done = false;
    private int currentLocationIndex = 0;
    private GraphPath<Point, DefaultWeightedEdge> path;
    private final Graph<Point, DefaultWeightedEdge> graph;

    public TravelBehaviour(HumanAgent a, Graph<Point, DefaultWeightedEdge> graph, String srcPoint, String dstPoint) {
        super(a);
        this.graph = graph;
        this.path = GraphUtils.getPathFromAtoB(graph, srcPoint, dstPoint);
        System.out.println(myAgent.getLocalName() + ": Path is: " + GraphUtils.printPath(graph, path));
    }

    @Override
    public void action() {
        Point pt1 = path.getVertexList().get(currentLocationIndex);
        Point pt2 = path.getVertexList().get(currentLocationIndex + 1);
        Edge edge = (Edge) path.getEdgeList().get(currentLocationIndex++);

        String msg = String.format("Moving from [%s] to [%s] by %s", pt1, pt2, edge);

        System.out.println(myAgent.getLocalName() + ":" + msg);
        ((HumanAgent) myAgent).informMovement(msg);

        if (currentLocationIndex == this.path.getLength()) {
            myAgent.doDelete();
            this.done = true;
        }
    }

    @Override
    public boolean done() {
        return this.done;
    }
}
