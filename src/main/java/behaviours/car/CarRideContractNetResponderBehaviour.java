package behaviours.car;

import agents.CarAgent;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import messages.CarRideCFPBlindRequestMessage;
import messages.CarRideCFPRequestMessage;
import messages.CarRideProposeMessage;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.CarCognitive;

import java.io.IOException;

public class CarRideContractNetResponderBehaviour extends ContractNetResponder {

    private final CarAgent carAgent;

    private final CarFSMBehaviour fsm;

    private final CarListeningBehaviour carListeningBehaviour;

    private GraphPath<Point, DefaultWeightedEdge> path;

    public CarRideContractNetResponderBehaviour(CarListeningBehaviour carListeningBehaviour, CarFSMBehaviour fsm) {
        super(carListeningBehaviour.getAgent(), MessageTemplate.MatchPerformative(ACLMessage.CFP));

        this.carAgent = (CarAgent) myAgent;
        this.carListeningBehaviour = carListeningBehaviour;
        this.fsm = fsm;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        System.out.printf("%s: Received CFP from %s\n", myAgent.getLocalName(), cfp.getSender().getLocalName());

        // rejects if already has human
        // this fixes a bug where the car would accept a ride after accepting others, because of delay
        if (this.fsm.hasHuman()) {
            System.out.printf("%s: Rejecting because our loyalty is with %s\n", myAgent.getLocalName(), this.fsm.getCurrentHuman().getLocalName());
            ACLMessage reply = cfp.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            return reply;
        }

        // gets the message info
        try {
            // if the content is a blind request message
            if (cfp.getContentObject() instanceof CarRideCFPBlindRequestMessage message) {
                return this.handleBlindRequest(cfp, message);
            }
            // if the content is a request message with price
            else if (cfp.getContentObject() instanceof CarRideCFPRequestMessage message) {
                return this.handlePriceRequest(cfp, message);
            } else {
                System.out.printf("%s: Unknown message type.\n", myAgent.getLocalName());
                throw new IllegalArgumentException("Unknown message type");
            }
        } catch (UnreadableException e) {
            // TODO
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        this.fsm.removeHuman();
        System.out.printf("%s: Proposal rejected.\n", myAgent.getLocalName());

        this.carListeningBehaviour.restart();
    }

    protected void handleOutOfSequence(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        // TODO
        System.out.printf("%s: Proposal xdddd out of sequence.\n", myAgent.getLocalName());
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        this.fsm.setCurrentPath(this.path);

        ACLMessage reply = accept.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        System.out.printf("%s: Proposal from %s accepted.\n", myAgent.getLocalName(), accept.getSender().getLocalName());

        this.carListeningBehaviour.setOnHold(true);
        this.carListeningBehaviour.setDone(true);
        return reply;
    }

    private ACLMessage handleBlindRequest(ACLMessage cfp, CarRideCFPBlindRequestMessage message) throws IOException {
        this.fsm.setCurrentHuman(cfp.getSender());

        this.path = GraphUtils.getRoadPathFromAtoB(((CarAgent) this.carListeningBehaviour.getAgent()).getGraph(), message.getStart().getName(), message.getEnd().getName());

        double totalCost = this.getTotalCost(message.getStart());

        float price = CarCognitive.getRidePrice(totalCost);

        // builds the proposal
        ACLMessage reply = cfp.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);

        System.out.printf("%s: Sending proposal with price %f for cost %f.\n", myAgent.getLocalName(), price, totalCost);
        reply.setContentObject(new CarRideProposeMessage(price, carAgent.getCarCapacity(), this.myAgent.getAID()));

        return reply;
    }

    private ACLMessage handlePriceRequest(ACLMessage cfp, CarRideCFPRequestMessage message) throws IOException {
        this.fsm.setCurrentHuman(cfp.getSender());

        this.path = GraphUtils.getRoadPathFromAtoB(((CarAgent) this.carListeningBehaviour.getAgent()).getGraph(), message.getStart().getName(), message.getEnd().getName());

        double totalCost = this.getTotalCost(message.getStart());

        boolean accept = CarCognitive.shouldAcceptRide(totalCost, message.getPrice());

        ACLMessage reply = cfp.createReply();
        if (accept) {
            // if accepts, builds even a better proposal
            float betterPrice = CarCognitive.getBetterRidePrice(totalCost, message.getPrice());
            reply.setPerformative(ACLMessage.PROPOSE);
            reply.setContentObject(new CarRideProposeMessage(betterPrice, this.carAgent.getCarCapacity(), this.myAgent.getAID()));

            System.out.printf("%s: Sending proposal with price %f for cost %f.\n", myAgent.getLocalName(), betterPrice, totalCost);
        } else {
            // if does not accept, sends refuse back
            reply.setPerformative(ACLMessage.REFUSE);

            System.out.printf("%s: Sending refuse\n", myAgent.getLocalName());
        }
        return reply;
    }


    private double getTotalCost(Point start) {
        // gets the path from current location to the start of the ride
        CarAgent carAgent = (CarAgent) this.myAgent;
        var pathToStart = GraphUtils.getRoadPathFromAtoB(carAgent.getGraph(), carAgent.getCurrentLocation().getName(), start.getName());
        double pathToStartCost = pathToStart.getWeight();
        double travelCost = this.path.getWeight();
        return pathToStartCost + travelCost;
    }
}
