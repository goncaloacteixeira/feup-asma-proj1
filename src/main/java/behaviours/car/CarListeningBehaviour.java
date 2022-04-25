package behaviours.car;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Setter;
import utils.ServiceUtils;

import java.util.Optional;

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
        ServiceUtils.register(this.myAgent, ServiceUtils.CAR_RIDE);
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
        System.out.println("fds");
        return CarFSMBehaviour.EVENT_PROPOSAL_ACCEPTED;
    }
}
