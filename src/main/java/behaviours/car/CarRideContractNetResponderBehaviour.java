package behaviours.car;

import agents.CarAgent;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import messages.CarRideCFPMessage;
import messages.CarRideProposeMessage;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.IOException;

public class CarRideContractNetResponderBehaviour extends ContractNetResponder {

    private final CarFSMBehaviour fsm;

    private final CarListeningBehaviour carListeningBehaviour;

    private GraphPath<Point, DefaultWeightedEdge> path;

    public CarRideContractNetResponderBehaviour(CarListeningBehaviour carListeningBehaviour, CarFSMBehaviour fsm) {
        super(carListeningBehaviour.getAgent(), MessageTemplate.MatchPerformative(ACLMessage.CFP));

        this.carListeningBehaviour = carListeningBehaviour;
        this.fsm = fsm;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        System.out.printf("%s: Received CFP from %s\n", myAgent.getLocalName(), cfp.getSender().getLocalName());
        // gets the message info
        CarRideCFPMessage message;
        try {
            message = (CarRideCFPMessage) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            return null;
        }

        // gets the ride info
        Point start = message.getStart();
        Point end = message.getEnd();

        this.path = GraphUtils.getPathFromAtoB(((CarAgent) this.carListeningBehaviour.getAgent()).getGraph(), start.getName(), end.getName());

        // gets the path from current location to the start of the ride
        CarAgent carAgent = (CarAgent) this.myAgent;
        var pathToStart = GraphUtils.getPathFromAtoB(carAgent.getGraph(), carAgent.getCurrentLocation().getName(), start.getName());
        double pathToStartCost = pathToStart.getWeight();
        double travelCost = this.path.getWeight();

        float price = (float) (travelCost + pathToStartCost); // TODO add negotiation that adds profit on top of this

        // builds the proposal
        ACLMessage reply = cfp.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        try {
            reply.setContentObject(new CarRideProposeMessage(price, (float) travelCost, carAgent.getCarCapacity(), this.myAgent.getName()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return reply;
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        // TODO
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal rejected.");
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        this.fsm.setCurrentPath(this.path);
        this.fsm.setCurrentHuman(accept.getSender());

        ACLMessage reply = accept.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        System.out.printf("%s: Proposal accepted.\n", myAgent.getLocalName());

        this.carListeningBehaviour.setDone(true);
        return reply;
    }
}
