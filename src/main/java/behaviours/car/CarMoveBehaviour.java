package behaviours.car;

import agents.CarAgent;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.List;

public class CarMoveBehaviour extends Behaviour {

    private final CarAgent carAgent;

    private final CarFSMBehaviour fsm;

    private GraphPath<Point, DefaultWeightedEdge> path;

    private int currentPathIndex;

    private boolean done;

    public CarMoveBehaviour(CarFSMBehaviour fsm) {
        super(fsm.getAgent());

        this.fsm = fsm;

        this.currentPathIndex = 0;
        this.carAgent = (CarAgent) fsm.getAgent();
        this.done = false;
    }

    @Override
    public void onStart() {
        this.path = GraphUtils.getPathFromAtoB(this.carAgent.getGraph(), this.carAgent.getCurrentLocation().getName(), this.fsm.getCurrentDestination().getName()); // TODO get only roads
    }

    @Override
    public void action() {
        if (this.currentPathIndex < this.path.getVertexList().size() - 1) {
            System.out.printf("%s: moving from [%s] to [%s]%n", this.carAgent.getLocalName(), this.path.getVertexList().get(this.currentPathIndex).getName(), this.path.getVertexList().get(this.currentPathIndex + 1).getName());
            this.currentPathIndex++;
            this.carAgent.moveTo(this.path.getVertexList().get(this.currentPathIndex));
        } else {
            System.out.println("arrrrrrrrrived at destination");
            // send message to the human stating that the car has arrived
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(this.fsm.getCurrentHuman());
            msg.setContent("car_arrived"); // TODO const this shit
            this.myAgent.send(msg);
            this.done = true;
        }
    }

    @Override
    public boolean done() {
        return this.done;
    }
}
