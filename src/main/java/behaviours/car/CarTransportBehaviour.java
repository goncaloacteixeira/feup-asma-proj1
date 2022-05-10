package behaviours.car;

import agents.CarAgent;
import graph.exceptions.CannotMoveException;
import graph.vertex.Point;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import messages.OnArrivalMessage;
import messages.OnPlaceInformMessage;
import utils.ServiceUtils;

import java.io.IOException;

public class CarTransportBehaviour extends Behaviour {

    private final CarFSMBehaviour carFSMBehaviour;

    private final CarAgent carAgent;

    private int currentPathIndex;

    private boolean done;

    public CarTransportBehaviour(CarFSMBehaviour carFSMBehaviour) {
        super(carFSMBehaviour.getAgent());

        this.carAgent = (CarAgent) this.myAgent;

        this.carFSMBehaviour = carFSMBehaviour;
        this.currentPathIndex = 0;
        this.done = false;
    }

    public int onEnd() {
        // TODO the car can simply quit which would trigger another event? this event will put the car into listening again
        System.out.printf("%s: transport behaviour ended\n", this.carAgent.getLocalName());
        this.reset();
        return CarFSMBehaviour.EVENT_TRAVEL_END;
    }

    @Override
    public void action() {
        try {
            // if still in the path
            if (this.currentPathIndex < this.carFSMBehaviour.getCurrentPath().getVertexList().size() - 1) {
                // move to the next vertex
                this.currentPathIndex++;
                Point nextPoint = this.carFSMBehaviour.getCurrentPath().getVertexList().get(this.currentPathIndex);
                this.carAgent.moveTo(nextPoint);
                // inform humans that the car arrived to the next point
                // TODO if car takes time to move (and it will) this won't be sent right away
                ServiceUtils.sendMessageToService(this.myAgent, ServiceUtils.buildRideName(this.carFSMBehaviour.getCurrentHuman().getLocalName()), new OnPlaceInformMessage(nextPoint), ACLMessage.INFORM);
            }
            // if arrived at the destination
            else {
                Point lastPoint = this.carFSMBehaviour.getCurrentPath().getVertexList().get(this.currentPathIndex);
                ServiceUtils.sendMessageToService(this.myAgent, ServiceUtils.buildRideName(this.carFSMBehaviour.getCurrentHuman().getLocalName()), new OnArrivalMessage(lastPoint), ACLMessage.INFORM);
                this.done = true;
            }
        } catch (CannotMoveException e) {
            // won't happen
            throw new RuntimeException(e);
        } catch (IOException e) {
            // TODO handle this
            throw new RuntimeException(e);
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
    }
}
