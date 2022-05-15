package behaviours.human;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import messages.StringMessages;

/**
 * This behaviour is used to wait for a car to arrive.
 * It knows that a car has arrived when it receives a message with the content "car_arrived".
 */
public class WaitCarRideBehaviour extends Behaviour {

    private boolean done = false;

    public WaitCarRideBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        super(fsmHumanBehaviour.getAgent());

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
        System.out.printf("%s: car arrived\n", this.myAgent.getLocalName());
        this.reset();
        return super.onEnd();
    }

    @Override
    public void reset() {
        this.done = false;
        super.reset();
    }
}
