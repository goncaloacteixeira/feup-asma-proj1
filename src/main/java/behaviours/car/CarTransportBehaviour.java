package behaviours.car;

import agents.CarAgent;
import graph.vertex.Point;
import jade.core.behaviours.Behaviour;

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
        // if still in the path
        if (this.currentPathIndex < this.carFSMBehaviour.getCurrentPath().getVertexList().size() - 1) {
            // move to the next vertex
            this.currentPathIndex++;
            Point nextPoint = this.carFSMBehaviour.getCurrentPath().getVertexList().get(this.currentPathIndex);
            this.carAgent.moveTo(nextPoint);
        }
        // if arrived at the destination
        else {
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
    }
}
