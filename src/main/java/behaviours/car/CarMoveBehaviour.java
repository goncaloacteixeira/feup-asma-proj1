package behaviours.car;

import agents.CarAgent;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;

public class CarMoveBehaviour extends Behaviour {

    private final CarAgent carAgent;

    private final GraphPath<Point, DefaultWeightedEdge> path;

    private int currentPathIndex;

    public CarMoveBehaviour(Agent a, Point moveTo) {
        super(a);

        this.currentPathIndex = 0;
        this.carAgent = (CarAgent) a;
        this.path = GraphUtils.getPathFromAtoB(this.carAgent.getGraph(), this.carAgent.getCurrentLocation().getName(), moveTo.getName()); // TODO get only roads
    }

    @Override
    public void action() {
        if (this.currentPathIndex < this.path.getVertexList().size() - 1) {
            System.out.printf("%s: moving from [%s] to [%s]%n", this.carAgent.getLocalName(), this.path.getVertexList().get(this.currentPathIndex).getName(), this.path.getVertexList().get(this.currentPathIndex + 1).getName());
            this.currentPathIndex++;
            this.carAgent.moveTo(this.path.getVertexList().get(this.currentPathIndex));
        }
    }

    @Override
    public boolean done() {
        return this.currentPathIndex == this.path.getVertexList().size();
    }
}
