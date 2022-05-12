package behaviours.human;

import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import messages.OnArrivalMessage;
import messages.OnPlaceInformMessage;

import java.io.Serializable;

class TravelCarBehaviour extends OneShotBehaviour {
    private final FSMHumanBehaviour fsmHumanBehaviour;
    private int exitValue;

    /**
     * Travel by car, similar to TravelDefaultBehaviour but while there is road it keeps traveling,
     * and if the point is the destination it jumps to DestinationBehaviour
     *
     * @param fsmHumanBehaviour parent behaviour
     */
    public TravelCarBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        this.fsmHumanBehaviour = fsmHumanBehaviour;
    }

    /**
     * The car will send a message anytime it arrives to a new point in the graph.
     * This waits for said messages,
     * and anytime a message arrives, it moves to the next point in the graph
     * // TODO this is assuming that the path of the car and the path of the human is the same
     *
     * When the car arrives to the destination, it also sends a message, this time of type OnArrivalMessage
     */
    @Override
    public void action() {
        ACLMessage msg = this.myAgent.receive();

        if (msg != null) {
            // get object content of message
            try {
                Serializable object = msg.getContentObject();
                if (object instanceof OnPlaceInformMessage onPlaceInformMessage) {
                    // then the car moved to a new point
                    System.out.printf("%s to %s: moved to %s\n", msg.getSender().getLocalName(), this.myAgent.getLocalName(), onPlaceInformMessage.getPlace());
                    // TODO this is assuming that the path of the car and the path of the human is the same
                    this.move();
                    this.exitValue = FSMHumanBehaviour.EVENT_CAR;
                } else if (object instanceof OnArrivalMessage) {
                    System.out.printf("%s to %s: arrived\n", msg.getSender().getLocalName(), this.myAgent.getLocalName());
                    // then the car arrived to the destination
                    this.exitValue = FSMHumanBehaviour.EVENT_CAR_END;
                } else {
                    // TODO
                    System.out.println("Unknown message");
                }
            } catch (UnreadableException e) {
                // TODO
                throw new RuntimeException(e);
            }
        } else {
            this.exitValue = FSMHumanBehaviour.EVENT_CAR;
        }
    }

    @Override
    public int onEnd() {
        return this.exitValue;
    }

    private void move() {
        String message = fsmHumanBehaviour.informTravel();
        System.out.println(message);
        // ((HumanAgent) myAgent).informMovement(msg);

        fsmHumanBehaviour.currentLocationIndex++;
    }
}
