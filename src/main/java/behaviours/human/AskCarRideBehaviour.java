package behaviours.human;

import behaviours.car.CarRideContractNetInitiatorBehaviour;
import graph.GraphUtils;
import graph.exceptions.NoRoadsException;
import graph.vertex.Point;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import messages.CarRideProposeMessage;
import messages.StringMessages;

/**
 * This behaviour is used to ask a car to ride.
 *
 * It is like an auction, but backwards.
 *   1. The human asks for prices;
 *   2. The cars reply with prices;
 *   3. The human proposes the minimum price to the other cars;
 *   4. Go to step 2 and 3, until there are no more proposals.
 */
public class AskCarRideBehaviour extends Behaviour{

    private final FSMHumanBehaviour fsm;

    private Point start;

    private Point end;

    @Setter
    private boolean done;

    /**
     * If a discussion with cars is happening
     */
    private boolean isDiscussing;

    /**
     * The price the human is asking for.
     * This is supposed to start as the lowest possible price (the length of the shortest path), and raise by INCREMENT
     *  until a car accepts the offer.
     */
    @Getter
    private float bestValue;

    @Getter
    private AID bestCar;

    public AskCarRideBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        super(fsmHumanBehaviour.getAgent());
        this.fsm = fsmHumanBehaviour;
        this.isDiscussing = false;
        this.done = false;

        this.bestValue = -1;
        this.bestCar = null;
    }

    @Override
    public void onStart() {
        try {
            this.start = fsm.path.getVertexList().get(fsm.currentLocationIndex);
            this.end = GraphUtils.roadStop(fsm.graph, fsm.path, fsm.currentLocationIndex);
            // set value as the length of the shortest path
            var path = GraphUtils.getPathFromAtoB(fsm.graph, this.start.getName(), this.end.getName());
            this.bestValue = (float) GraphUtils.calculateCost(this.fsm.graph, path);

            System.out.printf("%s: starting car ride auction from %s to %s\n", fsm.getAgent().getLocalName(), this.start, this.end);

            // sends the initial request
            this.myAgent.addBehaviour(new CarRideContractNetInitiatorBehaviour(this, this.myAgent, new ACLMessage(ACLMessage.CFP), this.start, this.end));
            this.isDiscussing = true;
        } catch (NoRoadsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset() {
        this.bestValue = -1;
        this.bestCar = null;
        this.isDiscussing = false;
        this.done = false;
        super.reset();
    }

    @Override
    public int onEnd() {
        this.reset();
        return super.onEnd();
    }

    @Override
    public void action() {
        if (this.isDiscussing) {
            // if a discussion is happening, do nothing
            return;
        }

        // if it reaches here it is because we have to discuss a new value
        this.isDiscussing = true;

        // sends a request to the cars with the current value
        System.out.printf("%s: asking other cars to ride from %s to %s with value %f\n", fsm.getAgent().getLocalName(), this.start, this.end, this.bestValue);
        this.myAgent.addBehaviour(new CarRideContractNetInitiatorBehaviour(this, this.myAgent, new ACLMessage(ACLMessage.CFP), this.start, this.end, this.bestValue, this.bestCar.getName()));
    }

    @Override
    public boolean done() {
        return this.done;
    }

    public void setBestProposal(CarRideProposeMessage proposal) {
        this.bestValue = proposal.getPrice();
        this.bestCar = proposal.getCarName();

        this.isDiscussing = false;
    }

    public void confirmBestProposal() {
        // sends message to best car
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(this.bestCar);
        message.setContent(StringMessages.CAR_RIDE_CONFIRMED);
        this.myAgent.send(message);

        this.done = true;
    }

    public void rejectBestProposal() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(this.bestCar);
        message.setContent(StringMessages.CAR_RIDE_REJECTED);
        this.myAgent.send(message);
    }
}
