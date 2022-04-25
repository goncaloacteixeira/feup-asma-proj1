package behaviours.car;

import graph.vertex.Point;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import messages.CarRideCFPMessage;
import messages.CarRideProposeMessage;

import java.io.IOException;
import java.util.Optional;

public class CarRideContractNetResponderBehaviour extends ContractNetResponder {

    private final CarFSMBehaviour fsm;

    private final CarListeningBehaviour carListeningBehaviour;

    private Point end;

    public CarRideContractNetResponderBehaviour(CarListeningBehaviour carListeningBehaviour, CarFSMBehaviour fsm) {
        super(carListeningBehaviour.getAgent(), MessageTemplate.MatchPerformative(ACLMessage.CFP));

        this.carListeningBehaviour = carListeningBehaviour;
        this.fsm = fsm;
    }
    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        System.out.printf("%s: Received CFP from %s\n", myAgent.getLocalName(), cfp.getSender().getLocalName());
        // gets the message info
        CarRideCFPMessage message = null;
        try {
            message = (CarRideCFPMessage) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            return null;
        }

        // gets the ride info
        Point start = message.getStart();
        this.end = message.getEnd();

        // calculates metrics of the ride
//        var currentPath = FIXME
//        var ridePath = FIXME
        var price = 5; // FIXME
        var distance = 10; // FIXME
        var capacity = 2; // FIXME

        // builds the proposal
        ACLMessage reply = cfp.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        try {
            reply.setContentObject(new CarRideProposeMessage(price, distance, capacity));
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
        this.fsm.setCurrentDestination(this.end);
        this.fsm.setCurrentHuman(accept.getSender());

        ACLMessage reply = accept.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        System.out.printf("%s: Proposal accepted.\n", myAgent.getLocalName());

        this.carListeningBehaviour.setDone(true);
        return reply;
    }
}
