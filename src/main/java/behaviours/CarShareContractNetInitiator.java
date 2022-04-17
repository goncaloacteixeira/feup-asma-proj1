package behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import org.jgrapht.alg.util.Pair;
import utils.ServiceUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.util.function.Function;

public class CarShareContractNetInitiator extends ContractNetInitiator {
    private final Pair<String, Boolean> done;
    private int nResponders;
    private String service;

    public CarShareContractNetInitiator(Agent a, ACLMessage cfp, int nResponders, Pair<String, Boolean> done) {
        super(a, cfp);
        this.nResponders = nResponders;
        this.done = done;
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.println("Agent " + refuse.getSender().getName() + " refused");
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getName() + " failed");
        }
        // Immediate failure --> we will not receive a response from this agent
        nResponders--;
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
        }
        // Evaluate proposals.
        int bestProposal = -1;
        AID bestProposer = null;
        ACLMessage accept = null;
        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                acceptances.addElement(reply);
                int proposal = Integer.parseInt(msg.getContent());
                if (proposal > bestProposal) {
                    bestProposal = proposal;
                    bestProposer = msg.getSender();
                    accept = reply;
                }
            }
        }
        // Accept the proposal of the best proposer
        if (accept != null) {
            System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }
    }

    protected void handleInform(ACLMessage inform) {
        System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
    }

    @Override
    public int onEnd() {
        reset();
        this.done.setSecond(Boolean.TRUE);
        return 0;
    }
}
