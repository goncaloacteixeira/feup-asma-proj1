package behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import jade.proto.SSContractNetResponder;
import jade.proto.SSIteratedContractNetResponder;
import org.jgrapht.alg.util.Pair;
import utils.ServiceUtils;

import java.util.Map;

public class CarShareContractNetResponder extends SSContractNetResponder {
    private Pair<String, Boolean> done;

    public CarShareContractNetResponder(Agent a, ACLMessage cfp, Pair<String, Boolean> done) {
        super(a, cfp);
        this.done = done;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        System.out.println("Agent " + myAgent.getLocalName() + ": CFP received from " + cfp.getSender().getName() + ". Action is " + cfp.getContent());
        int proposal = evaluateAction();
        if (proposal > 2) {
            // We provide a proposal
            System.out.println("Agent " + myAgent.getLocalName() + ": Proposing " + proposal);
            ACLMessage propose = cfp.createReply();
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContent(String.valueOf(proposal));
            return propose;
        } else {
            // We refuse to provide a proposal
            System.out.println("Agent " + myAgent.getLocalName() + ": Refuse");
            throw new RefuseException("evaluation-failed");
        }
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal accepted");

        System.out.println("Agent " + myAgent.getLocalName() + ": Action successfully performed");
        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        inform.setContent("done");
        return inform;
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println("Agent " + myAgent.getLocalName() + ": Proposal rejected");
    }

    private int evaluateAction() {
        // Simulate an evaluation by generating a random number
        return (int) (Math.random() * 10);
    }

    private boolean performAction() {
        // Simulate action execution by generating a random number
        return (Math.random() > 0.2);
    }


    public int onEnd() {
        this.done.setSecond(Boolean.TRUE);
        reset();
        return super.onEnd();
    }
}
