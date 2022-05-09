package behaviours.car;

import agents.CarAgent;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import lombok.Setter;
import utils.ServiceUtils;

class CarListeningBehaviour extends Behaviour {

    private final CarFSMBehaviour fsm;

    @Setter
    private boolean done;

    public CarListeningBehaviour(Agent a, CarFSMBehaviour fsm) {
        super(a);
        this.fsm = fsm;

        this.done = false;
    }

    @Override
    public void onStart() {
        CarAgent carAgent = (CarAgent) this.myAgent;
        ServiceUtils.joinService(carAgent, ServiceUtils.CAR_RIDE);
        System.out.println("Car " + this.myAgent.getLocalName() + " is listening");
        this.myAgent.addBehaviour(new CarRideContractNetResponderBehaviour(this, this.fsm));
    }

    @Override
    public void action() {
    }

    @Override
    public boolean done() {
        return this.done;
    }

    @Override
    public int onEnd() {
        CarAgent carAgent = (CarAgent) this.myAgent;
        ServiceUtils.leaveService(carAgent, ServiceUtils.CAR_RIDE);
        System.out.printf("%s: Not listening anymore\n", this.myAgent.getLocalName());

        this.reset();
        return CarFSMBehaviour.EVENT_PROPOSAL_ACCEPTED;
    }

    @Override
    public void reset() {
        super.reset();
        this.done = false;
    }
}
