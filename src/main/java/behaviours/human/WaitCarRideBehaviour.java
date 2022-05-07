package behaviours.human;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import messages.StringMessages;
import utils.ServiceUtils;

/**
 * This behaviour is used to wait for a car to arrive.
 * It knows that a car has arrived when it receives a message with the content "car_arrived".
 */
public class WaitCarRideBehaviour extends Behaviour {

    private final FSMHumanBehaviour fsmHumanBehaviour;

    private boolean done = false;

    public WaitCarRideBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        super(fsmHumanBehaviour.getAgent());

        this.fsmHumanBehaviour = fsmHumanBehaviour;
    }

    @Override
    public void action() {
        // listens for messages
        System.out.printf("%s: waiting for car to arrive\n", this.myAgent.getLocalName());
        ACLMessage msg = this.myAgent.receive();

        if (msg != null) {
            // if message is inform
            if (msg.getPerformative() == ACLMessage.INFORM) {
                if (msg.getContent() != null && msg.getContent().equals(StringMessages.CAR_ARRIVED)) {
                    this.done = true;
                }
            }
        } else {
            this.block();
        }
    }

    @Override
    public boolean done() {
        return this.done;
    }

    @Override
    public int onEnd() {
        System.out.println("Car arrived");

        // deregister the service started by the human who started the car share
        ServiceUtils.deregister(this.myAgent, this.fsmHumanBehaviour.getCurrentCarService());
        this.fsmHumanBehaviour.setCurrentCarService(null);

        return super.onEnd();
    }
}
