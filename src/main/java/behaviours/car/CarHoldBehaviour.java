package behaviours.car;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import messages.StringMessages;

/**
 * For when the car is in talks with the human but is not confirmed.
 */
public class CarHoldBehaviour extends Behaviour {

    private final CarFSMBehaviour carFsmBehaviour;

    private int event = -1;

    private boolean done = false;

    public CarHoldBehaviour(CarFSMBehaviour carFsmBehaviour) {
        this.carFsmBehaviour = carFsmBehaviour;
    }

    @Override
    public void action() {
        // listens for messages
        System.out.printf("%s: waiting for human confirmation\n", this.myAgent.getLocalName());
        ACLMessage msg = this.myAgent.receive();

        if (msg != null) {
            System.out.printf("%s: received message from %s\n", this.myAgent.getLocalName(), msg.getSender().getLocalName());
            // if message is inform
            if (msg.getPerformative() == ACLMessage.INFORM) {
                if (msg.getContent() != null && msg.getContent().equals(StringMessages.CAR_RIDE_CONFIRMED)) {
                    this.event = CarFSMBehaviour.EVENT_CAR_RIDE_CONFIRMED;
                    System.out.printf("%s: human confirmed\n", this.myAgent.getLocalName());
                    this.done = true;
                } else if (msg.getContent() != null && msg.getContent().equals(StringMessages.CAR_RIDE_REJECTED)) {
                    this.event = CarFSMBehaviour.EVENT_CAR_RIDE_REJECTED;
                    System.out.printf("%s: human rejected\n", this.myAgent.getLocalName());
                    this.carFsmBehaviour.removeHuman();
                    this.done = true;
                } else {
                    System.out.printf("%s: received unknown message: %s\n", this.myAgent.getLocalName(), msg.getContent());
                }
            } else if (msg.getPerformative() == ACLMessage.CFP) {
                System.out.printf("%s: received CFP message from %s lol now I don't want it\n", this.myAgent.getLocalName(), msg.getSender().getLocalName());
                // reply with refuse
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                this.myAgent.send(reply);
            }
        }

        if (!this.done) {
            this.block();
        }
    }

    @Override
    public boolean done() {
        return this.done;
    }

    @Override
    public int onEnd() {
        int event = this.event;

        this.reset();

        if (event == -1) {
            return super.onEnd();
        }
        return event;
    }

    @Override
    public void reset() {
        super.reset();
        this.event = -1;
        this.done = false;
    }
}
