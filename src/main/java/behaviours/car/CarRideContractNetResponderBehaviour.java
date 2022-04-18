package behaviours.car;

import graph.vertex.Point;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import messages.CarRideCFPMessage;
import messages.CarRideProposeMessage;

import java.io.IOException;

public class CarRideContractNetResponderBehaviour extends ContractNetResponder {

    private Point start;

    private Point end;

    public CarRideContractNetResponderBehaviour(Agent a, MessageTemplate mt) {
        super(a, mt);
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        System.out.println("Agent " + myAgent.getLocalName() + ": CFP received from " + cfp.getSender().getName());
        // gets the message info
        CarRideCFPMessage message = null;
        try {
            message = (CarRideCFPMessage) cfp.getContentObject();
        } catch (UnreadableException e) {
            e.printStackTrace();
            return null;
        }

        // gets the ride info
        this.start = message.getStart();
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
        this.myAgent.addBehaviour(new CarMoveBehaviour(this.myAgent, this.start));

        ACLMessage reply = accept.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        return reply;
    }
}
