package behaviours.human;

import agents.HumanAgent;
import graph.vertex.Point;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import messages.CarShareFullProposalMessage;
import messages.results.ShareRide;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.IOException;
import java.util.Vector;

public class CarShareContractNetInitiator extends ContractNetInitiator {
    private final Pair<String, Boolean> done;
    private final Graph<Point, DefaultWeightedEdge> graph;
    private int nResponders;
    private final GraphPath<Point, DefaultWeightedEdge> roadPath;
    private final String start;
    private final String end;

    private double myPercentage = 0.95;
    private double theirPercentage = 0.05;

    public CarShareContractNetInitiator(Agent a, ACLMessage cfp, int nResponders, Pair<String, Boolean> done, GraphPath<Point, DefaultWeightedEdge> roadPath, Graph<Point, DefaultWeightedEdge> graph, String p1, String p2) {
        super(a, cfp);
        this.nResponders = nResponders;
        this.done = done;
        this.roadPath = roadPath;
        this.graph = graph;
        this.start = p1;
        this.end = p2;
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        System.out.printf("%s: %s proposed %s\n", myAgent.getLocalName(), propose.getSender().getLocalName(), propose.getContent());
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.printf("%s: %s refused\n", myAgent.getLocalName(), refuse.getSender().getLocalName());
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

        // gets the reply with the best proposal
        ACLMessage bestReply = null;
        double bestProposal = 0;
        for (var response : responses) {
            ACLMessage message = (ACLMessage) response;
            if (message.getPerformative() == ACLMessage.PROPOSE) {
                // for all propose messages, starts with a negative reply, sets the positive reply for negotiation at the end
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);

                // gets the value proposed
                double proposal = Double.parseDouble(message.getContent());

                // updates if so
                if (bestReply == null || proposal > bestProposal) {
                    // puts the last best reply in the list of accepted ones
                    if (bestReply != null) {
                        acceptances.addElement(bestReply);
                    }
                    bestReply = reply;
                    bestProposal = proposal;
                } else {
                    acceptances.addElement(reply);
                }
            }
        }

        // puts the best reply in the list
        if (bestReply != null) {
            // gets their raise
            double theirRaise = bestProposal - this.theirPercentage;
            this.theirPercentage = bestProposal;
            System.out.printf("%s: got a raise of %f\n", myAgent.getLocalName(), theirRaise);

            // updates our value tip for tat
            this.myPercentage -= theirRaise;
            if (this.myPercentage < this.theirPercentage) {
                // accepts
                bestReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptances.addElement(bestReply);
            } else {
                bestReply.setPerformative(ACLMessage.CFP);
                try {
                    bestReply.setContentObject(new CarShareFullProposalMessage(this.start, this.end, this.myPercentage));
                } catch (IOException e) {
                    // won't happen
                    throw new RuntimeException(e);
                }
                acceptances.addElement(bestReply);

                this.newIteration(acceptances);
            }
        }
    }

    protected void handleInform(ACLMessage inform) {
        System.out.printf("%s: %s completed ContractNet\n", myAgent.getLocalName(), inform.getSender().getLocalName());
        try {
            /*
             * Decrement weight based on contributions. The INFORM message has a double[] containing the
             * contributions from the proposer
             */
            Double[] contributions = (Double[]) inform.getContentObject();
            for (int i = 0; i < roadPath.getEdgeList().size(); i++) {
                DefaultWeightedEdge e = roadPath.getEdgeList().get(i);
                double weight = graph.getEdgeWeight(e);
                var newTot = weight - contributions[i];
                System.out.printf("%s: %f - %f = %f\n", myAgent.getLocalName(), weight, contributions[i], newTot);
                graph.setEdgeWeight(e, newTot);
            }
            ((HumanAgent) myAgent).informResults(new ShareRide(myAgent.getLocalName(), roadPath.getVertexList().toString(), true));
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onEnd() {
        reset();
        this.done.setSecond(Boolean.TRUE);
        return 0;
    }
}
