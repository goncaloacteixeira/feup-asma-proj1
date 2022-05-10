package agents;

import behaviours.car.CarFSMBehaviour;
import graph.GraphUtils;
import graph.exceptions.CannotMoveException;
import graph.vertex.Point;
import graph.vertex.Semaphore;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Set;

public class CarAgent extends SubscribableAgent {

    @Getter
    private int carCapacity;

    @Getter
    private Graph<Point, DefaultWeightedEdge> graph;

    @Getter
    private Point currentLocation;

    @Getter
    @Setter
    private DFAgentDescription agentDescription;

    @Override
    public void setup() {
        Object[] args = this.getArguments();

        this.carCapacity = (int) args[0];

        // register the DF
        this.agentDescription = ServiceUtils.registerDF(this);

        // gets the graph
        try {
            this.graph = GraphUtils.importDefaultGraph();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // generate random sem location
        Set<Semaphore> semaphores = GraphUtils.getSemaphores(this.graph);
        // get random from set
        semaphores.stream().skip(new Random().nextInt(semaphores.size())).findFirst().ifPresent(semaphore -> {
            this.currentLocation = semaphore;
        });

        this.addBehaviour(new CarFSMBehaviour(this));
    }

    public void moveTo(Point point) throws CannotMoveException {
        // TODO take time to move according to the weight in the graph
        if (GraphUtils.isAdjacent(this.graph, this.currentLocation, point)) {
            System.out.printf("%s: moving from [%s] to [%s]%n", this.getLocalName(), this.currentLocation.getName(), point.getName());
            this.currentLocation = point;
        } else {
            throw new CannotMoveException("Cannot move from " + this.currentLocation.getName() + " to " + point.getName());
        }
    }
}
