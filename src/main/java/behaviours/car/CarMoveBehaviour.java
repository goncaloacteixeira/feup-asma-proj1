package behaviours.car;

import agents.CarAgent;
import graph.GraphUtils;
import graph.exceptions.CannotMoveException;
import graph.vertex.Point;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import messages.StringMessages;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

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
        // path until first of currentPath
        this.path = GraphUtils.getRoadPathFromAtoB(this.carAgent.getGraph(), this.carAgent.getCurrentLocation().getName(), this.fsm.getCurrentPath().getVertexList().get(0).getName());
        System.out.printf("%s: Moving with path %s\n", this.carAgent.getLocalName(), this.path.getVertexList());
    }

    @Override
    public int onEnd() {
        System.out.printf("%s: Reached humans\n", this.carAgent.getLocalName());
        this.reset();
        return super.onEnd();
    }

    @Override
    public void action() {
        if (this.currentPathIndex < this.path.getVertexList().size() - 1) {
            this.currentPathIndex++;
            try {
                this.carAgent.moveTo(this.path.getVertexList().get(this.currentPathIndex));
            } catch (CannotMoveException e) {
                // won't happen
                throw new RuntimeException(e);
            }
        } else {
            // send message to the human stating that the car has arrived
            ServiceUtils.sendStringMessageToService(this.myAgent, ServiceUtils.buildRideName(this.fsm.getCurrentHuman().getLocalName()), StringMessages.CAR_ARRIVED, ACLMessage.INFORM);
            this.done = true;
        }
    }

    @Override
    public boolean done() {
        return this.done;
    }

    @Override
    public void reset() {
        super.reset();
        this.done = false;
        this.currentPathIndex = 0;
    }
}
