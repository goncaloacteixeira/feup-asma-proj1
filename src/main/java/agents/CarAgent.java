package agents;

import behaviours.car.CarFSMBehaviour;
import graph.GraphUtils;
import graph.vertex.Point;
import graph.vertex.Semaphore;
import jade.core.Agent;
import lombok.Getter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Set;

public class CarAgent extends Agent {

    @Getter
    private int carCapacity;

    @Getter
    private Graph<Point, DefaultWeightedEdge> graph;

    @Getter
    private Point currentLocation;

    @Override
    public void setup() {
        Object[] args = this.getArguments();

        this.carCapacity = (int) args[0];

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

    public void moveTo(Point point) {
        // TODO verify if possible and adjacent
        System.out.printf("%s: moving from [%s] to [%s]%n", this.getLocalName(), this.currentLocation.getName(), point.getName());
        this.currentLocation = point;
    }
}
