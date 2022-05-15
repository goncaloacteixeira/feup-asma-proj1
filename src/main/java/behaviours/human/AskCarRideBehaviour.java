package behaviours.human;

import agents.HumanAgent;
import behaviours.car.CarRideContractNetInitiatorBehaviour;
import graph.GraphUtils;
import graph.exceptions.NoRoadsException;
import graph.vertex.Point;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import lombok.Setter;
import messages.CarRideProposeMessage;
import messages.StringMessages;
import messages.results.CarService;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.io.IOException;

/**
 * This behaviour is used to ask a car to ride.
 * <p>
 * It is like an auction, but backwards.
 * 1. The human asks for prices;
 * 2. The cars reply with prices;
 * 3. The human proposes the minimum price to the other cars;
 * 4. Go to step 2 and 3, until there are no more proposals.
 */
public class AskCarRideBehaviour extends Behaviour {

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
     * until a car accepts the offer.
     */
    private float bestValue;

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

        /*
         * Update first edge to include the difference between the best value and the initial cost
         * if the best value > initial cost, then the edge weight will be higher.
         */
        var path = GraphUtils.getPathFromAtoB(fsm.graph, this.start.getName(), this.end.getName());
        float initialCost = (float) GraphUtils.calculateCost(this.fsm.graph, path);
        float expected = (float) GraphUtils.calculateCostForHuman(this.fsm.original, path, (HumanAgent) myAgent);
        for (int i = 0; i < path.getEdgeList().size(); i++) {
            DefaultWeightedEdge e = path.getEdgeList().get(i);
            double weight = fsm.graph.getEdgeWeight(e);
            fsm.graph.setEdgeWeight(e, weight + this.bestValue / path.getEdgeList().size());
        }

        System.out.printf("%s: Car Service Fare: %.02f\n", myAgent.getLocalName(), (this.bestValue - initialCost));
        ((HumanAgent) myAgent).informResults(new CarService(myAgent.getLocalName(), path.getVertexList().toString(), (this.bestValue - initialCost), expected));
        this.done = true;
    }

    public void rejectBestProposal() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(this.bestCar);
        message.setContent(StringMessages.CAR_RIDE_REJECTED);
        this.myAgent.send(message);
    }
}
